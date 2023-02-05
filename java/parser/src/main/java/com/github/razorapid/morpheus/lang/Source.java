package com.github.razorapid.morpheus.lang;

import lombok.Value;

import java.util.Optional;

@Value
public class Source {
    String name;
    String source;

    public Optional<String> line(long idx) {
        var lastNewLineCol = -1L;
        var currentNewLineCol = 1L;
        var currentNewlineIdx = 1L;
        for (long i = 0; i <= source.length(); i++) {
            if (isNewLine(i) || isEOF(i)) {
                currentNewLineCol = i;
                if (currentNewlineIdx == idx) {
                    return Optional.of(source.substring((int) lastNewLineCol + 1, (int) currentNewLineCol));
                }
                currentNewlineIdx++;
                lastNewLineCol = currentNewLineCol;
            }
        }
        return Optional.empty();
    }

    private boolean isEOF(long pos) {
        return pos == source.length();
    }

    private boolean isNewLine(long pos) {
        return (pos < source.length() && source.charAt((int) pos) == '\n') ||
                (pos < source.length() - 1 && source.charAt((int) pos) == '\r' && source.charAt(Math.toIntExact(pos + 1)) == '\n');
    }
}
