package com.github.razorapid.morpheus.lang.lexer;

import com.github.razorapid.morpheus.lang.TokenType;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_ASSIGNMENT;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_BITWISE_AND;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_BITWISE_EXCL_OR;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_BITWISE_OR;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_COLON;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_COMPLEMENT;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_DEC;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_DIVIDE;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_DOLLAR;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_DOUBLE_COLON;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_EOL;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_EQUALITY;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_FLOAT;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_GREATER_THAN;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_GREATER_THAN_OR_EQUAL;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_IDENTIFIER;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_INC;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_INEQUALITY;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_INTEGER;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_LEFT_BRACES;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_LEFT_BRACKET;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_LEFT_SQUARE_BRACKET;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_LESS_THAN;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_LESS_THAN_OR_EQUAL;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_LISTENER;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_LOGICAL_AND;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_LOGICAL_OR;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_MINUS;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_MINUS_EQUALS;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_MULTIPLY;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_NEG;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_NOT;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_PERCENTAGE;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_PERIOD;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_PLUS;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_PLUS_EQUALS;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_POS;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_RIGHT_BRACES;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_RIGHT_BRACKET;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_RIGHT_SQUARE_BRACKET;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_SEMICOLON;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_STRING;
import static com.github.razorapid.morpheus.lang.lexer.LexerStateName.BLOCK_COMMENT;
import static com.github.razorapid.morpheus.lang.lexer.LexerStateName.FIELD;
import static com.github.razorapid.morpheus.lang.lexer.LexerStateName.IDENTIFIER;

@RequiredArgsConstructor
class BeginState implements LexerState {
    private static final Set<Character> NEW_LINE = Set.of(
        '\n'
    );
    private static final Set<Character> WHITE_SPACE = Set.of(
        ' ', '\t', '\r', '\n', '\f'
    );
    private static final Set<Character> STRING_TERMINATORS = Set.of(
        ' ', '\t', '\r', '\n', '!', '%', '&', '*', '/', '<', '>',
        '^', '|', '~', '(', ')', ',', ':', ';', '[', ']', '{', '}',
        '+', '-', '='
    );
    private static final Set<Character> KEYWORD_TERMINATORS = Set.of(
        ' ', '\t', '\r', '\n', '\f',
        '(', ')', '[', ']', '{', '}', ':', ';',
        '=', '/', '-', '+', '*', '%', '!', '^', '|', '&', '<', '>', '~',
        ',', '.', '\\',

        '$', '@', '#', '`', '"', '\'', '?'
    );
    private static final Set<Character> IDENTIFIER_TERMINATORS = Set.of(
        ' ', '\t', '\r', '\n', '\f',
        '(', ')', '[', ']', '{', '}', ':', ';'
    );
    private static final Set<Character> VARIABLE_IDENTIFIER_TERMINATORS = Set.of(
        ' ', '\t', '\r', '\n', '\f',
        '(', ')', '[', ']', '{', '}', ':', ';',
        '=', '/', '+', '-', '*', '%', '!', '^', '|', '&', '<', '>', '~',
        ',', '.', '\\'
    );
    private static final Set<Character> LISTENER_TERMINATORS = Set.of( //identifier can start with @ # ` \ " ' , ?
        ' ', '\t', '\r', '\n', '\f', // whitespace
        '(', ')', '{', '}', '[', ']', ':', ';',
        '=', '!', '%', '^', '*', '-', '+', '~', '|', '&', '/', '<', '>',
        ',', '.',  '\\',

        '$', '@', '#', '`', '"', '\'', '?'
    );
    private static final Set<Character> NUMBER_TERMINATORS = Set.of( // identifiers: . @ # $ _ " ' ` ? \
        ' ', '\t', '\r', '\n', // whitespace
        '(', ')', '{', '}', '[', ']', ':', ';',
        '=', '/', '+', '-', '*', '%', '!', '^', '|', '&', '<', '>', '~',
        ','
    );
    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();
    static {
        KEYWORDS.put("case", TokenType.TOKEN_CASE);
        KEYWORDS.put("if", TokenType.TOKEN_IF);
        KEYWORDS.put("else", TokenType.TOKEN_ELSE);
        KEYWORDS.put("while", TokenType.TOKEN_WHILE);
        KEYWORDS.put("for", TokenType.TOKEN_FOR);
        KEYWORDS.put("try", TokenType.TOKEN_TRY);
        KEYWORDS.put("catch", TokenType.TOKEN_CATCH);
        KEYWORDS.put("switch", TokenType.TOKEN_SWITCH);
        KEYWORDS.put("break", TokenType.TOKEN_BREAK);
        KEYWORDS.put("continue", TokenType.TOKEN_CONTINUE);
        KEYWORDS.put("NULL", TokenType.TOKEN_NULL);
        KEYWORDS.put("NIL", TokenType.TOKEN_NIL);
        KEYWORDS.put("size", TokenType.TOKEN_SIZE);
        KEYWORDS.put("end", TokenType.TOKEN_END);
        KEYWORDS.put("makeArray", TokenType.TOKEN_MAKEARRAY);
        KEYWORDS.put("makearray", TokenType.TOKEN_MAKEARRAY);
        KEYWORDS.put("endArray", TokenType.TOKEN_ENDARRAY);
        KEYWORDS.put("endarray", TokenType.TOKEN_ENDARRAY);
    }

    private static final Set<String> LISTENER_TYPES = Set.of(
        "game", "level", "local", "parm", "self", "group"
    );

    private final Lexer lexer;

    @Override
    public Lexer lexer() {
        return lexer;
    }

    @Override
    public MatchedToken nextToken() {
        char c = next();

        MatchedToken token = MatchedToken.notMatched();
        switch (c) {
            // skip carriage return
            case '\r': break;

            // count multiple new lines as one
            case '\n': {
                if (prevToken() != TOKEN_EOL) {
                    token = matched(TOKEN_EOL);
                }
                caret().newLine();
                break;
            }

            // single character cases
            case ';': { token = matched(TOKEN_SEMICOLON); break; }
            case '$': { token = matched(TOKEN_DOLLAR); break; }
            case '~': { token = matched(TOKEN_COMPLEMENT); break; }
            case '%': { token = matched(TOKEN_PERCENTAGE); break; }
            case '^': { token = matched(TOKEN_BITWISE_EXCL_OR); break; }
            case '(': { token = matched(TOKEN_LEFT_BRACKET); break; }
            case ')': { token = matched(TOKEN_RIGHT_BRACKET); break; }
            case '[': { token = matched(TOKEN_LEFT_SQUARE_BRACKET); break; }
            case ']': { token = matched(TOKEN_RIGHT_SQUARE_BRACKET); break; }
            case '{': { token = matched(TOKEN_LEFT_BRACES); break; }
            case '}': { token = matched(TOKEN_RIGHT_BRACES); break; }

            // multi character cases
            case ':': { token = matched(match(':') ? TOKEN_DOUBLE_COLON : TOKEN_COLON); break; }
            case '|': { token = matched(match('|') ? TOKEN_LOGICAL_OR : TOKEN_BITWISE_OR); break; }
            case '&': { token = matched(match('&') ? TOKEN_LOGICAL_AND : TOKEN_BITWISE_AND); break; }
            case '=': { token = matched(match('=') ? TOKEN_EQUALITY : TOKEN_ASSIGNMENT); break; }
            case '!': { token = matched(match('=') ? TOKEN_INEQUALITY : TOKEN_NOT); break; }
            case '<': { token = matched(match('=') ? TOKEN_LESS_THAN_OR_EQUAL : TOKEN_LESS_THAN); break; }
            case '>': { token = matched(match('=') ? TOKEN_GREATER_THAN_OR_EQUAL : TOKEN_GREATER_THAN); break; }
            case '+': {
                if (match('=')) {
                    token = matched(TOKEN_PLUS_EQUALS);
                } else if (match('+')) {
                    token = matched(TOKEN_INC);
                } else {
                    token = matched(TOKEN_PLUS);
                }
                break;
            }
            case '-': {
                if (match('=')) {
                    token = matched(TOKEN_MINUS_EQUALS);
                } else if (match('-')) {
                    token = matched(TOKEN_DEC);
                } else {
                    token = matched(TOKEN_MINUS);
                }
                break;
            }

            case '\t':
            case ' ': {
                if (peek() == '+' && (!WHITE_SPACE.contains(peekNext()) && peekNext() != '+' && peekNext() != '=' && peekNext() != '\0')) {
                    next();
                    token = matched(TOKEN_POS);
                } else if (peek() == '-' && (!WHITE_SPACE.contains(peekNext()) && peekNext() != '-' && peekNext() != '=' && peekNext() != '\0')) {
                    next();
                    token = matched(TOKEN_NEG);
                }
                break;
            }
            case '*': {
                if (match('/')) {
                    token = error("\'*/\' found outside of comment");
                    break;
                }
                token = matched(TOKEN_MULTIPLY); break;
            }
            case '.': {
                token = tryMatchFloat();
                if (token.isNotMatched()) {
                    token = matched(TOKEN_PERIOD);
                }
                break;
            }
            case '/': {
                if (match('/')) { // Single line comment, eat up to the new line without it
                    while (peek() != '\n' && !isEOF()) next();
                } else if (match('*')) {
                    switchTo(BLOCK_COMMENT);
                } else {
                    token = matched(TOKEN_DIVIDE);
                }
                break;
            }
            case '@':
            case ',': {
                if (isScanningVariable()) {
                    switchTo(FIELD);
                } else {
                    switchTo(IDENTIFIER);
                }
                break;
            }
            case '"': {
                token = tryMatchString();
                if (token.isNotMatched()) {
                    token = matchIdentifier(false);
                }
                break;
            }
            case '\\': { // Multiline break
                if (match('\n')) {
                    caret().newLine();
                    break;
                }
                if (peek() == '\r' && peekNext() == '\n') {
                    next();
                    next();
                    caret().newLine();
                    break;
                }
                //fallthrough
            }
            default: {
                if (Character.isDigit(c)) {
                    token = tryMatchNumber();
                    if (token.isNotMatched()) {
                        token = matchIdentifier(false);
                    }
                    break;
                }

                if (c == '#' || c == '`' || c == '\\' || c == '\'' || c == '?' || c == '_') {
                    token = matchIdentifier(false);
                    break;
                }

                token = tryMatchListener(c);
                if (token.isMatched()) {
                    break;
                }

                token = matchIdentifier(true);
                break;
            }
        }
        return token;
    }

    private MatchedToken tryMatchString() {
        int pos = currentPos();
        while (!NEW_LINE.contains(peek(pos)) && !isEOF(pos)) {
            if (peek(pos) == '"' && peek(pos - 1) != '\\' && STRING_TERMINATORS.contains(peek(pos + 1))) {
                currentPos(pos + 1);
                return matched(TOKEN_STRING);
            }
            pos++;
        }
        return MatchedToken.notMatched();
    }

    private MatchedToken tryMatchNumber() {
        int digits = currentPos();
        while (Character.isDigit(peek(digits))) digits++;
        if (isEOF(digits) || NUMBER_TERMINATORS.contains(peek(digits))) {
            currentPos(digits);
            return matched(TOKEN_INTEGER);
        }

        if (peek(digits) == 'E') {
            int exponentPos = digits + 1;
            if (peek(exponentPos) == '+' || peek(exponentPos) == '-') { // optional +- character, eg. 1.2E+1
                exponentPos++;
            }
            if (Character.isDigit(peek(exponentPos))) {
                while (Character.isDigit(peek(exponentPos))) exponentPos++;
                if (isEOF(exponentPos) || NUMBER_TERMINATORS.contains(peek(exponentPos))) {
                    currentPos(exponentPos);
                    return matched(TOKEN_FLOAT);
                }
            }
        }

        if (peek(digits) == '.' && Character.isDigit(peek(digits + 1))) {
            digits++;

            while (Character.isDigit(peek(digits))) digits++;
            if (isEOF(digits) || NUMBER_TERMINATORS.contains(peek(digits))) {
                currentPos(digits);
                return matched(TOKEN_FLOAT);
            }

            if (peek(digits) == 'E') {
                int exponentPos = digits + 1;
                if (peek(exponentPos) == '+' || peek(exponentPos) == '-') { // optional +- character, eg. 1.2E+1
                    exponentPos++;
                }
                if (Character.isDigit(peek(exponentPos))) {
                    while (Character.isDigit(peek(exponentPos))) exponentPos++;
                    if (isEOF(exponentPos) || NUMBER_TERMINATORS.contains(peek(exponentPos))) {
                        currentPos(exponentPos);
                        return matched(TOKEN_FLOAT);
                    }
                }
            }
        }

        return MatchedToken.notMatched();
    }

    private MatchedToken tryMatchFloat() {
        int pos = currentPos();
        int digits = pos;
        while (Character.isDigit(peek(digits))) digits++;
        if (digits > pos && (isEOF(digits) || NUMBER_TERMINATORS.contains(peek(digits)))) {
            currentPos(digits);
            return matched(TOKEN_FLOAT);
        }

        if (peek(digits) == 'E') {
            int exponentPos = digits + 1;
            if (peek(exponentPos) == '+' || peek(exponentPos) == '-') { // optional +- character, eg. .2E+1
                exponentPos++;
            }
            if (Character.isDigit(peek(exponentPos))) {
                while (Character.isDigit(peek(exponentPos))) exponentPos++;
                if (isEOF(exponentPos) || NUMBER_TERMINATORS.contains(peek(exponentPos))) {
                    currentPos(exponentPos);
                    return matched(TOKEN_FLOAT);
                }
            }
        }
        return MatchedToken.notMatched();
    }

    private MatchedToken tryMatchListener(char c) {
        for (String listener : LISTENER_TYPES) {
            char[] chars = listener.toCharArray();
            if (c == chars[0]) {
                boolean isListenerCandidate = true;
                int pos = currentPos();
                for (int i = 1; i < chars.length; i++) {
                    if (peek(pos) != chars[i]) {
                        isListenerCandidate = false;
                        break;
                    }
                    pos++;
                }

                if (isListenerCandidate && (isEOF(pos) || LISTENER_TERMINATORS.contains(peek(pos)))) {
                    currentPos(pos);
                    return matched(TOKEN_LISTENER);
                }
            }
        }
        return MatchedToken.notMatched();
    }

    private MatchedToken matchIdentifier(boolean lookupKeywords) {
        MatchedToken token = MatchedToken.notMatched();
        boolean matchedKeyword = false;
        while (!isEOF() && (
            (!isScanningVariable() && !IDENTIFIER_TERMINATORS.contains(peek())) ||
                (isScanningVariable() && !VARIABLE_IDENTIFIER_TERMINATORS.contains(peek()))
        )) {
            next();

            String tokenString = sourceString(tokenStartPos(), currentPos());
            if (lookupKeywords && KEYWORDS.containsKey(tokenString)) {
                if (KEYWORD_TERMINATORS.contains(peek())) {
                    matchedKeyword = true;
                    token = matched(KEYWORDS.getOrDefault(tokenString, TOKEN_IDENTIFIER));
                    break;
                }
            }
        }

        if (!matchedKeyword) {
            token = matched(TOKEN_IDENTIFIER);
        }

        return token;
    }

    private boolean isScanningVariable() {
        return prevToken() == TOKEN_PERIOD || prevToken() == TOKEN_DOLLAR;
    }
}
