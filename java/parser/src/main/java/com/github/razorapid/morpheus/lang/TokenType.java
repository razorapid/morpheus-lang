package com.github.razorapid.morpheus.lang;

public enum TokenType {
    TOKEN_CASE("case"),
    TOKEN_IF("if"),
    TOKEN_ELSE("else"),
    TOKEN_WHILE("while"),
    TOKEN_FOR("for"),
    TOKEN_TRY("try"),
    TOKEN_CATCH("catch"),
    TOKEN_SWITCH("switch"),
    TOKEN_BREAK("break"),
    TOKEN_CONTINUE("continue"),
    TOKEN_END("end"),
    TOKEN_MAKEARRAY("makeArray"),
    TOKEN_ENDARRAY("endArray"),
    TOKEN_DOUBLE_COLON("::"),
    TOKEN_ASSIGNMENT("="),
    TOKEN_PLUS_EQUALS("+="),
    TOKEN_MINUS_EQUALS("-="),
    TOKEN_INC("++"),
    TOKEN_DEC("--"),
    TOKEN_EQUALITY("=="),
    TOKEN_INEQUALITY("!="),
    TOKEN_LESS_THAN_OR_EQUAL("<="),
    TOKEN_LESS_THAN("<"),
    TOKEN_GREATER_THAN_OR_EQUAL(">="),
    TOKEN_GREATER_THAN(">"),
    TOKEN_LOGICAL_AND("&&"),
    TOKEN_LOGICAL_OR("||"),
    TOKEN_BITWISE_AND("&"),
    TOKEN_BITWISE_EXCL_OR("^"),
    TOKEN_BITWISE_OR("|"),
    TOKEN_LEFT_BRACKET("("),
    TOKEN_RIGHT_BRACKET(")"),
    TOKEN_LEFT_SQUARE_BRACKET("["),
    TOKEN_RIGHT_SQUARE_BRACKET("]"),
    TOKEN_LEFT_BRACES("{"),
    TOKEN_RIGHT_BRACES("}"),
    TOKEN_DOLLAR("$"),
    TOKEN_PERIOD("."),
    TOKEN_MINUS("-"),
    TOKEN_NEG(" -"),
    TOKEN_COMPLEMENT("~"),
    TOKEN_NOT("!"),
    TOKEN_PERCENTAGE("%"),
    TOKEN_DIVIDE("/"),
    TOKEN_MULTIPLY("*"),
    TOKEN_PLUS("+"),
    TOKEN_POS(" +"),
    TOKEN_NULL("NULL"),
    TOKEN_NIL("NIL"),
    TOKEN_LISTENER("game, level, group, local, self"),
    TOKEN_SIZE("size"),
    TOKEN_STRING("\"text\""),
    TOKEN_INTEGER("1234"),
    TOKEN_FLOAT("10.75"),
    TOKEN_IDENTIFIER("someNameWithoutQuotes"),
    TOKEN_COLON(":"),
    TOKEN_SEMICOLON(";"),
    TOKEN_EOL("\n"),
    TOKEN_EOF(null);

    private final String def;

    TokenType(String def) {
        this.def = def;
    }

    public String def() {
        return def;
    }

    public String nameWithExample() {
        if (def() != null) {
            return name() + " (ie. " + def() + ")";
        } else {
            return name();
        }
    }
}
