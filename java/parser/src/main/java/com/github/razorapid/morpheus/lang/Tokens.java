package com.github.razorapid.morpheus.lang;

import java.util.List;
import java.util.Objects;

import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_EOF;
import static java.util.Collections.emptyList;

/**
 * Token stream for operations on tokens scanned by the lexer
 */
public class Tokens {
    private final Tape<Token> tokens;
    private Token lastToken;

    private Tokens(Tape<Token> tokens) {
        this.tokens = tokens;
    }

    public static Tokens create() {
        return of(emptyList());
    }

    public static Tokens of(List<Token> tokens) {
        Objects.requireNonNull(tokens);
        return new Tokens(Tape.of(tokens));
    }

    public Token lastToken() {
        return lastToken;
    }

    public void restore(int pos) {
        tokens.pos(pos);
    }

    public Token consume(TokenType... type) {
        return match(type) ? lastToken : null;
    }

    public int mark() {
        return tokens.pos();
    }

    public boolean match(TokenType... types) {
        for (var type : types) {
            if (check(type)) {
                lastToken = nextToken();
                return true;
            }
        }
        return false;
    }

    public boolean check(TokenType... types) {
        for (var type : types) {
            if (peekToken().isType(type)) {
                return true;
            }
        }
        return false;
    }

    public Token nextToken() {
        return tokens.next();
    }

    public Token peekToken() {
        return peekTokenAhead(0);
    }

    public Token peekTokenAhead(int lookAhead) {
        Token t = tokens.peekNext(lookAhead);
        return t != null ? t : Token.of(TOKEN_EOF, "", -1, -1, -1);
    }

    public boolean isEOF() {
        return tokens.isEOB() || tokens.peek().isType(TOKEN_EOF);
    }

    public Token get(int idx) {
        return tokens.peek(idx);
    }

    public List<Token> list() {
        return tokens.data();
    }

    public void add(Token token) {
        tokens.add(token);
    }

    public long size() {
        return tokens.size();
    }

    public void rewind(int offset) {
        tokens.backward(offset);
    }
}
