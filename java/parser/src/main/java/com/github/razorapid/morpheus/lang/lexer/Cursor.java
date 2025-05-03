package com.github.razorapid.morpheus.lang.lexer;

import lombok.Data;

@Data
class Cursor {
    private int line = 1;
    private int col = 1;

    void newLine() {
        line++;
        col = 1;
    }

    void right() {
        col++;
    }

    void right(int offset) {
        col += offset;
    }
}
