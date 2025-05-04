package com.github.razorapid.morpheus.lang.lexer;

enum LexerStateName {
    BEGIN,
    BLOCK_COMMENT,
    FIELD,
    ESCAPED_FIELD,
    IDENTIFIER,
    ESCAPED_IDENTIFIER,
    SKIP_TILL_EOL,
}
