package com.github.razorapid.morpheus.lang.lexer;

import com.github.razorapid.morpheus.lang.Source;
import com.github.razorapid.morpheus.lang.Tape;
import com.github.razorapid.morpheus.lang.Token;
import com.github.razorapid.morpheus.lang.TokenType;
import com.github.razorapid.morpheus.lang.Tokens;

import java.util.Map;
import java.util.Optional;

import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_EOF;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_ERROR;
import static com.github.razorapid.morpheus.lang.lexer.LexerStateName.BEGIN;
import static com.github.razorapid.morpheus.lang.lexer.LexerStateName.BLOCK_COMMENT;
import static com.github.razorapid.morpheus.lang.lexer.LexerStateName.ESCAPED_FIELD;
import static com.github.razorapid.morpheus.lang.lexer.LexerStateName.ESCAPED_IDENTIFIER;
import static com.github.razorapid.morpheus.lang.lexer.LexerStateName.FIELD;
import static com.github.razorapid.morpheus.lang.lexer.LexerStateName.IDENTIFIER;
import static com.github.razorapid.morpheus.lang.lexer.LexerStateName.SKIP_TILL_EOL;
import static java.util.Objects.requireNonNull;

public class Lexer {

    private final Map<LexerStateName, LexerState> STATES = Map.of(
        BEGIN, new BeginState(this),
        BLOCK_COMMENT, new BlockCommentState(this),
        FIELD, new FieldState(this, false),
        ESCAPED_FIELD, new FieldState(this, true),
        IDENTIFIER, new IdentifierState(this, false),
        ESCAPED_IDENTIFIER, new IdentifierState(this, true),
        SKIP_TILL_EOL, new SkipTillEolState(this)
    );

    private final Tape<Character> source;
    private final Caret caret = new Caret();
    private LexerStateName state = BEGIN;
    private Token prevToken = null;
    private int startPos = 0;

    public Lexer(Source script) {
        this.source = createSource(requireNonNull(script, "script must not be null"));
    }

    public Tokens scan() {
        Tokens tokens = new Tokens();
        Token t;
        do {
            t = scanToken();
            tokens.add(t);
        } while (!t.isType(TOKEN_EOF));

        return tokens;
    }

    public Token scanToken() {
        if (prevTokenType() == TOKEN_EOF) return prevToken();
        MatchedToken token;
        do {
            token = nextToken();
        } while (token.isNotMatched());

        return token.val();
    }

    private static Tape<Character> createSource(Source script) {
        String source = script.source() + "\n"; // make sure source end with new line
        return Tape.of(source.chars().mapToObj(c -> (char) c).toArray(Character[]::new));
    }

    private MatchedToken nextToken() {
        startPos = currentPos();
        return isEOF() ? matched(TOKEN_EOF) : currentState().nextToken();
    }

    LexerState currentState() {
        return STATES.get(state);
    }

    void switchTo(LexerStateName newState) {
        state = newState;
    }

    int currentPos() {
        return source.pos();
    }

    void currentPos(int pos) {
        caret.right(pos - source.pos());
        source.pos(pos);
    }

    int tokenStartPos() {
        return startPos;
    }

    boolean isEOF() {
        return source.isEOB();
    }

    boolean isEOF(int pos) {
        return source.isEOB(pos);
    }

    String sourceString(int from, int to) {
        Character[] data = source.data(from, to);
        StringBuilder sb = new StringBuilder(data.length);
        for (char c : data) {
            sb.append(c);
        }
        return sb.toString();
    }

    Caret caret() {
        return caret;
    }

    TokenType prevTokenType() {
        return prevToken != null ? prevToken.type() : null;
    }

    Token prevToken() {
        return prevToken;
    }

    char peek() {
        return source.peek();
    }

    char peekNext() {
        return source.peekNext();
    }

    char peek(int pos) {
        return source.peek(pos);
    }

    char next() {
        caret.right();
        return source.next();
    }

    boolean match(char c) {
        boolean matched = source.match(c);
        if (matched) {
            caret.right();
        }
        return matched;
    }

    MatchedToken matched(TokenType type) {
        Token t = Token.of(type, sourceString(startPos, source.pos()), startPos, caret.line(), caret.col() - (source.pos() - startPos));
        prevToken = t;
        return MatchedToken.matched(t);
    }

    MatchedToken matchedEscaped(TokenType type) {
        Token t = Token.of(type, sourceString(startPos, source.pos()), startPos, caret.line(), caret.col() - (source.pos() - startPos));
        prevToken = t;
        return MatchedToken.matched(t);
    }

    MatchedToken error(String message) {
        Token t = Token.of(TOKEN_ERROR, message, startPos, caret.line(), caret.col() - (source.pos() - startPos));
        prevToken = t;
        return MatchedToken.matched(t);
    }
}
