package com.github.razorapid.morpheus.lang.lexer;

import com.github.razorapid.morpheus.lang.Token;

interface LexerState {
    Token nextToken();
}
