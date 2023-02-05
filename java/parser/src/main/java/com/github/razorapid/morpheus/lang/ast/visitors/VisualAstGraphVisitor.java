package com.github.razorapid.morpheus.lang.ast.visitors;

import com.github.razorapid.morpheus.lang.Token;
import com.github.razorapid.morpheus.lang.ast.AbstractSyntaxTree;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;
import com.github.razorapid.morpheus.lang.ast.AbstractSyntaxTreeVisitor;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;

public class VisualAstGraphVisitor implements AbstractSyntaxTreeVisitor<Node> {

    public Graph visit(AbstractSyntaxTree ast) {
        return graph("program").directed().with(
                visitStatements((AbstractSyntaxTree.Statements) ast.program())
        );
    }

    @Override
    public Node visitStatements(AbstractSyntaxTree.Statements node) {
        return graphStatementNode(node)
                .link(
                    node.statements().stream()
                            .sequential()
                            .filter(Objects::nonNull)
                            .map(it -> it.accept(this))
                            .toList()
                );
    }

    @Override
    public Node visitThreadLabel(AbstractSyntaxTree.ThreadLabel node) {
        return graphStatementNode(node)
                .link(
                        graphTokenNode(node.identifier()),
                        node.params().accept(this)
                );
    }

    @Override
    public Node visitSwitchCase(AbstractSyntaxTree.SwitchCase node) {
        return graphStatementNode(node)
                .link(
                        graphTokenNode(node.identifier()),
                        node.params().accept(this)
                );
    }

    @Override
    public Node visitIfElse(AbstractSyntaxTree.IfElse node) {
        if (node.elseClause() == null) {
            return graphStatementNode(node)
                    .link(
                            node.condition().accept(this),
                            node.ifClause().accept(this)
                    );
        }
        return graphStatementNode(node)
                .link(
                        node.condition().accept(this),
                        node.ifClause().accept(this),
                        node.elseClause().accept(this)
                );
    }

    @Override
    public Node visitSwitch(AbstractSyntaxTree.Switch node) {
        return graphStatementNode(node)
                .link(
                        node.condition().accept(this),
                        node.body().accept(this)
                );
    }

    @Override
    public Node visitWhileLoop(AbstractSyntaxTree.WhileLoop node) {
        return graphStatementNode(node)
                .link(
                        node.condition().accept(this),
                        node.body().accept(this)
                );
    }

    @Override
    public Node visitForLoop(AbstractSyntaxTree.ForLoop node) {
        return graphStatementNode(node)
                .link(
                        node.initializer().accept(this),
                        node.condition().accept(this),
                        node.advancement().accept(this),
                        node.body().accept(this)
                );
    }

    @Override
    public Node visitTryCatch(AbstractSyntaxTree.TryCatch node) {
        return graphStatementNode(node)
                .link(
                        node.tryClause().accept(this),
                        node.catchClause().accept(this)
                );
    }

    @Override
    public Node visitNoOperation(AbstractSyntaxTree.NoOperation node) {
        return graphStatementNode(node);
    }

    @Override
    public Node visitBreak(AbstractSyntaxTree.Break node) {
        return graphStatementNode(node);
    }

    @Override
    public Node visitContinue(AbstractSyntaxTree.Continue node) {
        return graphStatementNode(node);
    }

    @Override
    public Node visitExpressionStmt(AbstractSyntaxTree.ExpressionStmt node) {
        return graphStatementNode(node)
                .link(
                        node.expression().accept(this)
                );
    }

    @Override
    public Node visitFunctionCall(AbstractSyntaxTree.FunctionCall node) {
        if (node.listener() != null) {
            return graphExpressionNode(node)
                    .link(
                            node.listener().accept(this),
                            graphTokenNode(node.identifier()),
                            node.params().accept(this)
                    );
        }
        return graphExpressionNode(node)
                .link(
                        graphTokenNode(node.identifier()),
                        node.params().accept(this)
                );
    }

    @Override
    public Node visitParams(AbstractSyntaxTree.Params node) {
        return graphExpressionNode(node)
                .link(
                        node.param().stream().sequential()
                                .filter(Objects::nonNull)
                                .map(it -> it.accept(this))
                                .toList()
                );
    }

    @Override
    public Node visitPostfixOp(AbstractSyntaxTree.PostfixOp node) {
        return graphExpressionNode(node)
                .link(
                        node.lhs().accept(this),
                        graphTokenNode(node.operator())
                );
    }

    @Override
    public Node visitPrefixOp(AbstractSyntaxTree.PrefixOp node) {
        return graphExpressionNode(node)
                .link(
                        graphTokenNode(node.operator()),
                        node.rhs().accept(this)
                );
    }

    @Override
    public Node visitBinaryOp(AbstractSyntaxTree.BinaryOp node) {
        return graphExpressionNode(node)
                .link(
                        node.lhs().accept(this),
                        graphTokenNode(node.operator()),
                        node.rhs().accept(this)
                );
    }

    @Override
    public Node visitLiteral(AbstractSyntaxTree.Literal node) {
        return graphExpressionNode(node)
                .link(
                        graphTokenNode(node.token())
                );
    }

    @Override
    public Node visitVectorDeclaration(AbstractSyntaxTree.VectorDeclaration node) {
        return graphExpressionNode(node)
                .link(
                        node.x().accept(this),
                        node.y().accept(this),
                        node.z().accept(this)
                );
    }

    @Override
    public Node visitConstArrayDeclaration(AbstractSyntaxTree.ConstArrayDeclaration node) {
        return graphExpressionNode(node)
                .link(
                        node.indices().stream().sequential()
                                .flatMap(List::stream)
                                .map(indice -> indice.accept(this))
                                .toList()
                );
    }

    private Node graphStatementNode(AbstractSyntaxTree.Statement statement) {
        return node(UUID.randomUUID().toString())
                .with(Shape.RECTANGLE)
                .with(Label.of(statement.getClass().getSimpleName() + " [start: " + statement.start() + ", end: " + statement.end() + "]"));
    }

    private Node graphExpressionNode(AbstractSyntaxTree.Expression expression) {
        return node(UUID.randomUUID().toString())
                .with(Shape.INV_HOUSE)
                .with(Label.of(expression.getClass().getSimpleName() + " [start: " + expression.start() + ", end: " + expression.end() + "]"));
    }

    private Node graphTokenNode(Token token) {
        var lexeme = escape(token.lexeme());
        var uid = UUID.randomUUID().toString();
        return node(uid)
                .with(Label.htmlLines(
                        escapeTokenName(token.type().name()) + " [start: " + token.pos() + ", end: " + token.pos().addCol(token.lexeme().length()) + "]",
                        "<b>" + lexeme + "</b>"
                ));
    }

    private String escape(String s){
        return StringEscapeUtils.escapeHtml4(
                StringEscapeUtils.escapeJava(s).replace("\\\"", "\"")
        ).replace("]", "&#93;");
    }

    private String escapeTokenName(String s){
        return s; //s.replace("_", "\\_");
    }
}
