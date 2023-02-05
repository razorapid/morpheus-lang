package com.github.razorapid.morpheus.lang.cst.visitors;

import com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree;
import com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTreeVisitor;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Objects;
import java.util.UUID;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;

public class VisualGraphVisitor implements ConcreteSyntaxTreeVisitor<Node> {

    public Graph visit(ConcreteSyntaxTree cst) {
        return graph("program").directed().with(
                visitStatement((ConcreteSyntaxTree.StatementNode) cst.program())
        );
    }

    @Override
    public Node visitExpression(ConcreteSyntaxTree.ExpressionNode expression) {
        return graphExpression(expression);
    }

    @Override
    public Node visitStatement(ConcreteSyntaxTree.StatementNode statement) {
        return graphStatement(statement);
    }

    @Override
    public Node visitToken(ConcreteSyntaxTree.TokenNode token) {
        return graphToken(token);
    }

    private Node graphStatement(ConcreteSyntaxTree.StatementNode statement) {
        var result = node(UUID.randomUUID().toString())
                .with(Shape.RECTANGLE)
                .with(Label.of(statement.name()));
        if (!statement.children().isEmpty()) {
            result = result.link(
                    statement.children().stream().sequential().filter(Objects::nonNull).map(it -> it.accept(this)).toList()
            );
        }
        return result;
    }

    private Node graphExpression(ConcreteSyntaxTree.ExpressionNode expression) {
        var result = node(UUID.randomUUID().toString())
                .with(Shape.INV_HOUSE)
                .with(Label.of(expression.name()));
        if (!expression.children().isEmpty()) {
            result = result.link(
                    expression.children().stream().sequential().filter(Objects::nonNull).map(it -> it.accept(this)).toList()
            );
        }
        return result;
    }

    private Node graphToken(ConcreteSyntaxTree.TokenNode token) {
        var lexeme = escape(token.value().lexeme());
        var uid = UUID.randomUUID().toString();
        return node(uid)
                .with(Label.htmlLines(
                        escapeTokenName(token.value().type().name()),
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
