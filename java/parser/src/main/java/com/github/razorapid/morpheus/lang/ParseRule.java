package com.github.razorapid.morpheus.lang;

import com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTreeBuilder;
import com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Data
@RequiredArgsConstructor(access = PRIVATE)
class ParseRule {
    private static final ConcreteSyntaxTreeBuilder nodes = new ConcreteSyntaxTreeBuilder();
    private final ParseFn prefix;
    private final ParseFn infix;
    private final Operator precedence;

    static ParseRule prefixRule(ParseFn fn) {
        return new ParseRule(fn, null, null);
    }

    static ParseRule infixRule(ParseFn fn, Operator precedence) {
        return new ParseRule(null, fn, precedence);
    }

    interface ParseFn {
        default ConcreteSyntaxTree.Node parse(ParseRule rule, Parser parser) {
            return parse(null, rule, parser);
        }
        ConcreteSyntaxTree.Node parse(ConcreteSyntaxTree.Node lhs, ParseRule rule, Parser parser);

        static ParseRule.ParseFn literalExpression(ConcreteSyntaxTree.NodeType nodeType, String name, TokenType type) {
            return (ConcreteSyntaxTree.Node lhs, ParseRule rule, Parser parser) -> {
                var token = parser.consume(type);
                if (!parser.isMatched(token)) {
                    return null;
                }
                return nodes.literalExpression(nodeType, name, token);
            };
        }

        static ParseRule.ParseFn unaryNonIdentifier(ConcreteSyntaxTree.NodeType nodeType, String name, TokenType operator) {
            return (ConcreteSyntaxTree.Node lhs, ParseRule rule, Parser parser) -> {
                var token = parser.consume(operator);
                if (!parser.isMatched(token)) {
                    return null;
                }
                var expression = parser.parseNonIdentifierPrimaryExpression();
                if (!parser.isMatched(expression)) {
                    parser.error("bad token - got " + parser.lastToken().type().name() + " expected non identify primary expression");
                    return null;
                }
                return nodes.unaryNonIdentifierExpression(rule.prefix != null, nodeType, name, token, expression);
            };
        }

        static ParseRule.ParseFn binary(ConcreteSyntaxTree.NodeType nodeType, String name, TokenType operator) {
            return (ConcreteSyntaxTree.Node lhs, ParseRule rule, Parser parser) -> {
                var token = parser.consume(operator);
                if (!parser.isMatched(token)) {
                    return null;
                }

                parser.consumeNewLines();

                var expression = parser.parseExpression(rule.precedence());
                if (!parser.isMatched(expression)) {
                    parser.error("bad token - got " + parser.lastToken().type().name() + " expected expression");
                    return null;
                }
                return nodes.binarySubexpression(nodeType, name, lhs, token, expression);
            };
        }
    }
}
