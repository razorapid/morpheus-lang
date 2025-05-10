package com.github.razorapid.morpheus.lang.lexer;

import static com.github.razorapid.morpheus.lang.lexer.LexerStateName.BEGIN;
import static com.github.razorapid.morpheus.lang.lexer.MatchedToken.notMatched;

record BlockCommentState(Lexer lexer) implements LexerState {

    @Override
    public MatchedToken nextToken() {
        MatchedToken token = notMatched();
        while (!(peek() == '*' && peekNext() == '/') && !isEOF()) {
            char n = next();
            if (n == '\n') {
                caret().newLine();
            }
        }
        next();
        next();
        switchTo(BEGIN);
        return token;
    }
}
