package com.github.razorapid.morpheus.lang.lexer;

import static com.github.razorapid.morpheus.lang.lexer.LexerStateName.BEGIN;
import static com.github.razorapid.morpheus.lang.lexer.MatchedToken.notMatched;

record SkipTillEolState(Lexer lexer) implements LexerState {

    @Override
    public MatchedToken nextToken() {
        while (!isEOF() && peek() != '\n') next();
        switchTo(BEGIN);
        return notMatched();
    }
}
