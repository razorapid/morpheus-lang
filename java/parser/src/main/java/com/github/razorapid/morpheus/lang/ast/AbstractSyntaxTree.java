package com.github.razorapid.morpheus.lang.ast;

import com.github.razorapid.morpheus.lang.SourcePos;
import com.github.razorapid.morpheus.lang.Token;
import lombok.Value;

import java.util.List;

@Value
public class AbstractSyntaxTree {

    Node program;
    public interface Node {
        SourcePos start();
        SourcePos end();

        <T> T accept(AbstractSyntaxTreeVisitor<T> visitor);
    }
    public interface Statement extends Node {}
    public interface Expression extends Node {}

    @Value
    public static class Statements implements Statement {
        SourcePos start;
        SourcePos end;
        List<Statement> statements;

        @Override
        public <T> T accept(AbstractSyntaxTreeVisitor<T> visitor) {
            return visitor.visitStatements(this);
        }
    }

    @Value
    public static class ThreadLabel implements Statement {
        SourcePos start;
        SourcePos end;
        Token identifier;
        Params params;

        @Override
        public <T> T accept(AbstractSyntaxTreeVisitor<T> visitor) {
            return visitor.visitThreadLabel(this);
        }
    }

    @Value
    public static class SwitchCase implements Statement {
        SourcePos start;
        SourcePos end;
        Token identifier;
        Params params;

        @Override
        public <T> T accept(AbstractSyntaxTreeVisitor<T> visitor) {
            return visitor.visitSwitchCase(this);
        }
    }

    @Value
    public static class IfElse implements Statement {
        SourcePos start;
        SourcePos end;
        Expression condition;
        Statement ifClause;
        Statement elseClause;

        @Override
        public <T> T accept(AbstractSyntaxTreeVisitor<T> visitor) {
            return visitor.visitIfElse(this);
        }
    }

    @Value
    public static class Switch implements Statement {
        SourcePos start;
        SourcePos end;
        Expression condition;
        Statement body;

        @Override
        public <T> T accept(AbstractSyntaxTreeVisitor<T> visitor) {
            return visitor.visitSwitch(this);
        }
    }

    @Value
    public static class WhileLoop implements Statement {
        SourcePos start;
        SourcePos end;
        Expression condition;
        Statement body;

        @Override
        public <T> T accept(AbstractSyntaxTreeVisitor<T> visitor) {
            return visitor.visitWhileLoop(this);
        }
    }

    @Value
    public static class ForLoop implements Statement {
        SourcePos start;
        SourcePos end;
        Statement initializer;
        Expression condition;
        Statement advancement;
        Statement body;

        @Override
        public <T> T accept(AbstractSyntaxTreeVisitor<T> visitor) {
            return visitor.visitForLoop(this);
        }
    }

    @Value
    public static class TryCatch implements Statement {
        SourcePos start;
        SourcePos end;
        Statement tryClause;
        Statement catchClause;

        @Override
        public <T> T accept(AbstractSyntaxTreeVisitor<T> visitor) {
            return visitor.visitTryCatch(this);
        }
    }

    @Value
    public static class NoOperation implements Statement {
        SourcePos start;
        SourcePos end;

        @Override
        public <T> T accept(AbstractSyntaxTreeVisitor<T> visitor) {
            return visitor.visitNoOperation(this);
        }
    }

    @Value
    public static class Break implements Statement {
        SourcePos start;
        SourcePos end;

        @Override
        public <T> T accept(AbstractSyntaxTreeVisitor<T> visitor) {
            return visitor.visitBreak(this);
        }
    }

    @Value
    public static class Continue implements Statement {
        SourcePos start;
        SourcePos end;

        @Override
        public <T> T accept(AbstractSyntaxTreeVisitor<T> visitor) {
            return visitor.visitContinue(this);
        }
    }

    @Value
    public static class ExpressionStmt implements Statement {
        SourcePos start;
        SourcePos end;
        Expression expression;

        @Override
        public <T> T accept(AbstractSyntaxTreeVisitor<T> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    @Value
    public static class FunctionCall implements Expression {
        SourcePos start;
        SourcePos end;
        boolean thread;
        Expression listener;
        Token identifier;
        Params params;

        @Override
        public <T> T accept(AbstractSyntaxTreeVisitor<T> visitor) {
            return visitor.visitFunctionCall(this);
        }
    }

    @Value
    public static class Params implements Expression {
        SourcePos start;
        SourcePos end;
        List<Expression> param;

        @Override
        public <T> T accept(AbstractSyntaxTreeVisitor<T> visitor) {
            return visitor.visitParams(this);
        }
    }

    @Value
    public static class PostfixOp implements Expression {
        SourcePos start;
        SourcePos end;
        Expression lhs;
        Token operator;

        @Override
        public <T> T accept(AbstractSyntaxTreeVisitor<T> visitor) {
            return visitor.visitPostfixOp(this);
        }
    }

    @Value
    public static class PrefixOp implements Expression {
        SourcePos start;
        SourcePos end;
        Token operator;
        Expression rhs;

        @Override
        public <T> T accept(AbstractSyntaxTreeVisitor<T> visitor) {
            return visitor.visitPrefixOp(this);
        }
    }

    @Value
    public static class BinaryOp implements Expression {
        SourcePos start;
        SourcePos end;
        Expression lhs;
        Token operator;
        Expression rhs;

        @Override
        public <T> T accept(AbstractSyntaxTreeVisitor<T> visitor) {
            return visitor.visitBinaryOp(this);
        }
    }

    @Value
    public static class Literal implements Expression {
        SourcePos start;
        SourcePos end;
        Token token;

        @Override
        public <T> T accept(AbstractSyntaxTreeVisitor<T> visitor) {
            return visitor.visitLiteral(this);
        }
    }

    @Value
    public static class VectorDeclaration implements Expression {
        SourcePos start;
        SourcePos end;
        Expression x, y, z;

        @Override
        public <T> T accept(AbstractSyntaxTreeVisitor<T> visitor) {
            return visitor.visitVectorDeclaration(this);
        }
    }

    @Value
    public static class ConstArrayDeclaration implements Expression {
        SourcePos start;
        SourcePos end;
        boolean list;
        List<List<Expression>> indices;

        @Override
        public <T> T accept(AbstractSyntaxTreeVisitor<T> visitor) {
            return visitor.visitConstArrayDeclaration(this);
        }
    }
}
