package com.github.razorapid.morpheus.lang;

import java.util.ArrayList;
import java.util.List;

/**
 * Token stream for operations on tokens scanned by the lexer
 */
public class Tokens {
    private final List<Token> tokens = new ArrayList<>();
    private long currentPos = 0;
    private Token lastToken;

    public static Tokens of(List<Token> tokens) {
        var result = new Tokens();
        tokens.forEach(result::add);
        return result;
    }

    Token lastToken() {
        return lastToken;
    }

    void restore(long pos) {
        currentPos = pos;
    }

    Token consume(TokenType... type) {
        return match(type) ? lastToken : null;
    }

    long mark() {
        return currentPos;
    }

    boolean match(TokenType... types) {
        for (var type : types) {
            if (check(type)) {
                lastToken = nextToken();
                return true;
            }
        }
        return false;
    }

    boolean check(TokenType... types) {
        for (var type : types) {
            if (peekToken().isType(type)) {
                return true;
            }
        }
        return false;
    }

    Token nextToken() {
        var result = tokens.get((int) currentPos);
        currentPos++;
        return result;
    }

    Token peekToken() {
        return peekTokenAhead(0);
    }

    Token peekTokenAhead(long lookAhead) {
        return currentPos + lookAhead < tokens.size() ?
                tokens.get(Math.toIntExact(currentPos + lookAhead)) :
                Token.of(TokenType.TOKEN_EOF, "", -1, -1, -1);
    }

    boolean isEOF() {
        return currentPos >= tokens.size();
    }

    Token get(long idx) {
        return tokens.get((int) idx);
    }

    List<Token> list() {
        return new ArrayList<>(tokens);
    }

    void add(TokenType type, String lexeme, long pos, long line, long col) {
        add(Token.of(type, lexeme, pos, line, col));
    }

    void add(Token token) {
        tokens.add(token);
    }

    long size() {
        return tokens.size();
    }

    void rewind(long offset) {
        currentPos -= offset;
    }
}
