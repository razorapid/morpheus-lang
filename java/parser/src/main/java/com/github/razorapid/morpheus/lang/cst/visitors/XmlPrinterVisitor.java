package com.github.razorapid.morpheus.lang.cst.visitors;

import com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree;
import com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTreeVisitor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Objects;

@RequiredArgsConstructor
public class XmlPrinterVisitor implements ConcreteSyntaxTreeVisitor<String> {
    private final boolean includeLineAndCol;

    public String visit(ConcreteSyntaxTree cst) {
        return visitStatement((ConcreteSyntaxTree.StatementNode) cst.program());
    }

    @Override
    public String visitExpression(ConcreteSyntaxTree.ExpressionNode expression) {
        return xmlExpression(expression);
    }

    @Override
    public String visitStatement(ConcreteSyntaxTree.StatementNode statement) {
        return xmlStatement(statement);
    }

    @Override
    public String visitToken(ConcreteSyntaxTree.TokenNode token) {
        return xmlToken(token);
    }

    @Override
    public String visitError(ConcreteSyntaxTree.ErrorNode error) {
            return xmlError(error);
    }

    private String xmlStatement(ConcreteSyntaxTree.StatementNode statement) {
        if (statement.children().isEmpty()) {
            return "<Statement name=\"" + statement.name() + "\"/>";
        }
        return "<Statement name=\"" + statement.name() + "\">" +
                String.join("", statement.children().stream().sequential().filter(Objects::nonNull).map(it -> it.accept(this)).toList()) +
                "</Statement>";
    }

    private String xmlExpression(ConcreteSyntaxTree.ExpressionNode expression) {
        if (expression.children().isEmpty()) {
            return "<Expression name=\"" + expression.name() + "\"/>";
        }
        return "<Expression name=\"" + expression.name() + "\">" +
                String.join("", expression.children().stream().sequential().filter(Objects::nonNull).map(it -> it.accept(this)).toList()) +
                "</Expression>";
    }

    private String xmlError(ConcreteSyntaxTree.ErrorNode error) {
        if (error == null) return "";
        var se = error.error();
        var result =  "<Error";
        if (includeLineAndCol) {
            result += " col=\"" +
                se.pos().col() +
                "\" line=\"" +
                se.pos().line()
                + "\"";
        }
        result += " type=\"" +
            error.type() +
            "\" value=\"" +
            escape(se.error()) +
            "\">";

        result += String.join("", error.children().stream().sequential().filter(Objects::nonNull).map(it -> it.accept(this)).toList());
        result += "</Error>";
        return result;
    }

    private String xmlToken(ConcreteSyntaxTree.TokenNode token) {
        if (token == null) return "";
        var result =  "<Token";
        if (includeLineAndCol) {
            result += " col=\"" +
                    token.value().col() +
                    "\" line=\"" +
                    token.value().line()
            + "\"";
        }
        result += " type=\"" +
                token.value().type() +
                "\" value=\"" +
                escape(token.value().lexeme()) +
                "\"/>";
        return result;
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
