package com.github.razorapid.morpheus.lang.lexer;

interface LexerState {
    MatchedToken nextToken();
}
