package com.github.razorapid.morpheus.lang.lexer;

import lombok.RequiredArgsConstructor;

import static com.github.razorapid.morpheus.lang.lexer.LexerStateName.BEGIN;
import static com.github.razorapid.morpheus.lang.lexer.MatchedToken.notMatched;

@RequiredArgsConstructor
class BlockCommentState implements LexerState {

    private final Lexer lexer;

    @Override
    public Lexer lexer() {
        return lexer;
    }

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
