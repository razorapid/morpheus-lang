package com.github.razorapid.morpheus.lang.parser;

import com.github.razorapid.morpheus.lang.Source;
import com.github.razorapid.morpheus.lang.SourcePos;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class ParseError extends RuntimeException {
    Source script;
    SourcePos pos;
    String error;

    public String errorMessage() {
        var sb = new StringBuilder();

        scriptLine(sb);
        scriptLocation(sb);
        errorPlace(sb);
        error(sb);

        return sb.toString();
    }

    private void error(StringBuilder sb) {
        sb.append("^~^~^ Script file parse error: ").append(error).append(" (l: ").append(pos.line()).append(", c: ").append(pos.col()).append(")\n");
    }

    private StringBuilder errorPlace(StringBuilder sb) {
        return sb.append(String.format("%1$" + (pos.col() + 1) + "s", "^\n"));
    }

    private StringBuilder scriptLocation(StringBuilder sb) {
        return sb.append(" (").append(script.name()).append(", ").append(pos.line()).append(")\n");
    }

    private StringBuilder scriptLine(StringBuilder sb) {
        return sb.append(script.line(pos.line()).orElse(""));
    }
}
