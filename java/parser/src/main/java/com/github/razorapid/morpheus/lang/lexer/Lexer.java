package com.github.razorapid.morpheus.lang.lexer;

import com.github.razorapid.morpheus.lang.Source;
import com.github.razorapid.morpheus.lang.Tape;
import com.github.razorapid.morpheus.lang.Token;
import com.github.razorapid.morpheus.lang.TokenType;
import com.github.razorapid.morpheus.lang.Tokens;

import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class Lexer {

    private final Map<LexerStateName, LexerState> STATES = Map.of(
        LexerStateName.BEGIN, new BeginState(this),
        LexerStateName.BLOCK_COMMENT, new BlockCommentState(this),
        LexerStateName.FIELD, new FieldState(this),
        LexerStateName.IDENTIFIER, new IdentifierState(this)
    );

    private LexerStateName state = LexerStateName.BEGIN;
    private final Tokens tokens = new Tokens();
    private int startPos = 0;
    private final Caret caret = new Caret();
    private final Tape<Character> source;
    private TokenType prevToken = null;

    public Lexer(Source script) {
        this.source = createSource(requireNonNull(script, "script must not be null"));
    }

    public Optional<Tokens> scan() {
        while (!isEOF()) {
            MatchedToken token = nextToken();
            if (token.isMatched()) {
                tokens.add(token.val());
            }
        }

        return Optional.of(tokens);
    }

    public Optional<Token> scanToken() {
        if (isEOF()) return Optional.empty();
        MatchedToken token;
        do {
            token = nextToken();
        } while (token.isNotMatched());
        return Optional.of(token.val());
    }

    private static Tape<Character> createSource(Source script) {
        String source = script.source() + "\n"; // make sure source end with new line
        return Tape.of(source.chars().mapToObj(c -> (char) c).toArray(Character[]::new));
    }

    private MatchedToken nextToken() {
        LexerState state = currentState();
        startPos = source.pos();
        MatchedToken t = state.nextToken();
        if (t.isMatched()) {
            prevToken = t.val().type();
        }
        return t;
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

    TokenType prevToken() {
        return prevToken;
    }

    char peek() {
        return source.peek();
    }

    char peekNext() {
        return source.peekNext();
    }

    char peek(long pos) {
        return source.peek((int) pos);
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

    MatchedToken addToken(TokenType type) {
        return MatchedToken.matched(
            Token.of(type, sourceString(startPos, source.pos()), startPos, caret.line(), caret.col() - (source.pos() - startPos))
        );
    }
}
