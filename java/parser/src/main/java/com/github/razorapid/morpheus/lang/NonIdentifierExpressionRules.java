package com.github.razorapid.morpheus.lang;

import com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree;
import com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTreeBuilder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

import static com.github.razorapid.morpheus.lang.Operator.INDEX;
import static com.github.razorapid.morpheus.lang.Operator.NONE;
import static com.github.razorapid.morpheus.lang.Operator.PROPERTY_COMMAND;
import static com.github.razorapid.morpheus.lang.ParseRule.ParseFn.literalExpression;
import static com.github.razorapid.morpheus.lang.ParseRule.ParseFn.unaryNonIdentifier;
import static com.github.razorapid.morpheus.lang.ParseRule.infixRule;
import static com.github.razorapid.morpheus.lang.ParseRule.prefixRule;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_BREAK;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_CASE;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_CATCH;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_COMPLEMENT;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_CONTINUE;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_DOLLAR;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_ELSE;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_END;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_FLOAT;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_FOR;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_IDENTIFIER;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_IF;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_INTEGER;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_LEFT_BRACKET;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_LEFT_SQUARE_BRACKET;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_LISTENER;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_NEG;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_NIL;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_NOT;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_NULL;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_PERIOD;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_RIGHT_BRACKET;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_RIGHT_SQUARE_BRACKET;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_SIZE;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_STRING;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_SWITCH;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_TRY;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_WHILE;
import static com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree.NodeType.ARITHMETIC_NEGATION_NON_IDENTIFIER_EXPRESSION;
import static com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree.NodeType.BITWISE_COMPLEMENT_NON_IDENTIFIER_EXPRESSION;
import static com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree.NodeType.FLOAT_LITERAL;
import static com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree.NodeType.INTEGER_LITERAL;
import static com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree.NodeType.LISTENER_LITERAL;
import static com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree.NodeType.LOGICAL_NEGATION_NON_IDENTIFIER_EXPRESSION;
import static com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree.NodeType.NIL_LITERAL;
import static com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree.NodeType.NULL_LITERAL;
import static com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree.NodeType.STRING_LITERAL;

class NonIdentifierExpressionRules {
    private static final ConcreteSyntaxTreeBuilder nodes = new ConcreteSyntaxTreeBuilder();

    private final Parser parser;
    @Getter
    private final Map<TokenType, ParseRule> nonIdentifierPrimaryExpressionRules;
    private final Map<TokenType, ParseRule> nonIdentifierScalarComponentExpressionRules;

    NonIdentifierExpressionRules(Parser parser) {
        this.parser = parser;
        this.nonIdentifierPrimaryExpressionRules = createNonIdentifierPrimaryExpressionRules();
        this.nonIdentifierScalarComponentExpressionRules = createNonIdentifierScalarComponentExpressionRules();
    }

    private Map<TokenType, ParseRule> createNonIdentifierScalarComponentExpressionRules() {
        var rules = createNonIdentifierPrimaryExpressionRules();
        rules.put(TOKEN_DOLLAR, prefixRule(TARGETNAME_SCALAR_COMPONENT_EXPRESSION_FN));
        return rules;
    }

    private Map<TokenType, ParseRule> createNonIdentifierPrimaryExpressionRules() {
        var rules = new HashMap<TokenType, ParseRule>();
        rules.put(TOKEN_LEFT_BRACKET, prefixRule(VECTOR_OR_GROUP_EXPRESSION_FN));
        rules.put(TOKEN_LEFT_SQUARE_BRACKET, infixRule(SUBSCRIPT_EXPRESSION_FN, INDEX));
        rules.put(TOKEN_DOLLAR, prefixRule(TARGETNAME_EXPRESSION_FN));
        rules.put(TOKEN_PERIOD, infixRule(MEMBER_SELECTION_EXPRESSION_FN, PROPERTY_COMMAND));
        rules.put(TOKEN_NEG, prefixRule(unaryNonIdentifier(ARITHMETIC_NEGATION_NON_IDENTIFIER_EXPRESSION, "arithmeticNegationNonIdentifierExpression", TOKEN_NEG)));
        //nonIdentifyPrimaryExpressionRules.put(TOKEN_POS <-- the token exists in original parser, but looks like it's not used anywhere
        rules.put(TOKEN_COMPLEMENT, prefixRule(unaryNonIdentifier(BITWISE_COMPLEMENT_NON_IDENTIFIER_EXPRESSION, "bitwiseComplementNonIdentifierExpression", TOKEN_COMPLEMENT)));
        rules.put(TOKEN_NOT, prefixRule(unaryNonIdentifier(LOGICAL_NEGATION_NON_IDENTIFIER_EXPRESSION, "logicalNegationNonIdentifierExpression", TOKEN_NOT)));
        rules.put(TOKEN_NULL, prefixRule(literalExpression(NULL_LITERAL, "nullLiteral", TOKEN_NULL)));
        rules.put(TOKEN_NIL, prefixRule(literalExpression(NIL_LITERAL, "nilLiteral", TOKEN_NIL)));
        rules.put(TOKEN_LISTENER, prefixRule(literalExpression(LISTENER_LITERAL, "listenerLiteral", TOKEN_LISTENER)));
        rules.put(TOKEN_FLOAT, prefixRule(literalExpression(FLOAT_LITERAL, "floatLiteral", TOKEN_FLOAT)));
        rules.put(TOKEN_INTEGER, prefixRule(literalExpression(INTEGER_LITERAL, "integerLiteral", TOKEN_INTEGER)));
        rules.put(TOKEN_STRING, prefixRule(literalExpression(STRING_LITERAL, "stringLiteral", TOKEN_STRING)));
        return rules;
    }

    private ConcreteSyntaxTree.Node parseScalarComponentExpression() {
        var expression = parseIdentifierScalarComponentExpression();
        if (parser.isMatched(expression)) {
            return nodes.scalarComponentExpression(expression);
        }

        expression = parseNonIdentifierScalarComponentExpression();
        if (parser.isMatched(expression)) {
            return nodes.scalarComponentExpression(expression);
        }
        return null;
    }

    private ConcreteSyntaxTree.Node parseIdentifierScalarComponentExpression() {
        var tokenIdentifier = parser.consume(TOKEN_IDENTIFIER);
        if (!parser.isMatched(tokenIdentifier)) {
            return null;
        }
        return nodes.identifierScalarComponentExpression(tokenIdentifier);
    }

    private ConcreteSyntaxTree.Node parseNonIdentifierScalarComponentExpression() {
        return parseNonIdentifierScalarComponentExpression(NONE);
    }

    private ConcreteSyntaxTree.Node parseNonIdentifierScalarComponentExpression(Operator precedence) {
        var result = parser.parseRules(precedence, nonIdentifierScalarComponentExpressionRules);
        return parser.isMatched(result) ? nodes.nonIdentifierScalarComponentExpression(result) : null;
    }

    ParseRule.ParseFn VECTOR_OR_GROUP_EXPRESSION_FN = (ConcreteSyntaxTree.Node lhs, ParseRule rule, Parser parser) -> {
        var tokenLeftBracket = parser.consume(TOKEN_LEFT_BRACKET);
        if (!parser.isMatched(tokenLeftBracket)) {
            return null;
        }
        parser.consumeNewLines();

        Token tokenRightBracket;

        var pos = parser.mark();

        // try parse expression
        var expression = parser.parseExpression();
        if (parser.isMatched(expression)) {
            parser.consumeNewLines();

            tokenRightBracket = parser.consume(TOKEN_RIGHT_BRACKET);
            if (!parser.isMatched(tokenRightBracket)) {
                // Try vector
                parser.restore(pos);
            } else {
                return nodes.groupingExpression(tokenLeftBracket, expression, tokenRightBracket);
            }
        }

        // try parse vector
        ConcreteSyntaxTree.Node numberExpressionX, numberExpressionY, numberExpressionZ;

        numberExpressionX = parseScalarComponentExpression();
        if (!parser.isMatched(numberExpressionX)) {
            parser.errorBadToken(parser.currentToken(), "vector scalar component");
            return null;
        }

        numberExpressionY = parseScalarComponentExpression();
        if (!parser.isMatched(numberExpressionY)) {
            parser.errorBadToken(parser.currentToken(), "vector scalar component");
            return null;
        }

        numberExpressionZ = parseScalarComponentExpression();
        if (!parser.isMatched(numberExpressionZ)) {
            // when two first terms parsed as number expression, we are definitely parsing vector with errors
            parser.errorBadToken(parser.currentToken(), "vector scalar componen");
            return null;
        }

        parser.consumeNewLines();

        tokenRightBracket = parser.consume(TOKEN_RIGHT_BRACKET);
        if (!parser.isMatched(tokenRightBracket)) {
            // when all three terms parsed as number expression, we are definitely parsing vector that is missing closing bracket
            parser.errorBadToken(parser.currentToken(), TOKEN_RIGHT_BRACKET);
            return null;
        }

        return nodes.vectorDeclarationExpression(
                tokenLeftBracket,
                numberExpressionX,
                numberExpressionY,
                numberExpressionZ,
                tokenRightBracket
        );
    };

    ParseRule.ParseFn TARGETNAME_EXPRESSION_FN = (ConcreteSyntaxTree.Node lhs, ParseRule rule, Parser parser) -> {
        var tokenDollar = parser.consume(TOKEN_DOLLAR);
        if (!parser.isMatched(tokenDollar)) {
            return null;
        }
        var primaryExpression = parser.parsePrimaryExpression();
        if (!parser.isMatched(primaryExpression)) {
            parser.errorBadToken(parser.currentToken(), "non identifier primary expression");
            return null;
        }
        return nodes.targetnameExpression(tokenDollar, primaryExpression);
    };

    ParseRule.ParseFn TARGETNAME_SCALAR_COMPONENT_EXPRESSION_FN = (ConcreteSyntaxTree.Node lhs, ParseRule rule, Parser parser) -> {
        var tokenDollar = parser.consume(TOKEN_DOLLAR);
        if (!parser.isMatched(tokenDollar)) {
            return null;
        }
        var expression = parseScalarComponentExpression();
        if (!parser.isMatched(expression)) {
            parser.errorBadToken(parser.currentToken(), "number expression");
            return null;
        }
        return nodes.targetnameScalarComponentExpression(tokenDollar, expression);
    };

    ParseRule.ParseFn MEMBER_SELECTION_EXPRESSION_FN = (ConcreteSyntaxTree.Node lhs, ParseRule rule, Parser parser) -> {
        var tokenPeriod = parser.consume(TOKEN_PERIOD);
        if (!parser.isMatched(tokenPeriod)) {
            return null;
        }

        var tokenMemberName = parser.consume(
            TOKEN_CASE,
            TOKEN_IF,
            TOKEN_ELSE,
            TOKEN_WHILE,
            TOKEN_FOR,
            TOKEN_TRY,
            TOKEN_CATCH,
            TOKEN_SWITCH,
            TOKEN_BREAK,
            TOKEN_CONTINUE,
            TOKEN_END,
            TOKEN_SIZE,
            TOKEN_LISTENER,
            TOKEN_STRING,
            TOKEN_IDENTIFIER
        );

        if (!parser.isMatched(tokenMemberName)) {
            parser.errorBadToken(parser.currentToken(), "string or identifier");
            return null;
        }

        return nodes.memberSelectionExpression(lhs, tokenPeriod, tokenMemberName);
    };

    ParseRule.ParseFn SUBSCRIPT_EXPRESSION_FN = (ConcreteSyntaxTree.Node lhs, ParseRule rule, Parser parser) -> {
        var tokenLeftSqBracket = parser.consume(TOKEN_LEFT_SQUARE_BRACKET);
        if (!parser.isMatched(tokenLeftSqBracket)) {
            return null;
        }
        var expression = parser.parseExpression();
        if (!parser.isMatched(expression)) {
            parser.errorBadToken(parser.currentToken(), "expression");
            return null;
        }
        var tokenRightSqBracket = parser.consume(TOKEN_RIGHT_SQUARE_BRACKET);
        if (!parser.isMatched(tokenRightSqBracket)) {
            parser.errorBadToken(parser.currentToken(), TOKEN_RIGHT_SQUARE_BRACKET);
            return null;
        }
        return nodes.subscriptExpression(lhs, tokenLeftSqBracket, expression, tokenRightSqBracket);
    };
}
