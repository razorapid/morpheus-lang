package com.github.razorapid.morpheus.lang.lexer;

import com.github.razorapid.morpheus.lang.TokenType;

interface LexerState {
    Lexer lexer();
    MatchedToken nextToken();

    default char peek() {
        return lexer().peek();
    }

    default char peekNext() {
        return lexer().peekNext();
    }

    default char peek(int pos) {
        return lexer().peek(pos);
    }

    default boolean match(char c) {
        return lexer().match(c);
    }

    default MatchedToken matched(TokenType type) {
        return lexer().matched(type);
    }

    default MatchedToken matchedEscaped(TokenType type) {
        return lexer().matchedEscaped(type);
    }

    default MatchedToken error(String message) {
        return lexer().error(message);
    }

    default char next() {
        return lexer().next();
    }

    default void currentPos(int pos) {
        lexer().currentPos(pos);
    }

    default int currentPos() {
        return lexer().currentPos();
    }

    default String sourceString(int from, int to) {
        return lexer().sourceString(from, to);
    }

    default int tokenStartPos() {
        return lexer().tokenStartPos();
    }

    default boolean isEOF() {
        return lexer().isEOF();
    }

    default boolean isEOF(int pos) {
        return lexer().isEOF(pos);
    }

    default void switchTo(LexerStateName newState) {
        lexer().switchTo(newState);
    }

    default LexerState currentState() {
        return lexer().currentState();
    }

    default Caret caret() {
        return lexer().caret();
    }

    default TokenType prevToken() {
        return lexer().prevTokenType();
    }
}
