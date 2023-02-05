package com.github.razorapid.morpheus.lang.cst;

import com.github.razorapid.morpheus.lang.Token;
import lombok.Value;

import java.util.Arrays;
import java.util.List;

@Value
public class ConcreteSyntaxTree {
    Node program;

    public enum NodeType {
        STATEMENT_LINE,
        EOL,
        STATEMENT,
        COMPOUND_STATEMENT,
        LABEL_STATEMENT,
        SELECTION_STATEMENT,
        ITERATION_STATEMENT,
        TRY_CATCH_STATEMENT,
        BREAK_STATEMENT,
        CONTINUE_STATEMENT,
        NOOP_STATEMENT,
        EXPRESSION_STATEMENT,
        THREAD_FUNCTION_CALL_EXPRESSION,
        LISTENER_FUNCTION_CALL_EXPRESSION,
        ASSIGNMENT_EXPRESSION,
        INCREMENT_EXPRESSION,
        DECREMENT_EXPRESSION,
        EVENT_PARAMETER_LIST,
        NON_IDENTIFIER_PRIMARY_EXPRESSION,
        SWITCH_CASE_LABEL_STATEMENT,
        THREAD_LABEL_STATEMENT,
        IF_ELSE_STATEMENT,
        SWITCH_STATEMENT,
        WHILE_STATEMENT,
        FOR_STATEMENT,
        PRIMARY_EXPRESSION,
        CONST_ARRAY_EXPRESSION,
        IDENTIFIER_PRIMARY_EXPRESSION,
        IDENTIFIER_LITERAL,
        EXPRESSION,
        FUNCTION_PRIMARY_EXPRESSION,
        BINARY_EXPRESSION,
        UNARY_FUNCTION_PRIMARY_EXPRESSION,
        ARITHMETIC_NEGATION_FUNCTION_EXPRESSION,
        BITWISE_COMPLETION_FUNCTION_EXPRESSION,
        LOGICAL_NEGATION_FUNCTION_EXPRESSION,
        VECTOR_DECLARATION_EXPRESSION,
        GROUPING_EXPRESSION,
        MAKE_ARRAY_EXPRESSION,
        MAKE_ARRAY_ROW_EXPRESSION,
        BLOCK_START,
        BLOCK_END,
        SEMICOLON,
        IDENTIFIER,
        INFIX_OPERATOR,
        POSTFIX_OPERATOR,
        COLON,
        DOUBLE_COLON,
        SCALAR_COMPONENT_EXPRESSION,
        IDENTIFIER_SCALAR_COMPONENT_EXPRESSION,
        NON_IDENTIFIER_SCALAR_COMPONENT_EXPRESSION,
        TARGETNAME_EXPRESSION,
        PREFIX_OPERATOR,
        MEMBER_SELECTION_EXPRESSION,
        SUBSCRIPT_EXPRESSION,
        KEYWORD,
        UNARY_NON_IDENTIFIER_EXPRESSION,
        LITERAL_EXPRESSION,
        LITERAL,
        MULTIPLICATION_EXPRESSION,
        DIVISION_EXPRESSION,
        MODULO_EXPRESSION,
        ADDITION_EXPRESSION,
        SUBTRACTION_EXPRESSION,
        LESS_THAN_EXPRESSION,
        GREATER_THAN_EXPRESSION,
        LESS_THAN_OR_EQUALS_EXPRESSION,
        GREATER_THAN_OR_EQUAL_EXPRESSION,
        EQUALITY_EXPRESSION,
        INEQUALITY_EXPRESSION,
        BITWISE_AND_EXPRESSION,
        BITWISE_XOR_EXPRESSION,
        BITWISE_OR_EXPRESSION,
        LOGICAL_AND_EXPRESSION,
        LOGICAL_OR_EXPRESSION,
        ARITHMETIC_NEGATION_NON_IDENTIFIER_EXPRESSION,
        BITWISE_COMPLEMENT_NON_IDENTIFIER_EXPRESSION,
        LOGICAL_NEGATION_NON_IDENTIFIER_EXPRESSION,
        NULL_LITERAL,
        NIL_LITERAL,
        LISTENER_LITERAL,
        FLOAT_LITERAL,
        INTEGER_LITERAL,
        STRING_LITERAL,
        STATEMENT_LIST
    }

    public interface Node {
        NodeType type();
        String name();

        <T> T accept(ConcreteSyntaxTreeVisitor<T> visitor);
    }

    public interface TerminalNode extends Node {
    }
    public interface NonTerminalNode extends Node {
        List<Node> children();
        default <T extends Node> T childAt(long id) {
            return (T)children().get((int) id);
        }
    }

    @Value
    public static class TokenNode implements TerminalNode {
        NodeType type;
        Token value;

        @Override
        public NodeType type() {
            return type;
        }

        @Override
        public String name() {
            return value.type().name();
        }

        @Override
        public <T> T accept(ConcreteSyntaxTreeVisitor<T> visitor) {
            return visitor.visitToken(this);
        }
    }

    @Value
    public static class ExpressionNode implements NonTerminalNode {
        NodeType type;
        String name;
        List<Node> children;

        @Override
        public NodeType type() {
            return type;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public <T> T accept(ConcreteSyntaxTreeVisitor<T> visitor) {
            return visitor.visitExpression(this);
        }

        @Override
        public List<Node> children() {
            return children;
        }
    }

    @Value
    public static class StatementNode implements NonTerminalNode {
        NodeType type;
        String name;
        List<Node> children;

        @Override
        public NodeType type() {
            return type;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public <T> T accept(ConcreteSyntaxTreeVisitor<T> visitor) {
            return visitor.visitStatement(this);
        }

        @Override
        public List<Node> children() {
            return children;
        }
    }

    static Node createTokenNode(NodeType type, Token token) {
        return new TokenNode(type, token);
    }

    static Node createStatementNode(NodeType type, String name, Node... children) {
        return new StatementNode(type, name, Arrays.stream(children).toList());
    }

    static StatementNode createStatementNode(NodeType type, String name, List<Node> children) {
        return new StatementNode(type, name, children);
    }

    static Node createExpressionNode(NodeType type, String name, Node... children) {
        return new ExpressionNode(type, name, Arrays.stream(children).toList());
    }

    static Node createExpressionNode(NodeType type, String name, List<Node> children) {
        return new ExpressionNode(type, name, children);
    }
}
