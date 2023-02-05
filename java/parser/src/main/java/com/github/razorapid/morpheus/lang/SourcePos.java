package com.github.razorapid.morpheus.lang;

import lombok.Value;

@Value
public class SourcePos {
    long pos;
    long line;
    long col;

    public SourcePos addCol(long offset) {
        return new SourcePos(pos + offset, line, col + offset);
    }

    @Override
    public String toString() {
        return "(l: " + line + ", c: " + col + ')';
    }
}
