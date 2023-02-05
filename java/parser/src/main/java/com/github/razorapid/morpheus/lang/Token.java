package com.github.razorapid.morpheus.lang;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Token {
    TokenType type;
    String lexeme;
    SourcePos pos;

    public static Token of(TokenType type, String lexeme, long pos, long line, long col) {
        return new Token(type, lexeme, new SourcePos(pos, line, col));
    }

    public boolean isType(TokenType type) {
        return this.type == type;
    }

    public long line() {
        return pos.line();
    }

    public long col() {
        return pos.col();
    }

    @Override
    public String toString() {
        return type.name() + " " + lexeme + " " + pos;
    }
}
