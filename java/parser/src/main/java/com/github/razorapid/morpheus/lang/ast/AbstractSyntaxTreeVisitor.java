package com.github.razorapid.morpheus.lang.ast;

public interface AbstractSyntaxTreeVisitor<R> {
    R visitSyntaxError(AbstractSyntaxTree.SyntaxError node);
    R visitStatements(AbstractSyntaxTree.Statements node);
    R visitThreadLabel(AbstractSyntaxTree.ThreadLabel node);
    R visitSwitchCase(AbstractSyntaxTree.SwitchCase node);
    R visitIfElse(AbstractSyntaxTree.IfElse node);
    R visitSwitch(AbstractSyntaxTree.Switch node);
    R visitWhileLoop(AbstractSyntaxTree.WhileLoop node);
    R visitForLoop(AbstractSyntaxTree.ForLoop node);
    R visitTryCatch(AbstractSyntaxTree.TryCatch node);
    R visitNoOperation(AbstractSyntaxTree.NoOperation node);
    R visitBreak(AbstractSyntaxTree.Break node);
    R visitContinue(AbstractSyntaxTree.Continue node);
    R visitExpressionStmt(AbstractSyntaxTree.ExpressionStmt node);
    R visitFunctionCall(AbstractSyntaxTree.FunctionCall node);
    R visitParams(AbstractSyntaxTree.Params node);
    R visitPostfixOp(AbstractSyntaxTree.PostfixOp node);
    R visitPrefixOp(AbstractSyntaxTree.PrefixOp node);
    R visitBinaryOp(AbstractSyntaxTree.BinaryOp node);
    R visitLiteral(AbstractSyntaxTree.Literal node);
    R visitVectorDeclaration(AbstractSyntaxTree.VectorDeclaration node);
    R visitConstArrayDeclaration(AbstractSyntaxTree.ConstArrayDeclaration node);
}
