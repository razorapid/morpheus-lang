package com.github.razorapid.morpheus.lang;

enum Operator {
    NULL(-1),
    NONE(0),

    LOGICAL_OR(1),
    LOGICAL_AND(2),
    BITWISE_OR(3),
    BITWISE_XOR(4),
    BITWISE_AND(5),
    EQUALITY(6), INEQUALITY(6),
    LESS_THAN(7), LESS_THAN_OR_EQUAL(7), GREATER_THAN(7), GREATER_THAN_OR_EQUAL(7),
    PLUS(8), MINUS(8),
    MULTIPLY(9), DIVIDE(9), MODULUS(9),
    INDEX(11),
    PROPERTY_COMMAND(12);

    private final int precedence;

    Operator(int precedence) {
        this.precedence = precedence;
    }

    boolean precedenceLowerThan(Operator other) {
        return this.precedence < other.precedence;
    }
}
