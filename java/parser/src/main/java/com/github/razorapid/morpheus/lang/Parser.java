package com.github.razorapid.morpheus.lang;

import com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree;
import com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTreeBuilder;
import lombok.NonNull;

import java.util.*;

public class Parser {

    private final Source script;
    private final Tokens tokens;
    private final ConcreteSyntaxTreeBuilder nodes = new ConcreteSyntaxTreeBuilder();
    private final Map<TokenType, ParseRule> nonIdentifierPrimaryExpressionRules;

    private boolean panicMode = false;
    private final List<ParseError> errors = new ArrayList<>();

    public Parser(@NonNull Source script, @NonNull Tokens tokens) {
        this.script = script;
        this.tokens = tokens;

        var nonIdentifierExpressionRules = new NonIdentifierExpressionRules(this);
        this.nonIdentifierPrimaryExpressionRules = nonIdentifierExpressionRules.nonIdentifierPrimaryExpressionRules();
    }

    public List<ParseError> errors() {
        return Collections.unmodifiableList(errors);
    }

    public ConcreteSyntaxTree parse() {
        var statementList = parseStatementList(true);
        return new ConcreteSyntaxTree(statementList);
    }

    private ConcreteSyntaxTree.Node parseStatementList(boolean strict) {
        var statements = new ArrayList<ConcreteSyntaxTree.Node>();
        while (isNotEOF()) {
            try {
                var statement = parseStatement();
                if (!isMatched(statement)) {
                    if (strict && !check(TokenType.TOKEN_EOL, TokenType.TOKEN_SEMICOLON)) {
                        errorBadToken(peekToken(), "next statement");
                    }
                    break;
                }
                if (strict && !check(TokenType.TOKEN_EOL, TokenType.TOKEN_SEMICOLON)) {
                    errorBadToken(peekToken(), "next statement or semicolon");
                }
                var tokenEol = consume(TokenType.TOKEN_EOL);
                statements.add(nodes.statementLine(statement, tokenEol));
            } catch (ParseError e) {
                errors.add(e);
                panicMode = true;
                sync();
            }
        }

        return nodes.statementList(statements);
    }

    private ConcreteSyntaxTree.Node parseStatement() {
        var statement = parseCompoundStatement();
        if (isMatched(statement)) {
            return nodes.statement(statement);
        }

        statement = parseLabelStatement();
        if (isMatched(statement)) {
            return nodes.statement(statement);
        }

        statement = parseSelectionStatement();
        if (isMatched(statement)) {
            return nodes.statement(statement);
        }

        statement = parseIterationStatement();
        if (isMatched(statement)) {
            return nodes.statement(statement);
        }

        statement = parseTryCatchStatement();
        if (isMatched(statement)) {
            return nodes.statement(statement);
        }

        statement = parseBreakStatement();
        if (isMatched(statement)) {
            return nodes.statement(statement);
        }

        statement = parseContinueStatement();
        if (isMatched(statement)) {
            return nodes.statement(statement);
        }

        statement = parseNoopStatement();
        if (isMatched(statement)) {
            return nodes.statement(statement);
        }

        statement = parseExpressionStatement();
        if (isMatched(statement)) {
            return nodes.statement(statement);
        }

        return null;
    }

    private ConcreteSyntaxTree.Node parseCompoundStatement() {
        var tokenLeftBraces = consume(TokenType.TOKEN_LEFT_BRACES);
        if (!isMatched(tokenLeftBraces)) {
            return null;
        }
        consumeNewLines();

        var statements = new ArrayList<ConcreteSyntaxTree.Node>();
        while (isNotEOF() && !check(TokenType.TOKEN_RIGHT_BRACES)) {
            var statement = parseStatement();
            if (!isMatched(statement)) {
                return null;
            }
            statements.add(statement);
            consumeNewLines();
        }
        var tokenRightBraces = consume(TokenType.TOKEN_RIGHT_BRACES);
        if (!isMatched(tokenRightBraces)) {
            errorBadToken(tokens.peekToken(), TokenType.TOKEN_RIGHT_BRACES);
            return null;
        }

        return nodes.compoundStatement(tokenLeftBraces, statements, tokenRightBraces);
    }

    private ConcreteSyntaxTree.Node parseLabelStatement() {
        var statement = parseSwitchCaseLabelStatement();
        if (isMatched(statement)) {
            return nodes.labelStatement(statement);
        }

        statement = parseThreadLabelStatement();
        if (isMatched(statement)) {
            return nodes.labelStatement(statement);
        }
        return null;
    }

    private ConcreteSyntaxTree.Node parseSelectionStatement() {
        var statement = parseIfElseStatement();
        if (isMatched(statement)) {
            return nodes.selectionStatement(statement);
        }

        statement = parseSwitchStatement();
        if (isMatched(statement)) {
            return nodes.selectionStatement(statement);
        }

        return null;
    }

    private ConcreteSyntaxTree.Node parseIterationStatement() {
        var statement = parseWhileStatement();
        if (isMatched(statement)) {
            return nodes.iterationStatement(statement);
        }

        statement = parseForStatement();
        if (isMatched(statement)) {
            return nodes.iterationStatement(statement);
        }
        return null;
    }

    private ConcreteSyntaxTree.Node parseTryCatchStatement() {
        var tokenTry = consume(TokenType.TOKEN_TRY);
        if (!isMatched(tokenTry)) {
            return null;
        }
        consumeNewLines();

        var tryStatement = parseCompoundStatement();
        if (!isMatched(tryStatement)) {
            errorBadToken(tokens.peekToken(), "block of statements beginning with " + TokenType.TOKEN_LEFT_BRACES.nameWithExample());
            return null;
        }
        consumeNewLines();

        var tokenCatch = consume(TokenType.TOKEN_CATCH);
        if (!isMatched(tokenCatch)) {
            errorBadToken(tokens.peekToken(), TokenType.TOKEN_CATCH);
            return null;
        }
        consumeNewLines();

        var catchStatement = parseCompoundStatement();
        if (!isMatched(catchStatement)) {
            errorBadToken(tokens.peekToken(), TokenType.TOKEN_LEFT_BRACES);
            return null;
        }

        return nodes.tryCatchStatement(
                tokenTry,
                tryStatement,
                tokenCatch,
                catchStatement
        );
    }

    private ConcreteSyntaxTree.Node parseBreakStatement() {
        if (match(TokenType.TOKEN_BREAK)) {
            return nodes.breakStatement(tokens.lastToken());
        }
        return null;
    }

    private ConcreteSyntaxTree.Node parseContinueStatement() {
        if (match(TokenType.TOKEN_CONTINUE)) {
            return nodes.continueStatement(tokens.lastToken());
        }
        return null;
    }

    private ConcreteSyntaxTree.Node parseNoopStatement() {
        if (match(TokenType.TOKEN_SEMICOLON)) {
            return nodes.noopStatement(tokens.lastToken());
        }
        return null;
    }

    private ConcreteSyntaxTree.Node parseExpressionStatement() {
        var statement = parseThreadFunctionCallExpression();
        if (isMatched(statement)) {
            return nodes.expressionStatement(statement);
        }

        statement = parseListenerFunctionCallExpression();
        if (isMatched(statement)) {
            return nodes.expressionStatement(statement);
        }

        statement = parseAssignmentExpression();
        if (isMatched(statement)) {
            return nodes.expressionStatement(statement);
        }

        statement = parseIncrementOrDecrementExpression();
        if (isMatched(statement)) {
            return nodes.expressionStatement(statement);
        }
        return null;
    }

    private ConcreteSyntaxTree.Node parseSwitchCaseLabelStatement() {
        var tokenCase = consume(TokenType.TOKEN_CASE);
        if (!isMatched(tokenCase)) {
            return null;
        }

        var tokenIntOrText = consume(TokenType.TOKEN_INTEGER, TokenType.TOKEN_IDENTIFIER, TokenType.TOKEN_STRING);
        if (!isMatched(tokenIntOrText)) {
            errorBadToken(tokens.peekToken(), TokenType.TOKEN_INTEGER, TokenType.TOKEN_IDENTIFIER, TokenType.TOKEN_STRING);
            return null;
        }

        var eventParamList = parseEventParameterList();

        var tokenColon = consume(TokenType.TOKEN_COLON);
        if (!isMatched(tokenColon)) {
            errorBadToken(tokens.peekToken(), TokenType.TOKEN_COLON);
            return null;
        }

        return nodes.switchCaseLabelStatement(tokenCase, tokenIntOrText, eventParamList, tokenColon);
    }

    /**
     * This rule is ambiguous with rule: threadFunctionCallExpression
     * We mark parser position, to backtrack to known state if we don't match colon
     * at the end of the statement.
     *
     * This way we will later parse this in threadFunctionCallExpression production rule.
     *
     * Note: This backtracking is quite expensive. Especially if there were a lot of event parameters used.
     * This is done for the sake of keeping source code close to grammar rules definitions
     * (which themselves are defined with readability and comprehensibility in mind)
     *
     * @see #parseThreadFunctionCallExpression
     */
    private ConcreteSyntaxTree.Node parseThreadLabelStatement() {
        var mark = mark(); //
        var tokenIdentOrEnd = consume(TokenType.TOKEN_IDENTIFIER, TokenType.TOKEN_END);
        if (!isMatched(tokenIdentOrEnd)) {
            return null;
        }

        var eventParamList = parseEventParameterList();

        var tokenColon = consume(TokenType.TOKEN_COLON);
        if (isMatched(tokenColon)) {
            return nodes.threadLabelStatement(tokenIdentOrEnd, eventParamList, tokenColon);
        }

        // not matched, try threadFunctionCallExpression later
        restore(mark);
        return null;
    }

    private ConcreteSyntaxTree.Node parseIfElseStatement() {
        var ifToken = consume(TokenType.TOKEN_IF);
        if (!isMatched(ifToken)) {
            return null;
        }
        consumeNewLines();

        var primaryExpression = parsePrimaryExpression();
        if (!isMatched(primaryExpression)) {
            errorBadToken(tokens.peekToken(), "primary expression");
            return null;
        }
        consumeNewLines();

        var statement = parseStatement();
        if (!isMatched(statement)) {
            errorBadToken(tokens.peekToken(), "statement");
            return null;
        }
        consumeNewLines();

        var tokenElse = consume(TokenType.TOKEN_ELSE);
        ConcreteSyntaxTree.Node elseStatement = null;
        if (isMatched(tokenElse)) {
            consumeNewLines();
            elseStatement = parseStatement();
            if (!isMatched(elseStatement)) {
                errorBadToken(tokens.peekToken(), "else statement");
                return null;
            }
        } else {
            // We consumed all new lines looking for else statement
            // We didn't find it so we end the if statement with a new line
            if (tokens.peekTokenAhead(-1).type() == TokenType.TOKEN_EOL) {
                tokens.rewind(1);
            }
        }

        return nodes.ifElseStatement(ifToken, primaryExpression, statement, tokenElse, elseStatement);
    }

    private ConcreteSyntaxTree.Node parseSwitchStatement() {
        var tokenSwitch = consume(TokenType.TOKEN_SWITCH);
        if (!isMatched(tokenSwitch)) {
            return null;
        }
        consumeNewLines();

        var primaryExpression = parsePrimaryExpression();
        if (!isMatched(primaryExpression)) {
            errorBadToken(tokens.peekToken(), "primary expression");
            return null;
        }
        consumeNewLines();

        var compoundStatement = parseCompoundStatement();
        if (!isMatched(compoundStatement)) {
            errorBadToken(tokens.peekToken(), "block of statements beginning with " + TokenType.TOKEN_LEFT_BRACES.nameWithExample());
            return null;
        }

        return nodes.switchStatement(tokenSwitch, primaryExpression, compoundStatement);
    }

    private ConcreteSyntaxTree.Node parseWhileStatement() {
        var tokenWhile = consume(TokenType.TOKEN_WHILE);
        if (!isMatched(tokenWhile)) {
            return null;
        }
        consumeNewLines();

        var primaryExpression = parsePrimaryExpression();
        if (!isMatched(primaryExpression)) {
            errorBadToken(tokens.peekToken(), "primary expression");
            return null;
        }
        consumeNewLines();

        var loopStatement = parseStatement();
        if (!isMatched(loopStatement)) {
            errorBadToken(tokens.peekToken(), "loop body");
            return null;
        }

        return nodes.whileStatement(tokenWhile, primaryExpression, loopStatement);
    }

    private ConcreteSyntaxTree.Node parseForStatement() {
        var tokenFor = consume(TokenType.TOKEN_FOR);
        if (!isMatched(tokenFor)) {
            return null;
        }
        consumeNewLines();

        var tokenLeftBracket = consume(TokenType.TOKEN_LEFT_BRACKET);
        if (!isMatched(tokenLeftBracket)) {
            errorBadToken(tokens.peekToken(), TokenType.TOKEN_LEFT_BRACKET);
            return null;
        }
        consumeNewLines();

        var preStatement = parseStatement();
        if (!isMatched(preStatement)) {
            errorBadToken(tokens.peekToken(), "statement");
            return null;
        }
        consumeNewLines();

        var tokenStatementSemicolon = consume(TokenType.TOKEN_SEMICOLON);
        if (!isMatched(tokenStatementSemicolon)) {
            errorBadToken(tokens.peekToken(), TokenType.TOKEN_SEMICOLON);
            return null;
        }
        consumeNewLines();

        var expression = parseExpression();
        if (!isMatched(expression)) {
            errorBadToken(tokens.peekToken(), "expression");
            return null;
        }
        consumeNewLines();

        var expressionSemicolon = consume(TokenType.TOKEN_SEMICOLON);
        if (!isMatched(expressionSemicolon)) {
            errorBadToken(tokens.peekToken(), TokenType.TOKEN_SEMICOLON);
            return null;
        }
        consumeNewLines();

        var postStatements = parseStatementList(false);
        if (!isMatched(postStatements)) {
            errorBadToken(tokens.peekToken(), "statement");
            return null;
        }
        consumeNewLines();

        var tokenRightBracket = consume(TokenType.TOKEN_RIGHT_BRACKET);
        if (!isMatched(tokenRightBracket)) {
            errorBadToken(tokens.peekToken(), TokenType.TOKEN_RIGHT_BRACKET);
            return null;
        }
        consumeNewLines();

        var loopStatement = parseStatement();
        if (!isMatched(loopStatement)) {
            errorBadToken(tokens.peekToken(), "loop body");
            return null;
        }

        return nodes.forStatement(
                tokenFor,
                tokenLeftBracket,
                preStatement,
                tokenStatementSemicolon,
                expression,
                expressionSemicolon,
                postStatements,
                tokenRightBracket,
                loopStatement
        );
    }

    /**
     * This rule is ambiguous with threadLabelStatement (see #parseThreadLabelStatement)
     * You need to be careful with rule parsing order.
     *
     * @see #parseThreadLabelStatement
     */
    private ConcreteSyntaxTree.Node parseThreadFunctionCallExpression() {
        var tokenIdentOrEnd = consume(TokenType.TOKEN_IDENTIFIER, TokenType.TOKEN_END);
        if (!isMatched(tokenIdentOrEnd)) {
            return null;
        }

        var eventParamList = parseEventParameterList();

        return nodes.threadFunctionCallExpression(tokenIdentOrEnd, eventParamList);
    }

    /**
     * This rule is ambiguous with the rest of expression statements
     * that start with nonIdentifierPrimaryExpression rule.
     *
     * This method uses backtracking to rollback to last known parser position
     * upon failing to match the rule at the ambiguity points.
     *
     * @see #parseAssignmentExpression
     * @see #parseIncrementOrDecrementExpression
     * @return
     */
    private ConcreteSyntaxTree.Node parseListenerFunctionCallExpression() {
        var pos = mark();

        var expression = parseNonIdentifierPrimaryExpression();
        if (!isMatched(expression)) {
            return null;
        }

        var tokenIdentifier = consume(TokenType.TOKEN_IDENTIFIER);
        if (!isMatched(tokenIdentifier)) {
            restore(pos);
            return null;
        }

        var eventParamList = parseEventParameterList();

        return nodes.listenerFunctionCallExpression(expression, tokenIdentifier, eventParamList);
    }

    private ConcreteSyntaxTree.Node parseAssignmentExpression() {
        var pos = mark();

        var lhs = parseNonIdentifierPrimaryExpression();
        if (!isMatched(lhs)) {
            return null;
        }

        var tokenAssignmentOp = consume(TokenType.TOKEN_ASSIGNMENT, TokenType.TOKEN_PLUS_EQUALS, TokenType.TOKEN_MINUS_EQUALS);
        if (!isMatched(tokenAssignmentOp)) {
            restore(pos);
            return null;
        }

        var rhs = parseExpression();
        if (!isMatched(rhs)) {
            error("Expected expression on the right side of the " + tokenAssignmentOp.lexeme() + " operator.");
            return null;
        }
        return nodes.assignmentExpression(lhs, tokenAssignmentOp, rhs);
    }

    private ConcreteSyntaxTree.Node parseIncrementOrDecrementExpression() {
        var pos = mark();

        var lhs = parseNonIdentifierPrimaryExpression();
        if (!isMatched(lhs)) {
            return null;
        }

        var tokenOp = consume(TokenType.TOKEN_INC, TokenType.TOKEN_DEC);
        if (!isMatched(tokenOp)) {
            restore(pos);
            return null;
        }

        return switch (tokenOp.type()) {
            case TOKEN_INC -> nodes.incrementExpression(lhs, tokenOp);
            case TOKEN_DEC -> nodes.decrementExpression(lhs, tokenOp);
            default -> throw new IllegalStateException("Unexpected value: " + tokenOp.type());
        };
    }

    private ConcreteSyntaxTree.Node parseEventParameterList() {
        var expressions = new ArrayList<ConcreteSyntaxTree.Node>();
        while(isNotEOF()) {
            var primExpression = parsePrimaryExpression();
            if (isMatched(primExpression)) {
                expressions.add(primExpression);
            } else {
                break;
            }
        }
        return nodes.eventParameterList(expressions);
    }

    /**
     * nonIdentifierPrimaryExpression production rule is left-recursive
     * In order to handle it in an elegant way, Pratt expression parsing algorithm is used
     * This way we don't need to factor our rules to get rid of left-recursion and focusing instead
     * on grammar readability
     */
    ConcreteSyntaxTree.Node parseNonIdentifierPrimaryExpression() {
        return parseNonIdentifierPrimaryExpression(Operator.NONE);
    }

    private ConcreteSyntaxTree.Node parseNonIdentifierPrimaryExpression(Operator precedence) {
        var expression = parseRules(precedence, nonIdentifierPrimaryExpressionRules);
        return isMatched(expression) ? nodes.nonIdentifierPrimaryExpression(expression) : null;
    }

    ConcreteSyntaxTree.Node parsePrimaryExpression() {
        var expression = parseConstArrayExpression();
        if (isMatched(expression)) {
            return nodes.primaryExpression(expression);
        }

        expression = parseIdentifierPrimaryExpression();
        if (isMatched(expression)) {
            return nodes.primaryExpression(expression);
        }

        expression = parseNonIdentifierPrimaryExpression();
        if (isMatched(expression)) {
            return nodes.primaryExpression(expression);
        }

        return null;
    }

    private ConcreteSyntaxTree.Node parseConstArrayExpression() {
        var pos = mark();
        var lhs = parseIdentifierPrimaryExpression();
        if (!isMatched(lhs)) {
            lhs = parseNonIdentifierPrimaryExpression();
            if (!isMatched(lhs)) {
                restore(pos);
                return null;
            }
        }

        if (!check(TokenType.TOKEN_DOUBLE_COLON)) {
            restore(pos);
            return null;
        }

        var constArrayElems = new ArrayList<ConcreteSyntaxTree.Node>();
        constArrayElems.add(lhs);
        ConcreteSyntaxTree.Node rhs;
        do {
            var tokenDoubleColon = consume(TokenType.TOKEN_DOUBLE_COLON);
            if (!isMatched(tokenDoubleColon)) {
                break;
            }
            rhs = parseIdentifierPrimaryExpression();
            if (!isMatched(rhs)) {
                rhs = parseNonIdentifierPrimaryExpression();
                if (!isMatched(rhs)) {
                    error(
                        "Const array declaration can't end on " +
                        tokenDoubleColon.lexeme() +
                        ". You need to end the declaration with array element."
                    );
                }
            }

            constArrayElems.add(nodes.constArrayElementSeparator(tokenDoubleColon));
            constArrayElems.add(rhs);

        } while (isMatched(rhs));

        return nodes.constArrayExpression(constArrayElems);
    }

    private ConcreteSyntaxTree.Node parseIdentifierPrimaryExpression() {
        var tokenIdentifier = consume(TokenType.TOKEN_IDENTIFIER);
        if (!isMatched(tokenIdentifier)) {
            return null;
        }
        return nodes.identifierPrimaryExpression(tokenIdentifier);
    }

    ConcreteSyntaxTree.Node parseExpression() {
        return parseExpression(Operator.NONE);
    }

    ConcreteSyntaxTree.Node parseExpression(Operator precedence) {
        var expression = parseMakeArrayExpression();
        if (isMatched(expression)) {
            ConcreteSyntaxTree.Node rhs = nodes.expression(expression);
            do {
                rhs = parseBinaryExpression(precedence, rhs);
                if (isMatched(rhs)) {
                    expression = rhs;
                }
            } while (isMatched(rhs));
            return nodes.expression(expression);
        }

        expression = parseFunctionPrimaryExpression();
        if (isMatched(expression)) {
            ConcreteSyntaxTree.Node rhs = nodes.expression(expression);
            do {
                rhs = parseBinaryExpression(precedence, rhs);
                if (isMatched(rhs)) {
                    expression = rhs;
                }
            } while (isMatched(rhs));
            return nodes.expression(expression);
        }

        expression = parseNonIdentifierPrimaryExpression();
        if (isMatched(expression)) {
            ConcreteSyntaxTree.Node rhs = nodes.expression(expression);
            do {
                rhs = parseBinaryExpression(precedence, rhs);
                if (isMatched(rhs)) {
                    expression = rhs;
                }
            } while (isMatched(rhs));
            return nodes.expression(expression);
        }

        errorBadToken(peekToken(), "make array expression, function primary expression or non identifier primary expression");
        return null;
    }

    private ConcreteSyntaxTree.Node parseMakeArrayExpression() {
        var tokenMakeArray = consume(TokenType.TOKEN_MAKEARRAY);
        if (!isMatched(tokenMakeArray)) {
            return null;
        }
        var tokenEol = consume(TokenType.TOKEN_EOL);
        if (!isMatched(tokenEol)) {
            errorBadToken(peekToken(), TokenType.TOKEN_EOL);
            return null;
        }

        var rows = new ArrayList<ConcreteSyntaxTree.Node>();
        while (!check(TokenType.TOKEN_ENDARRAY)) {
            var row = parseMakeArrayRowExpression();
            if (!isMatched(row)) {
                break;
            }
            rows.add(row);
        }

        var tokenEndArray = consume(TokenType.TOKEN_ENDARRAY);
        if (!isMatched(tokenEndArray)) {
            errorBadToken(peekToken(), TokenType.TOKEN_ENDARRAY);
            return null;
        }

        return nodes.makeArrayExpression(tokenMakeArray, tokenEol, rows, tokenEndArray);
    }

    private ConcreteSyntaxTree.Node parseMakeArrayRowExpression() {
        var cols = new ArrayList<ConcreteSyntaxTree.Node>();
        while (true) {
            var col = parsePrimaryExpression();
            if (isMatched(col)) {
                cols.add(col);
            } else {
                errorBadToken(peekToken(), "primary expression");
                return null;
            }

            if (check(TokenType.TOKEN_EOL)) {
                break;
            }
        }
        var rowEnd = consume(TokenType.TOKEN_EOL);
        if (!isMatched(rowEnd)) {
            errorBadToken(peekToken(), TokenType.TOKEN_EOL);
            return null;
        }

        return nodes.makeArrayRowExpression(cols, rowEnd);
    }
    private ConcreteSyntaxTree.Node parseFunctionPrimaryExpression() {
        var expression = parseUnaryFunctionPrimaryExpression();
        if (isMatched(expression)) {
            return nodes.functionPrimaryExpression(expression);
        }

        expression = parseConstArrayExpression();
        if (isMatched(expression)) {
            return nodes.functionPrimaryExpression(expression);
        }

        expression = parseThreadFunctionCallExpression();
        if (isMatched(expression)) {
            return nodes.functionPrimaryExpression(expression);
        }

        expression = parseListenerFunctionCallExpression();
        if (isMatched(expression)) {
            return nodes.functionPrimaryExpression(expression);
        }

        return null;
    }

    private ConcreteSyntaxTree.Node parseBinaryExpression(Operator precedence, ConcreteSyntaxTree.Node lhs) {
        if (!isMatched(lhs)) {
            return null;
        }

        var rules = binaryExpressionRules;
        var tokenType = peekToken().type();
        var rule = rules.get(tokenType);

        ConcreteSyntaxTree.Node rhs = null;
        while (precedence.precedenceLowerThan(nextRulePrecedence(rules))) {
            rule = rules.get(peekToken().type());
            var infix = rule != null ? rule.infix() : null;
            /*
                No infix expression at this point, so we're done.
                This should never happen, because we won't go inside while loop without any infix rule
             */
            if (infix == null) {
                return rhs;
            }
            rhs = nodes.binaryExpression(infix.parse(lhs, rule, this));
            lhs = rhs;
        }
        return rhs;
    }

    private ConcreteSyntaxTree.Node parseUnaryFunctionPrimaryExpression() {
        /*
            This rule is ambiguous with unary non identifier primary expression
            that can also start with one of these unary operators
        */
        var pos = mark();
        var token = consume(TokenType.TOKEN_NEG, TokenType.TOKEN_COMPLEMENT, TokenType.TOKEN_NOT);
        if (!isMatched(token)) {
            return null;
        }

        var expression = parseFunctionPrimaryExpression();
        if (!isMatched(expression)) {
            restore(pos);
            return null;
        }

        return nodes.unaryFunctionPrimaryExpression(switch (token.type()) {
            case TOKEN_NEG -> nodes.arithmeticNegationFunctionExpression(token, expression);
            case TOKEN_COMPLEMENT -> nodes.bitwiseCompletionFunctionExpression(token, expression);
            case TOKEN_NOT -> nodes.logicalNegationFunctionExpression(token, expression);
            default -> throw new IllegalStateException("Unexpected value: " + token.type());
        });
    }

    /**
     * Pratt parsing algorithm
     */
    ConcreteSyntaxTree.Node parseRules(Operator precedence, Map<TokenType, ParseRule> rules) {
        var tokenType = peekToken().type();
        var rule = rules.get(tokenType);
        var prefix = rule != null ? rule.prefix() : null;

        if (prefix == null) {
            return null;
        }

        var leftHandSide = prefix.parse(rule, this);
        if (!isMatched(leftHandSide)) {
            throw new RuntimeException("Could not parse \"" + tokenType + "\".");
        }

        while (precedence.precedenceLowerThan(nextRulePrecedence(rules))) {
            rule = rules.get(peekToken().type());
            var infix = rule != null ? rule.infix() : null;
            /*
                No infix expression at this point, so we're done.
                This should never happen, because we won't go inside while loop without any infix rule
            */
            if (infix == null) {
                return leftHandSide;
            }
            leftHandSide = infix.parse(leftHandSide, rule, this);
        }
        return leftHandSide;
    }

    private Operator nextRulePrecedence(Map<TokenType, ParseRule> rules) {
        var rule = rules.get(peekToken().type());
        return rule != null && rule.infix() != null ? rule.precedence() : Operator.NULL;
    }

    boolean isMatched(ConcreteSyntaxTree.Node node) {
        return node != null;
    }

    boolean isMatched(Token token) {
        return token != null;
    }

    Token lastToken() {
        return tokens.lastToken();
    }

    Token currentToken() {
        return peekToken();
    }

    long mark() {
        return tokens.mark();
    }

    void restore(long pos) {
        tokens.restore(pos);
    }

    private boolean match(TokenType... types) {
        return tokens.match(types);
    }

    private boolean check(TokenType... types) {
        return tokens.check(types);
    }

    Token consume(TokenType... type) {
        return tokens.consume(type);
    }

    private Token nextToken() {
        return tokens.nextToken();
    }

    private Token peekToken() {
        return tokens.peekToken();
    }

    private boolean isNotEOF() {
        return !tokens.isEOF();
    }

    void consumeNewLines() {
        while (tokens.check(TokenType.TOKEN_EOL)) tokens.nextToken();
    }

    void errorBadToken(Token badToken, TokenType... expectedTokenTypes) {
        error("bad token '" + badToken.type().name() + " (" + badToken.lexeme() + ")', expected " + tokenNames(expectedTokenTypes));
    }

    void errorBadToken(Token badToken, String expected) {
        error("bad token '" + badToken.type().name() + "', expected '" + expected + "'");
    }

    String tokenNames(TokenType... tokens) {
        return String.join(" | ", Arrays.stream(tokens).map(TokenType::nameWithExample).toList());
    }

    void error(String message) {
        throw new ParseError(script, tokens.peekToken().pos(), message);
    }

    private void sync() {
        while (isNotEOF()) {
            if (nextToken().isType(TokenType.TOKEN_EOL)) {
                return;
            }
        }
    }

    private static final Map<TokenType, ParseRule> binaryExpressionRules = new HashMap<>();
    static {
        binaryExpressionRules.put(TokenType.TOKEN_MULTIPLY, ParseRule.infixRule(ParseRule.ParseFn.binary(ConcreteSyntaxTree.NodeType.MULTIPLICATION_EXPRESSION, "multiplicationExpression", TokenType.TOKEN_MULTIPLY), Operator.MULTIPLY));
        binaryExpressionRules.put(TokenType.TOKEN_DIVIDE, ParseRule.infixRule(ParseRule.ParseFn.binary(ConcreteSyntaxTree.NodeType.DIVISION_EXPRESSION, "divisionExpression", TokenType.TOKEN_DIVIDE), Operator.DIVIDE));
        binaryExpressionRules.put(TokenType.TOKEN_PERCENTAGE, ParseRule.infixRule(ParseRule.ParseFn.binary(ConcreteSyntaxTree.NodeType.MODULO_EXPRESSION, "moduloExpression", TokenType.TOKEN_PERCENTAGE), Operator.MODULUS));
        binaryExpressionRules.put(TokenType.TOKEN_PLUS, ParseRule.infixRule(ParseRule.ParseFn.binary(ConcreteSyntaxTree.NodeType.ADDITION_EXPRESSION, "additionExpression", TokenType.TOKEN_PLUS), Operator.PLUS));
        binaryExpressionRules.put(TokenType.TOKEN_MINUS, ParseRule.infixRule(ParseRule.ParseFn.binary(ConcreteSyntaxTree.NodeType.SUBTRACTION_EXPRESSION, "subtractionExpression", TokenType.TOKEN_MINUS), Operator.MINUS));
        binaryExpressionRules.put(TokenType.TOKEN_LESS_THAN, ParseRule.infixRule(ParseRule.ParseFn.binary(ConcreteSyntaxTree.NodeType.LESS_THAN_EXPRESSION, "lessThanExpression", TokenType.TOKEN_LESS_THAN), Operator.LESS_THAN));
        binaryExpressionRules.put(TokenType.TOKEN_GREATER_THAN, ParseRule.infixRule(ParseRule.ParseFn.binary(ConcreteSyntaxTree.NodeType.GREATER_THAN_EXPRESSION, "greaterThanExpression", TokenType.TOKEN_GREATER_THAN), Operator.GREATER_THAN));
        binaryExpressionRules.put(TokenType.TOKEN_LESS_THAN_OR_EQUAL, ParseRule.infixRule(ParseRule.ParseFn.binary(ConcreteSyntaxTree.NodeType.LESS_THAN_OR_EQUALS_EXPRESSION, "lessThanOrEqualExpression", TokenType.TOKEN_LESS_THAN_OR_EQUAL), Operator.LESS_THAN_OR_EQUAL));
        binaryExpressionRules.put(TokenType.TOKEN_GREATER_THAN_OR_EQUAL, ParseRule.infixRule(ParseRule.ParseFn.binary(ConcreteSyntaxTree.NodeType.GREATER_THAN_OR_EQUAL_EXPRESSION, "greaterThanOrEqualExpression", TokenType.TOKEN_GREATER_THAN_OR_EQUAL), Operator.GREATER_THAN_OR_EQUAL));
        binaryExpressionRules.put(TokenType.TOKEN_EQUALITY, ParseRule.infixRule(ParseRule.ParseFn.binary(ConcreteSyntaxTree.NodeType.EQUALITY_EXPRESSION, "equalityExpression", TokenType.TOKEN_EQUALITY), Operator.EQUALITY));
        binaryExpressionRules.put(TokenType.TOKEN_INEQUALITY, ParseRule.infixRule(ParseRule.ParseFn.binary(ConcreteSyntaxTree.NodeType.INEQUALITY_EXPRESSION, "inequalityExpression", TokenType.TOKEN_INEQUALITY), Operator.INEQUALITY));
        binaryExpressionRules.put(TokenType.TOKEN_BITWISE_AND, ParseRule.infixRule(ParseRule.ParseFn.binary(ConcreteSyntaxTree.NodeType.BITWISE_AND_EXPRESSION, "bitwiseAndExpression", TokenType.TOKEN_BITWISE_AND), Operator.BITWISE_AND));
        binaryExpressionRules.put(TokenType.TOKEN_BITWISE_EXCL_OR, ParseRule.infixRule(ParseRule.ParseFn.binary(ConcreteSyntaxTree.NodeType.BITWISE_XOR_EXPRESSION, "bitwiseXorExpression", TokenType.TOKEN_BITWISE_EXCL_OR), Operator.BITWISE_XOR));
        binaryExpressionRules.put(TokenType.TOKEN_BITWISE_OR, ParseRule.infixRule(ParseRule.ParseFn.binary(ConcreteSyntaxTree.NodeType.BITWISE_OR_EXPRESSION,"bitwiseOrExpression", TokenType.TOKEN_BITWISE_OR), Operator.BITWISE_OR));
        binaryExpressionRules.put(TokenType.TOKEN_LOGICAL_AND, ParseRule.infixRule(ParseRule.ParseFn.binary(ConcreteSyntaxTree.NodeType.LOGICAL_AND_EXPRESSION, "logicalAndExpression", TokenType.TOKEN_LOGICAL_AND), Operator.LOGICAL_AND));
        binaryExpressionRules.put(TokenType.TOKEN_LOGICAL_OR, ParseRule.infixRule(ParseRule.ParseFn.binary(ConcreteSyntaxTree.NodeType.LOGICAL_OR_EXPRESSION, "logicalOrExpression", TokenType.TOKEN_LOGICAL_OR), Operator.LOGICAL_OR));
    }
}
