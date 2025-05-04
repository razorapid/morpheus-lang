package com.github.razorapid.morpheus.lang.lexer;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class BlockCommentState implements LexerState {

    private final Lexer lexer;

    @Override
    public Lexer lexer() {
        return lexer;
    }

    @Override
    public MatchedToken nextToken() {
        MatchedToken token = MatchedToken.notMatched();
        while (!(peek() == '*' && peekNext() == '/') && !isEOF()) {
            char n = next();
            if (n == '\n') {
                caret().newLine();
            }
        }
        next();
        next();
        switchTo(LexerStateName.BEGIN);
        return token;
    }
}
