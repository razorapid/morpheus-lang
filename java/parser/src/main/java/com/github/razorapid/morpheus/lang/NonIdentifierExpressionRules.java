package com.github.razorapid.morpheus.lang;

import com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree;
import com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTreeBuilder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

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
        rules.put(TokenType.TOKEN_DOLLAR, ParseRule.prefixRule(TARGETNAME_SCALAR_COMPONENT_EXPRESSION_FN));
        return rules;
    }

    private Map<TokenType, ParseRule> createNonIdentifierPrimaryExpressionRules() {
        var rules = new HashMap<TokenType, ParseRule>();
        rules.put(TokenType.TOKEN_LEFT_BRACKET, ParseRule.prefixRule(VECTOR_OR_GROUP_EXPRESSION_FN));
        rules.put(TokenType.TOKEN_LEFT_SQUARE_BRACKET, ParseRule.infixRule(SUBSCRIPT_EXPRESSION_FN, Operator.INDEX));
        rules.put(TokenType.TOKEN_DOLLAR, ParseRule.prefixRule(TARGETNAME_EXPRESSION_FN));
        rules.put(TokenType.TOKEN_PERIOD, ParseRule.infixRule(MEMBER_SELECTION_EXPRESSION_FN, Operator.PROPERTY_COMMAND));
        rules.put(TokenType.TOKEN_NEG, ParseRule.prefixRule(ParseRule.ParseFn.unaryNonIdentifier(ConcreteSyntaxTree.NodeType.ARITHMETIC_NEGATION_NON_IDENTIFIER_EXPRESSION, "arithmeticNegationNonIdentifierExpression", TokenType.TOKEN_NEG)));
        //nonIdentifyPrimaryExpressionRules.put(TOKEN_POS <-- the token exists in original parser, but looks like it's not used anywhere
        rules.put(TokenType.TOKEN_COMPLEMENT, ParseRule.prefixRule(ParseRule.ParseFn.unaryNonIdentifier(ConcreteSyntaxTree.NodeType.BITWISE_COMPLEMENT_NON_IDENTIFIER_EXPRESSION, "bitwiseComplementNonIdentifierExpression", TokenType.TOKEN_COMPLEMENT)));
        rules.put(TokenType.TOKEN_NOT, ParseRule.prefixRule(ParseRule.ParseFn.unaryNonIdentifier(ConcreteSyntaxTree.NodeType.LOGICAL_NEGATION_NON_IDENTIFIER_EXPRESSION, "logicalNegationNonIdentifierExpression", TokenType.TOKEN_NOT)));
        rules.put(TokenType.TOKEN_NULL, ParseRule.prefixRule(ParseRule.ParseFn.literalExpression(ConcreteSyntaxTree.NodeType.NULL_LITERAL, "nullLiteral", TokenType.TOKEN_NULL)));
        rules.put(TokenType.TOKEN_NIL, ParseRule.prefixRule(ParseRule.ParseFn.literalExpression(ConcreteSyntaxTree.NodeType.NIL_LITERAL, "nilLiteral", TokenType.TOKEN_NIL)));
        rules.put(TokenType.TOKEN_LISTENER, ParseRule.prefixRule(ParseRule.ParseFn.literalExpression(ConcreteSyntaxTree.NodeType.LISTENER_LITERAL, "listenerLiteral", TokenType.TOKEN_LISTENER)));
        rules.put(TokenType.TOKEN_FLOAT, ParseRule.prefixRule(ParseRule.ParseFn.literalExpression(ConcreteSyntaxTree.NodeType.FLOAT_LITERAL, "floatLiteral", TokenType.TOKEN_FLOAT)));
        rules.put(TokenType.TOKEN_INTEGER, ParseRule.prefixRule(ParseRule.ParseFn.literalExpression(ConcreteSyntaxTree.NodeType.INTEGER_LITERAL, "integerLiteral", TokenType.TOKEN_INTEGER)));
        rules.put(TokenType.TOKEN_STRING, ParseRule.prefixRule(ParseRule.ParseFn.literalExpression(ConcreteSyntaxTree.NodeType.STRING_LITERAL, "stringLiteral", TokenType.TOKEN_STRING)));
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
        var tokenIdentifier = parser.consume(TokenType.TOKEN_IDENTIFIER);
        if (!parser.isMatched(tokenIdentifier)) {
            return null;
        }
        return nodes.identifierScalarComponentExpression(tokenIdentifier);
    }

    private ConcreteSyntaxTree.Node parseNonIdentifierScalarComponentExpression() {
        return parseNonIdentifierScalarComponentExpression(Operator.NONE);
    }

    private ConcreteSyntaxTree.Node parseNonIdentifierScalarComponentExpression(Operator precedence) {
        var result = parser.parseRules(precedence, nonIdentifierScalarComponentExpressionRules);
        return parser.isMatched(result) ? nodes.nonIdentifierScalarComponentExpression(result) : null;
    }

    ParseRule.ParseFn VECTOR_OR_GROUP_EXPRESSION_FN = (ConcreteSyntaxTree.Node lhs, ParseRule rule, Parser parser) -> {
        var tokenLeftBracket = parser.consume(TokenType.TOKEN_LEFT_BRACKET);
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

            tokenRightBracket = parser.consume(TokenType.TOKEN_RIGHT_BRACKET);
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

        tokenRightBracket = parser.consume(TokenType.TOKEN_RIGHT_BRACKET);
        if (!parser.isMatched(tokenRightBracket)) {
            // when all three terms parsed as number expression, we are definitely parsing vector that is missing closing bracket
            parser.errorBadToken(parser.currentToken(), TokenType.TOKEN_RIGHT_BRACKET);
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
        var tokenDollar = parser.consume(TokenType.TOKEN_DOLLAR);
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
        var tokenDollar = parser.consume(TokenType.TOKEN_DOLLAR);
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
        var tokenPeriod = parser.consume(TokenType.TOKEN_PERIOD);
        if (!parser.isMatched(tokenPeriod)) {
            return null;
        }

        var tokenMemberName = parser.consume(
            TokenType.TOKEN_CASE,
            TokenType.TOKEN_IF,
            TokenType.TOKEN_ELSE,
            TokenType.TOKEN_WHILE,
            TokenType.TOKEN_FOR,
            TokenType.TOKEN_TRY,
            TokenType.TOKEN_CATCH,
            TokenType.TOKEN_SWITCH,
            TokenType.TOKEN_BREAK,
            TokenType.TOKEN_CONTINUE,
            TokenType.TOKEN_END,
            TokenType.TOKEN_SIZE,
            TokenType.TOKEN_LISTENER,
            TokenType.TOKEN_STRING,
            TokenType.TOKEN_IDENTIFIER
        );

        if (!parser.isMatched(tokenMemberName)) {
            parser.errorBadToken(parser.currentToken(), "string or identifier");
            return null;
        }

        return nodes.memberSelectionExpression(lhs, tokenPeriod, tokenMemberName);
    };

    ParseRule.ParseFn SUBSCRIPT_EXPRESSION_FN = (ConcreteSyntaxTree.Node lhs, ParseRule rule, Parser parser) -> {
        var tokenLeftSqBracket = parser.consume(TokenType.TOKEN_LEFT_SQUARE_BRACKET);
        if (!parser.isMatched(tokenLeftSqBracket)) {
            return null;
        }
        var expression = parser.parseExpression();
        if (!parser.isMatched(expression)) {
            parser.errorBadToken(parser.currentToken(), "expression");
            return null;
        }
        var tokenRightSqBracket = parser.consume(TokenType.TOKEN_RIGHT_SQUARE_BRACKET);
        if (!parser.isMatched(tokenRightSqBracket)) {
            parser.errorBadToken(parser.currentToken(), TokenType.TOKEN_RIGHT_SQUARE_BRACKET);
            return null;
        }
        return nodes.subscriptExpression(lhs, tokenLeftSqBracket, expression, tokenRightSqBracket);
    };
}
