package com.github.razorapid.morpheus.lang.cst;

public interface ConcreteSyntaxTreeVisitor<R> {
    R visitExpression(ConcreteSyntaxTree.ExpressionNode expression);
    R visitStatement(ConcreteSyntaxTree.StatementNode statement);
    R visitToken(ConcreteSyntaxTree.TokenNode token);
    R visitError(ConcreteSyntaxTree.ErrorNode error);
}
