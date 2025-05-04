package com.github.razorapid.morpheus.lang.lexer;

import lombok.RequiredArgsConstructor;

import static com.github.razorapid.morpheus.lang.lexer.LexerStateName.BEGIN;
import static com.github.razorapid.morpheus.lang.lexer.MatchedToken.notMatched;

@RequiredArgsConstructor
class SkipTillEolState implements LexerState {
    private final Lexer lexer;

    @Override
    public Lexer lexer() {
        return lexer;
    }

    @Override
    public MatchedToken nextToken() {
        while (!isEOF() && peek() != '\n') next();
        switchTo(BEGIN);
        return notMatched();
    }
}
