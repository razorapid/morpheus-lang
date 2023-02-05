package com.github.razorapid.morpheus.lang.cst;

import com.github.razorapid.morpheus.lang.Token;

import java.util.ArrayList;
import java.util.List;

import static com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree.*;
import static com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree.NodeType.*;

public class ConcreteSyntaxTreeBuilder {

    public StatementNode statementList(List<Node> statements) {
        return createStatementNode(STATEMENT_LIST, "statementList", statements);
    }

    public Node statementLine(Node statement, Token tokenEol) {
        return tokenEol != null ?
                createStatementNode(STATEMENT_LINE, "statementLine", statement, createTokenNode(EOL, tokenEol)) :
                createStatementNode(STATEMENT_LINE,"statementLine", statement);
    }

    public Node statement(Node statement) {
        return createStatementNode(STATEMENT, "statement", statement);
    }

    public StatementNode compoundStatement(Token leftBraces, List<Node> statements, Token rightBraces) {
        var nodes = new ArrayList<Node>();
        nodes.add(createTokenNode(BLOCK_START, leftBraces));
        nodes.addAll(statements);
        nodes.add(createTokenNode(BLOCK_END, rightBraces));
        return createStatementNode(COMPOUND_STATEMENT, "compoundStatement", nodes);
    }

    public Node labelStatement(Node statement) {
        return createStatementNode(LABEL_STATEMENT, "labelStatement", statement);
    }

    public Node selectionStatement(Node statement) {
        return createStatementNode(SELECTION_STATEMENT, "selectionStatement", statement);
    }

    public Node iterationStatement(Node statement) {
        return createStatementNode(ITERATION_STATEMENT, "iterationStatement", statement);
    }

    public Node tryCatchStatement(Token tokenTry, Node tryStatement, Token tokenCatch, Node catchStatement) {
        return createStatementNode(TRY_CATCH_STATEMENT, "tryCatchStatement",
                createTokenNode(KEYWORD, tokenTry),
                tryStatement,
                createTokenNode(KEYWORD, tokenCatch),
                catchStatement
        );
    }

    public Node breakStatement(Token token) {
        return createStatementNode(BREAK_STATEMENT, "breakStatement", createTokenNode(KEYWORD, token));
    }

    public Node continueStatement(Token token) {
        return createStatementNode(CONTINUE_STATEMENT, "continueStatement", createTokenNode(KEYWORD, token));
    }

    public Node noopStatement(Token token) {
        return createStatementNode(NOOP_STATEMENT, "noopStatement", createTokenNode(SEMICOLON, token));
    }

    public Node expressionStatement(Node statement) {
        return createStatementNode(EXPRESSION_STATEMENT, "expressionStatement", statement);
    }

    public Node threadFunctionCallExpression(Token tokenIdentOrEnd, Node eventParamList) {
        return createExpressionNode(THREAD_FUNCTION_CALL_EXPRESSION, "threadFunctionCallExpression", createTokenNode(IDENTIFIER, tokenIdentOrEnd), eventParamList);
    }

    public Node listenerFunctionCallExpression(Node expression, Token tokenIdentifier, Node eventParamList) {
        return createExpressionNode(LISTENER_FUNCTION_CALL_EXPRESSION, "listenerFunctionCallExpression", expression, createTokenNode(IDENTIFIER, tokenIdentifier), eventParamList);
    }

    public Node assignmentExpression(Node lhs, Token tokenAssignmentOp, Node rhs) {
        return createExpressionNode(ASSIGNMENT_EXPRESSION, "assignmentExpression", lhs, createTokenNode(INFIX_OPERATOR, tokenAssignmentOp), rhs);
    }

    public Node incrementExpression(Node lhs, Token tokenOp) {
        return createExpressionNode(INCREMENT_EXPRESSION, "incrementExpression", lhs, createTokenNode(POSTFIX_OPERATOR, tokenOp));
    }

    public Node decrementExpression(Node lhs, Token tokenOp) {
        return createExpressionNode(DECREMENT_EXPRESSION, "decrementExpression", lhs, createTokenNode(POSTFIX_OPERATOR, tokenOp));
    }

    public Node eventParameterList(List<Node> expressions) {
        return createExpressionNode(EVENT_PARAMETER_LIST, "eventParameterList", expressions);
    }

    public Node nonIdentifierPrimaryExpression(Node expression) {
        return createExpressionNode(NON_IDENTIFIER_PRIMARY_EXPRESSION, "nonIdentifierPrimaryExpression", expression);
    }

    public Node switchCaseLabelStatement(Token tokenCase, Token tokenIntOrIdent, Node eventParamList, Token tokenColon) {
        return createStatementNode(SWITCH_CASE_LABEL_STATEMENT, "switchCaseLabelStatement",
                createTokenNode(KEYWORD, tokenCase),
                createTokenNode(IDENTIFIER, tokenIntOrIdent),
                eventParamList,
                createTokenNode(COLON, tokenColon)
        );
    }

    public Node threadLabelStatement(Token tokenIdentOrEnd, Node eventParamList, Token tokenColon) {
        return createStatementNode(THREAD_LABEL_STATEMENT, "threadLabelStatement", createTokenNode(IDENTIFIER, tokenIdentOrEnd), eventParamList, createTokenNode(COLON, tokenColon));
    }

    public Node ifElseStatement(Token ifToken, Node primaryExpression, Node statement, Token tokenElse, Node elseStatement) {
        if (tokenElse != null && elseStatement != null) {
            return createStatementNode(IF_ELSE_STATEMENT, "ifElseStatement",
                    createTokenNode(KEYWORD, ifToken),
                    primaryExpression,
                    statement,
                    createTokenNode(KEYWORD, tokenElse),
                    elseStatement
            );
        }
        return createStatementNode(IF_ELSE_STATEMENT, "ifElseStatement",
                createTokenNode(KEYWORD, ifToken),
                primaryExpression,
                statement
        );
    }

    public Node switchStatement(Token tokenSwitch, Node primaryExpression, Node compoundStatement) {
        return createStatementNode(SWITCH_STATEMENT, "switchStatement", createTokenNode(KEYWORD, tokenSwitch), primaryExpression, compoundStatement);
    }

    public Node whileStatement(Token tokenWhile, Node primaryExpression, Node loopStatement) {
        return createStatementNode(WHILE_STATEMENT, "whileStatement", createTokenNode(KEYWORD, tokenWhile), primaryExpression, loopStatement);
    }

    public Node forStatement(
            Token tokenFor, Token tokenLeftBracket,
            Node preStatement, Token tokenStatementSemicolon,
            Node expression, Token expressionSemicolon,
            Node postStatements, Token tokenRightBracket, Node loopStatement
    ) {
        return createStatementNode(
                FOR_STATEMENT,
                "forStatement",
                createTokenNode(KEYWORD, tokenFor), createTokenNode(BLOCK_START, tokenLeftBracket),
                preStatement,
                createTokenNode(SEMICOLON, tokenStatementSemicolon),
                expression,
                createTokenNode(SEMICOLON, expressionSemicolon),
                postStatements,
                createTokenNode(BLOCK_END, tokenRightBracket),
                loopStatement
        );
    }

    public Node primaryExpression(Node expression) {
        return createExpressionNode(PRIMARY_EXPRESSION, "primaryExpression", expression);
    }

    public Node constArrayExpression(List<Node> elems) {
        return createExpressionNode(CONST_ARRAY_EXPRESSION, "constArrayExpression", elems);
    }

    public Node identifierPrimaryExpression(Token tokenIdentifier) {
        return createExpressionNode(
                IDENTIFIER_PRIMARY_EXPRESSION,
                "identifierPrimaryExpression",
                createExpressionNode(IDENTIFIER_LITERAL, "identifierLiteral", createTokenNode(IDENTIFIER, tokenIdentifier))
        );
    }

    public Node expression(Node expression) {
        return createExpressionNode(EXPRESSION, "expression", expression);
    }

    public Node functionPrimaryExpression(Node expression) {
        return createExpressionNode(FUNCTION_PRIMARY_EXPRESSION, "functionPrimaryExpression", expression);
    }

    public Node binaryExpression(Node expression) {
        return createExpressionNode(BINARY_EXPRESSION, "binaryExpression", expression);
    }

    public Node unaryFunctionPrimaryExpression(Node expression) {
        return createExpressionNode(UNARY_FUNCTION_PRIMARY_EXPRESSION, "unaryFunctionPrimaryExpression", expression);
    }

    public Node arithmeticNegationFunctionExpression(Token token, Node expression) {
        return createExpressionNode(ARITHMETIC_NEGATION_FUNCTION_EXPRESSION, "arithmeticNegationFunctionExpression", expression);
    }

    public Node bitwiseCompletionFunctionExpression(Token token, Node expression) {
        return createExpressionNode(BITWISE_COMPLETION_FUNCTION_EXPRESSION, "bitwiseCompletionFunctionExpression", expression);
    }

    public Node logicalNegationFunctionExpression(Token token, Node expression) {
        return createExpressionNode(LOGICAL_NEGATION_FUNCTION_EXPRESSION, "logicalNegationFunctionExpression", expression);
    }

    public Node vectorDeclarationExpression(Token tokenLeftBracket, Node numberExpressionX, Node numberExpressionY, Node numberExpressionZ, Token tokenRightBracket) {
        return createExpressionNode(VECTOR_DECLARATION_EXPRESSION, "vectorDeclarationExpression",
                createTokenNode(BLOCK_START, tokenLeftBracket),
                numberExpressionX,
                numberExpressionY,
                numberExpressionZ,
                createTokenNode(BLOCK_END, tokenRightBracket)
        );
    }

    public Node groupingExpression(Token tokenLeftBracket, Node expression, Token tokenRightBracket) {
        return createExpressionNode(GROUPING_EXPRESSION, "groupingExpression",
                createTokenNode(BLOCK_START, tokenLeftBracket),
                expression,
                createTokenNode(BLOCK_END, tokenRightBracket)
        );
    }

    public Node constArrayElementSeparator(Token separator) {
        return createTokenNode(DOUBLE_COLON, separator);
    }

    public Node makeArrayExpression(Token tokenMakeArray, Token tokenEol, List<Node> rows, Token tokenEndArray) {
        var nodes = new ArrayList<Node>();
        nodes.add(createTokenNode(KEYWORD, tokenMakeArray));
        nodes.add(createTokenNode(EOL, tokenEol));
        nodes.addAll(rows);
        nodes.add(createTokenNode(KEYWORD, tokenEndArray));
        return createExpressionNode(MAKE_ARRAY_EXPRESSION, "makeArrayExpression", nodes);
    }

    public Node makeArrayRowExpression(List<Node> cols, Token rowEnd) {
        var nodes = new ArrayList<>(cols);
        nodes.add(createTokenNode(EOL, rowEnd));
        return createExpressionNode(MAKE_ARRAY_ROW_EXPRESSION, "makeArrayRowExpression", nodes);
    }

    public Node scalarComponentExpression(Node expression) {
        return createExpressionNode(SCALAR_COMPONENT_EXPRESSION, "scalarComponentExpression", expression);
    }

    public Node identifierScalarComponentExpression(Token tokenIdentifier) {
        return createExpressionNode(IDENTIFIER_SCALAR_COMPONENT_EXPRESSION, "identifierScalarComponentExpression", createTokenNode(IDENTIFIER, tokenIdentifier));
    }

    public Node nonIdentifierScalarComponentExpression(Node expression) {
        return createExpressionNode(NON_IDENTIFIER_SCALAR_COMPONENT_EXPRESSION, "nonIdentifierScalarComponentExpression", expression);
    }

    public Node targetnameExpression(Token tokenDollar, Node primaryExpression) {
        return createExpressionNode(TARGETNAME_EXPRESSION, "targetnameExpression", createTokenNode(PREFIX_OPERATOR, tokenDollar), primaryExpression);
    }

    public Node targetnameScalarComponentExpression(Token tokenDollar, Node expression) {
        return createExpressionNode(TARGETNAME_EXPRESSION, "targetnameScalarComponentExpression", createTokenNode(PREFIX_OPERATOR, tokenDollar), expression);
    }

    public Node memberSelectionExpression(Node lhs, Token tokenPeriod, Token tokenIdenOrSize) {
        return createExpressionNode(MEMBER_SELECTION_EXPRESSION, "memberSelectionExpression", lhs, createTokenNode(INFIX_OPERATOR, tokenPeriod), createTokenNode(IDENTIFIER, tokenIdenOrSize));
    }

    public Node subscriptExpression(Node lhs, Token tokenLeftSqBracket, Node expression, Token tokenRightSqBracket) {
        return createExpressionNode(SUBSCRIPT_EXPRESSION, "subscriptExpression", lhs, createTokenNode(POSTFIX_OPERATOR, tokenLeftSqBracket), expression, createTokenNode(POSTFIX_OPERATOR, tokenRightSqBracket));
    }

    public Node literalExpression(NodeType nodeType, String name, Token token) {
        return createExpressionNode(LITERAL_EXPRESSION, "literalExpression",
                createExpressionNode(nodeType, name, createTokenNode(LITERAL, token)));
    }

    public Node unaryNonIdentifierExpression(boolean prefix, NodeType nodeType, String name, Token token, Node expression) {
        return createExpressionNode(UNARY_NON_IDENTIFIER_EXPRESSION, "unaryNonIdentifierExpression",
                createExpressionNode(nodeType, name, createTokenNode(prefix ? PREFIX_OPERATOR : POSTFIX_OPERATOR, token), expression));
    }

    public Node binarySubexpression(NodeType nodeType, String name, Node lhs, Token token, Node rhs) {
        return createExpressionNode(nodeType, name, lhs, createTokenNode(INFIX_OPERATOR, token), rhs);
    }
}
