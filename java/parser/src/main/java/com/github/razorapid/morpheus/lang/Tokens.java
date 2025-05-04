package com.github.razorapid.morpheus.lang;

import java.util.ArrayList;
import java.util.List;

import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_EOF;

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

    public Token lastToken() {
        return lastToken;
    }

    public void restore(long pos) {
        currentPos = pos;
    }

    public Token consume(TokenType... type) {
        return match(type) ? lastToken : null;
    }

    public long mark() {
        return currentPos;
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
        var result = tokens.get((int) currentPos);
        currentPos++;
        return result;
    }

    public Token peekToken() {
        return peekTokenAhead(0);
    }

    public Token peekTokenAhead(long lookAhead) {
        return currentPos + lookAhead < tokens.size() ?
                tokens.get(Math.toIntExact(currentPos + lookAhead)) :
                Token.of(TOKEN_EOF, "", -1, -1, -1);
    }

    public boolean isEOF() {
        return peekToken().isType(TOKEN_EOF) || currentPos >= tokens.size();
    }

    public Token get(long idx) {
        return tokens.get((int) idx);
    }

    public List<Token> list() {
        return new ArrayList<>(tokens);
    }

    public void add(TokenType type, String lexeme, long pos, long line, long col) {
        add(Token.of(type, lexeme, pos, line, col));
    }

    public void add(Token token) {
        tokens.add(token);
    }

    public long size() {
        return tokens.size();
    }

    public void rewind(long offset) {
        currentPos -= offset;
    }
}
