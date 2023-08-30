package com.github.razorapid.morpheus.lang.cst.visitors;

import com.github.razorapid.morpheus.lang.SourcePos;
import com.github.razorapid.morpheus.lang.Token;
import com.github.razorapid.morpheus.lang.ast.AbstractSyntaxTree;
import com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree;
import com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTreeVisitor;

import java.util.List;

import static com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree.NodeType.BLOCK_END;
import static com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree.NodeType.BLOCK_START;
import static com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree.NodeType.DOUBLE_COLON;
import static com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree.NodeType.PREFIX_OPERATOR;
import static com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree.NodeType.SEMICOLON;

public class CstToAstVisitor implements ConcreteSyntaxTreeVisitor<AbstractSyntaxTree.Node> {

    public AbstractSyntaxTree visit(ConcreteSyntaxTree cst) {
        return new AbstractSyntaxTree(visitStatement((ConcreteSyntaxTree.StatementNode) cst.program()));
    }

    @Override
    public AbstractSyntaxTree.Node visitExpression(ConcreteSyntaxTree.ExpressionNode expression) {
        return switch(expression.type()) {
            case THREAD_FUNCTION_CALL_EXPRESSION -> visitFunctionCall(true, expression);
            case LISTENER_FUNCTION_CALL_EXPRESSION -> visitFunctionCall(false, expression);
            case ASSIGNMENT_EXPRESSION,
                    MEMBER_SELECTION_EXPRESSION, SUBSCRIPT_EXPRESSION,
                    MULTIPLICATION_EXPRESSION, DIVISION_EXPRESSION,
                    MODULO_EXPRESSION,
                    ADDITION_EXPRESSION, SUBTRACTION_EXPRESSION,
                    LESS_THAN_EXPRESSION, GREATER_THAN_EXPRESSION,
                    LESS_THAN_OR_EQUALS_EXPRESSION, GREATER_THAN_OR_EQUAL_EXPRESSION,
                    EQUALITY_EXPRESSION, INEQUALITY_EXPRESSION,
                    BITWISE_AND_EXPRESSION, BITWISE_XOR_EXPRESSION, BITWISE_OR_EXPRESSION,
                    LOGICAL_AND_EXPRESSION, LOGICAL_OR_EXPRESSION -> visitBinaryExpression(expression);
            case INCREMENT_EXPRESSION, DECREMENT_EXPRESSION -> visitPostfixExpression(expression);
            case EVENT_PARAMETER_LIST -> visitParams(expression);
            case NON_IDENTIFIER_PRIMARY_EXPRESSION, IDENTIFIER_PRIMARY_EXPRESSION,
                    PRIMARY_EXPRESSION, EXPRESSION,
                    FUNCTION_PRIMARY_EXPRESSION,
                    BINARY_EXPRESSION,
                    SCALAR_COMPONENT_EXPRESSION, IDENTIFIER_SCALAR_COMPONENT_EXPRESSION, NON_IDENTIFIER_SCALAR_COMPONENT_EXPRESSION,
                    LITERAL_EXPRESSION -> skipToChild(expression);
            case UNARY_FUNCTION_PRIMARY_EXPRESSION, UNARY_NON_IDENTIFIER_EXPRESSION,
                    ARITHMETIC_NEGATION_FUNCTION_EXPRESSION,
                    BITWISE_COMPLETION_FUNCTION_EXPRESSION,
                    LOGICAL_NEGATION_FUNCTION_EXPRESSION,
                    TARGETNAME_EXPRESSION,
                    ARITHMETIC_NEGATION_NON_IDENTIFIER_EXPRESSION,
                    BITWISE_COMPLEMENT_NON_IDENTIFIER_EXPRESSION,
                    LOGICAL_NEGATION_NON_IDENTIFIER_EXPRESSION -> visitPrefixExpression(expression);
            case VECTOR_DECLARATION_EXPRESSION -> visitVectorDeclaration(expression);
            case GROUPING_EXPRESSION -> visitGroupingExpression(expression);
            case CONST_ARRAY_EXPRESSION -> visitConstArrayDeclaration(true, expression);
            case MAKE_ARRAY_EXPRESSION -> visitConstArrayDeclaration(false, expression);
            case IDENTIFIER_LITERAL, STRING_LITERAL,
                    INTEGER_LITERAL, FLOAT_LITERAL,
                    NULL_LITERAL, NIL_LITERAL,
                    LISTENER_LITERAL -> visitLiteral(expression);
            default -> throw new IllegalStateException("Unknown expression type: " + expression.type());
        };
    }

    private List<AbstractSyntaxTree.Expression> visitConstArrayRowDeclaration(ConcreteSyntaxTree.ExpressionNode expression) {
        return expression.children().stream()
                .limit(expression.children().size() - 1)
                .map(node -> node.accept(this))
                .map(AbstractSyntaxTree.Expression.class::cast)
                .toList();
    }

    private AbstractSyntaxTree.Node visitGroupingExpression(ConcreteSyntaxTree.ExpressionNode expression) {
        return expression.children().get(1).accept(this);
    }

    private AbstractSyntaxTree.Node visitVectorDeclaration(ConcreteSyntaxTree.ExpressionNode expression) {
        var scalarComponents = expression.children().stream()
                .filter(node -> node.type() != BLOCK_START && node.type() != BLOCK_END)
                .map(node -> node.accept(this))
                .map(AbstractSyntaxTree.Expression.class::cast)
                .toList();
        var declStart = ((ConcreteSyntaxTree.TokenNode) expression.children().get(0)).value();
        var declEnd = ((ConcreteSyntaxTree.TokenNode) expression.children().get(expression.children().size() - 1)).value();
        var start = declStart.pos();
        var end = declEnd.pos().addCol(declEnd.lexeme().length());

        return new AbstractSyntaxTree.VectorDeclaration(start, end, scalarComponents.get(0), scalarComponents.get(1), scalarComponents.get(2));
    }

    private AbstractSyntaxTree.Node visitLiteral(ConcreteSyntaxTree.ExpressionNode expression) {
        var literalNode = (ConcreteSyntaxTree.TokenNode) expression.children().get(0);
        var literal = literalNode.value();
        var start = literal.pos();
        var end = literal.pos().addCol(literal.lexeme().length());

        return new AbstractSyntaxTree.Literal(start, end, literal);
    }

    private AbstractSyntaxTree.Node visitConstArrayDeclaration(boolean list, ConcreteSyntaxTree.ExpressionNode expression) {
        if (list) {
            var indices = expression.children().stream()
                    .filter(node -> node.type() != DOUBLE_COLON)
                    .map(node -> node.accept(this)).map(AbstractSyntaxTree.Expression.class::cast).toList();
            var start = indices.get(0).start();
            var end = indices.get(indices.size() - 1).end();

            return new AbstractSyntaxTree.ConstArrayDeclaration(start, end, list, List.of(indices));
        }

        var indices = expression.children().stream()
                .filter(node -> !(node instanceof ConcreteSyntaxTree.TokenNode))
                .map(ConcreteSyntaxTree.ExpressionNode.class::cast)
                .map(this::visitConstArrayRowDeclaration)
                .toList();

        var makeArrayKeyword = ((ConcreteSyntaxTree.TokenNode)expression.children().get(0)).value();
        var endArrayKeyword = ((ConcreteSyntaxTree.TokenNode)expression.children().get(expression.children().size() - 1)).value();

        var start = makeArrayKeyword.pos();
        var end = endArrayKeyword.pos().addCol(endArrayKeyword.lexeme().length());

        return new AbstractSyntaxTree.ConstArrayDeclaration(start, end, list, indices);
    }

    private AbstractSyntaxTree.Node skipToChild(ConcreteSyntaxTree.ExpressionNode expression) {
        return expression.children().get(0).accept(this);
    }

    private AbstractSyntaxTree.Node visitPrefixExpression(ConcreteSyntaxTree.ExpressionNode expression) {
        var operatorNode = (ConcreteSyntaxTree.TokenNode) expression.children().get(0);
        var operator = operatorNode.value();
        var operand = (AbstractSyntaxTree.Expression) expression.children().get(1).accept(this);
        var start = operator.pos();
        var end = operand.end();

        return new AbstractSyntaxTree.PrefixOp(start, end, operator, operand);
    }

    private AbstractSyntaxTree.Node visitParams(ConcreteSyntaxTree.ExpressionNode expression) {
        var params = expression.children().stream().map(node -> node.accept(this))
                .map(AbstractSyntaxTree.Expression.class::cast)
                .toList();

        SourcePos start = null, end = null;
        if (!params.isEmpty()) {
            start = params.get(0).start();
            end = params.get(params.size() - 1).end();
        }

        return new AbstractSyntaxTree.Params(start, end, params);
    }

    private AbstractSyntaxTree.Node visitPostfixExpression(ConcreteSyntaxTree.ExpressionNode expression) {
        var operand = (AbstractSyntaxTree.Expression) expression.children().get(0).accept(this);
        var operatorNode = (ConcreteSyntaxTree.TokenNode) expression.children().get(1);
        var operator = operatorNode.value();
        var start = operand.start();
        var end = operator.pos().addCol(operator.lexeme().length());

        return new AbstractSyntaxTree.PostfixOp(start, end, operand, operator);
    }

    private AbstractSyntaxTree.Node visitBinaryExpression(ConcreteSyntaxTree.ExpressionNode expression) {
        var lhs = (AbstractSyntaxTree.Expression) expression.children().get(0).accept(this);
        var operatorNode = (ConcreteSyntaxTree.TokenNode) expression.children().get(1);
        var operator = operatorNode.value();
        var rhs = (AbstractSyntaxTree.Expression) expression.children().get(2).accept(this);
        var start = lhs.start();
        var end = rhs.end();

        /* Support for subscript expression and the likes */
        if (expression.children().size() > 3) {
            var closingToken = ((ConcreteSyntaxTree.TokenNode) expression.children().get(3)).value();
            end = closingToken.pos().addCol(closingToken.lexeme().length());
        }

        return new AbstractSyntaxTree.BinaryOp(start, end, lhs, operator, rhs);
    }

    private AbstractSyntaxTree.Node visitFunctionCall(boolean thread, ConcreteSyntaxTree.ExpressionNode expression) {
        if (thread) {
            var identifierNode = (ConcreteSyntaxTree.TokenNode) expression.children().get(0);
            var identifier = identifierNode.value();
            var params = (AbstractSyntaxTree.Params) expression.children().get(1).accept(this);
            var start = identifier.pos();
            var end = params.end() != null ? params.end() : identifier.pos().addCol(identifier.lexeme().length());

            return new AbstractSyntaxTree.FunctionCall(start, end, thread, null, identifier, params);
        }

        var listener = (AbstractSyntaxTree.Expression) expression.children().get(0).accept(this);
        var identifierNode = (ConcreteSyntaxTree.TokenNode) expression.children().get(1);
        var identifier = identifierNode.value();
        var params = (AbstractSyntaxTree.Params) expression.children().get(2).accept(this);
        var start = listener.start();
        var end = params.end() != null ? params.end() : identifier.pos().addCol(identifier.lexeme().length());

        return new AbstractSyntaxTree.FunctionCall(start, end, thread, listener, identifier, params);
    }

    @Override
    public AbstractSyntaxTree.Node visitStatement(ConcreteSyntaxTree.StatementNode statement) {
        return switch (statement.type()) {
            case STATEMENT_LIST -> visitStatements(statement);
            case STATEMENT_LINE, STATEMENT,
                    LABEL_STATEMENT, SELECTION_STATEMENT,
                    ITERATION_STATEMENT -> skipToChild(statement);
            case COMPOUND_STATEMENT -> visitCompoundStatement(statement);
            case TRY_CATCH_STATEMENT -> visitTryCatchStatement(statement);
            case BREAK_STATEMENT -> visitBreakStatement(statement);
            case CONTINUE_STATEMENT -> visitContinueStatement(statement);
            case NOOP_STATEMENT -> visitNoopStatement(statement);
            case EXPRESSION_STATEMENT -> visitExpressionStatement(statement);
            case SWITCH_CASE_LABEL_STATEMENT -> visitSwitchCaseStatement(statement);
            case THREAD_LABEL_STATEMENT -> visitThreadLabelStatement(statement);
            case IF_ELSE_STATEMENT -> visitIfElseStatement(statement);
            case SWITCH_STATEMENT -> visitSwitchStatement(statement);
            case WHILE_STATEMENT -> visitWhileStatement(statement);
            case FOR_STATEMENT -> visitForStatement(statement);
            default -> throw new IllegalStateException("Unknown statement type: " + statement.type());
        };
    }

    private AbstractSyntaxTree.Node visitForStatement(ConcreteSyntaxTree.StatementNode statement) {
        var initializer = (AbstractSyntaxTree.Statement) statement.children().get(2) != null ?
                (AbstractSyntaxTree.Statement) statement.children().get(2).accept(this) :
                null;
        var condition = (AbstractSyntaxTree.Expression) statement.children().get(4).accept(this);
        var advancement = (AbstractSyntaxTree.Statement) statement.children().get(6).accept(this);
        var body = (AbstractSyntaxTree.Statement) statement.children().get(8).accept(this);
        var start = ((ConcreteSyntaxTree.TokenNode) statement.children().get(0)).value().pos();
        var end = body.end();

        return new AbstractSyntaxTree.ForLoop(start, end, initializer, condition, advancement, body);
    }

    private AbstractSyntaxTree.Node visitWhileStatement(ConcreteSyntaxTree.StatementNode statement) {
        var condition = (AbstractSyntaxTree.Expression) statement.children().get(1).accept(this);
        var body = (AbstractSyntaxTree.Statement) statement.children().get(2).accept(this);
        var start = ((ConcreteSyntaxTree.TokenNode) statement.children().get(0)).value().pos();
        var end = body.end();

        return new AbstractSyntaxTree.WhileLoop(start, end, condition, body);
    }

    private AbstractSyntaxTree.Node visitSwitchStatement(ConcreteSyntaxTree.StatementNode statement) {
        var condition = (AbstractSyntaxTree.Expression) statement.children().get(1).accept(this);
        var body = (AbstractSyntaxTree.Statement) statement.children().get(2).accept(this);
        var start = ((ConcreteSyntaxTree.TokenNode) statement.children().get(0)).value().pos();
        var end = body.end();

        return new AbstractSyntaxTree.Switch(start, end, condition, body);
    }

    private AbstractSyntaxTree.Node visitIfElseStatement(ConcreteSyntaxTree.StatementNode statement) {
        var condition = (AbstractSyntaxTree.Expression) statement.children().get(1).accept(this);
        var ifClause = (AbstractSyntaxTree.Statement) statement.children().get(2).accept(this);
        AbstractSyntaxTree.Statement elseClause = null;
        if (statement.children().size() > 3) {
            if (statement.children().get(3).type() != SEMICOLON || statement.children().size() > 4) {
                var elseClauseIdx = statement.children().get(3).type() != SEMICOLON ? 4 : 5;
                elseClause = (AbstractSyntaxTree.Statement) statement.children().get(elseClauseIdx).accept(this);
            }
        }
        var start = ((ConcreteSyntaxTree.TokenNode) statement.children().get(0)).value().pos();
        var end = elseClause != null ? elseClause.end() : ifClause.end();

        return new AbstractSyntaxTree.IfElse(start, end, condition, ifClause, elseClause);
    }

    private AbstractSyntaxTree.Node visitSwitchCaseStatement(ConcreteSyntaxTree.StatementNode statement) {
        Token operator = null;
        Token identifier = null;
        var paramsIdx = 2;
        var identifierNode = (ConcreteSyntaxTree.TokenNode) statement.children().get(1);
        if (identifierNode.type() == PREFIX_OPERATOR) {
            operator = identifierNode.value();
            identifier = ((ConcreteSyntaxTree.TokenNode) statement.children().get(2)).value();
            paramsIdx = 3;
        } else {
            identifier = identifierNode.value();
        }
        var params = (AbstractSyntaxTree.Params) visitParams((ConcreteSyntaxTree.ExpressionNode) statement.children().get(paramsIdx));
        var colon = ((ConcreteSyntaxTree.TokenNode) statement.children().get(statement.children().size() - 1)).value();
        var start = ((ConcreteSyntaxTree.TokenNode) statement.children().get(0)).value().pos();
        var end = colon.pos().addCol(colon.lexeme().length());

        return new AbstractSyntaxTree.SwitchCase(start, end, operator, identifier, params);
    }

    private AbstractSyntaxTree.Node visitExpressionStatement(ConcreteSyntaxTree.StatementNode statement) {
        var expression = (AbstractSyntaxTree.Expression) statement.children().get(0).accept(this);

        return new AbstractSyntaxTree.ExpressionStmt(expression.start(), expression.end(), expression);
    }

    private AbstractSyntaxTree.Node visitNoopStatement(ConcreteSyntaxTree.StatementNode statement) {
        var token = ((ConcreteSyntaxTree.TokenNode) statement.children().get(0)).value();
        var start = token.pos();
        var end = token.pos().addCol(token.lexeme().length());

        return new AbstractSyntaxTree.NoOperation(start, end);
    }

    private AbstractSyntaxTree.Node visitContinueStatement(ConcreteSyntaxTree.StatementNode statement) {
        var token = ((ConcreteSyntaxTree.TokenNode) statement.children().get(0)).value();
        var start = token.pos();
        var end = token.pos().addCol(token.lexeme().length());

        return new AbstractSyntaxTree.Continue(start, end);
    }

    private AbstractSyntaxTree.Node visitBreakStatement(ConcreteSyntaxTree.StatementNode statement) {
        var token = ((ConcreteSyntaxTree.TokenNode) statement.children().get(0)).value();
        var start = token.pos();
        var end = token.pos().addCol(token.lexeme().length());

        return new AbstractSyntaxTree.Break(start, end);
    }

    private AbstractSyntaxTree.Node visitTryCatchStatement(ConcreteSyntaxTree.StatementNode statement) {
        var tryClause = (AbstractSyntaxTree.Statement) statement.children().get(1).accept(this);
        var catchClause = (AbstractSyntaxTree.Statement) statement.children().get(3).accept(this);
        var start = ((ConcreteSyntaxTree.TokenNode) statement.children().get(0)).value().pos();
        var end = catchClause.end();

        return new AbstractSyntaxTree.TryCatch(start, end, tryClause, catchClause);
    }

    private AbstractSyntaxTree.Node visitThreadLabelStatement(ConcreteSyntaxTree.StatementNode statement) {
        var identifierNode = (ConcreteSyntaxTree.TokenNode) statement.children().get(0);
        var identifier = identifierNode.value();
        var params = (AbstractSyntaxTree.Params) visitParams((ConcreteSyntaxTree.ExpressionNode) statement.children().get(1));
        var colon = ((ConcreteSyntaxTree.TokenNode) statement.children().get(statement.children().size() - 1)).value();
        var start = identifier.pos();
        var end = colon.pos().addCol(colon.lexeme().length());

        return new AbstractSyntaxTree.ThreadLabel(start, end, identifier, params);
    }

    private AbstractSyntaxTree.Node visitCompoundStatement(ConcreteSyntaxTree.StatementNode statement) {
        var blockStart = ((ConcreteSyntaxTree.TokenNode)statement.children().get(0)).value();
        var blockEnd = ((ConcreteSyntaxTree.TokenNode)statement.children().get(statement.children().size() - 1)).value();
        var start = blockStart.pos();
        var end = blockEnd.pos().addCol(blockEnd.lexeme().length());
        var statements = statement.children().stream().sequential()
            .filter(node -> !(node instanceof ConcreteSyntaxTree.TokenNode))
            .map(node -> node.accept(this))
            .map(AbstractSyntaxTree.Statement.class::cast)
            .toList();

        return new AbstractSyntaxTree.Statements(start, end, statements);
    }

    private AbstractSyntaxTree.Node skipToChild(ConcreteSyntaxTree.StatementNode statement) {
        return statement.children().get(0).accept(this);
    }

    private AbstractSyntaxTree.Node visitStatements(ConcreteSyntaxTree.StatementNode statement) {
        var statements = statement.children().stream().sequential()
            .map(node -> node.accept(this))
            .map(AbstractSyntaxTree.Statement.class::cast)
            .toList();

        SourcePos start = null, end = null;
        if (!statements.isEmpty()) {
            start = statements.get(0).start();
            end = statements.get(statements.size() - 1).end();
        }
        return new AbstractSyntaxTree.Statements(start, end, statements);
    }

    @Override
    public AbstractSyntaxTree.Node visitToken(ConcreteSyntaxTree.TokenNode token) {
        return new AbstractSyntaxTree.Literal(token.value().pos(), token.value().pos().addCol(token.value().lexeme().length()), token.value());
    }
}
