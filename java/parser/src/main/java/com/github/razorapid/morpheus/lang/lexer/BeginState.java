package com.github.razorapid.morpheus.lang.lexer;

import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_ASSIGNMENT;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_BITWISE_AND;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_BITWISE_EXCL_OR;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_BITWISE_OR;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_BREAK;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_CASE;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_CATCH;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_COLON;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_COMPLEMENT;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_CONTINUE;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_DEC;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_DIVIDE;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_DOLLAR;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_DOUBLE_COLON;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_ELSE;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_END;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_ENDARRAY;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_EOL;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_EQUALITY;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_FLOAT;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_FOR;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_GREATER_THAN;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_GREATER_THAN_OR_EQUAL;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_IDENTIFIER;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_IF;
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
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_MAKEARRAY;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_MINUS;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_MINUS_EQUALS;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_MULTIPLY;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_NEG;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_NIL;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_NOT;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_NULL;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_PERCENTAGE;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_PERIOD;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_PLUS;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_PLUS_EQUALS;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_POS;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_RIGHT_BRACES;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_RIGHT_BRACKET;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_RIGHT_SQUARE_BRACKET;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_SEMICOLON;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_SIZE;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_STRING;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_SWITCH;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_TRY;
import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_WHILE;
import static com.github.razorapid.morpheus.lang.lexer.LexerStateName.BLOCK_COMMENT;
import static com.github.razorapid.morpheus.lang.lexer.LexerStateName.ESCAPED_FIELD;
import static com.github.razorapid.morpheus.lang.lexer.LexerStateName.ESCAPED_IDENTIFIER;
import static com.github.razorapid.morpheus.lang.lexer.LexerStateName.FIELD;
import static com.github.razorapid.morpheus.lang.lexer.LexerStateName.IDENTIFIER;
import static com.github.razorapid.morpheus.lang.lexer.LexerStateName.SKIP_TILL_EOL;
import static com.github.razorapid.morpheus.lang.lexer.MatchedToken.notMatched;

@RequiredArgsConstructor
class BeginState implements LexerState {
    private static final Set<Character> OTHER = nonPrintableASCIICharacters();
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
    // Contains MISC
    private static final Set<Character> IDENTIFIER_TERMINATORS = sets(Set.of(
        ' ', '\t', '\r', '\n', '\f',
        '$', '@', '\\', '!', '%', '&', '*', '/',
        '<', '>', '(', ')', '[', ']', '{', '}',
        '^', '|', '~', ',', ':', ';', '+', '-', '=', '.',

        '#', '\'', '?', '`'
    ), OTHER);
    private static final Set<Character> NUMBER_TERMINATORS = Set.of(
        ' ', '\t', '\r', '\n',
        '(', ')', '{', '}', '[', ']', ':', ';',
        '=', '/', '+', '-', '*', '%', '!', '^', '|', '&', '<', '>', '~',
        ','
    );
    private static final Set<Character> ESCAPED_IDENTIFIER_TERMINATORS = Set.of(
        ' ', '\t', '(', ')', ':', ';', '[', '{', ',', ']', '}'
    );
    private static final Set<Character> ESCAPED_FIELD_TERMINATORS = Set.of(
        '!', '%', '&', '*', '+', '-', '.', '/', '<', '>', '\\', '|',
        '=', '^', '~'
    );
    private static final Keywords KEYWORDS = new Keywords();

    static {
        KEYWORDS.add("case", TOKEN_CASE);
        KEYWORDS.add("if", TOKEN_IF);
        KEYWORDS.add("else", TOKEN_ELSE);
        KEYWORDS.add("while", TOKEN_WHILE);
        KEYWORDS.add("for", TOKEN_FOR);
        KEYWORDS.add("try", TOKEN_TRY);
        KEYWORDS.add("catch", TOKEN_CATCH);
        KEYWORDS.add("switch", TOKEN_SWITCH);
        KEYWORDS.add("break", TOKEN_BREAK);
        KEYWORDS.add("continue", TOKEN_CONTINUE);
        KEYWORDS.add("NULL", TOKEN_NULL);
        KEYWORDS.add("NIL", TOKEN_NIL);
        KEYWORDS.add("size", TOKEN_SIZE);
        KEYWORDS.add("end", TOKEN_END);
        KEYWORDS.add("makeArray", TOKEN_MAKEARRAY);
        KEYWORDS.add("makearray", TOKEN_MAKEARRAY);
        KEYWORDS.add("endArray", TOKEN_ENDARRAY);
        KEYWORDS.add("endarray", TOKEN_ENDARRAY);

        KEYWORDS.add("game", TOKEN_LISTENER);
        KEYWORDS.add("level", TOKEN_LISTENER);
        KEYWORDS.add("local", TOKEN_LISTENER);
        KEYWORDS.add("parm", TOKEN_LISTENER);
        KEYWORDS.add("self", TOKEN_LISTENER);
        KEYWORDS.add("group", TOKEN_LISTENER);
    }

    private final Lexer lexer;

    @Override
    public Lexer lexer() {
        return lexer;
    }

    @Override
    public MatchedToken nextToken() {
        char c = next();

        MatchedToken token = notMatched();
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
                    switchTo(SKIP_TILL_EOL);
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
                    token = matchIdentifier();
                }
                break;
            }
            case '\\': { // Multiline break
                if (match('\n')) {
                    caret().newLine();
                    break;
                } else if (peek() == '\r') {
                    if (peekNext() == '\n') {
                        next();
                        next();
                        caret().newLine();
                    } else {
                        next();
                        token = matchedEscaped(TOKEN_IDENTIFIER);
                    }
                    break;
                } else if (ESCAPED_IDENTIFIER_TERMINATORS.contains(peek())) { // \n and \r\n are treated separately in this case
                    token = matchedEscaped(TOKEN_IDENTIFIER);
                    break;
                } else if (ESCAPED_FIELD_TERMINATORS.contains(peek())) {
                    if (isScanningVariable()) {
                        token = matchedEscaped(TOKEN_IDENTIFIER);
                        break;
                    } else {
                        switchTo(ESCAPED_IDENTIFIER);
                        break;
                    }
                } else {
                    if (isScanningVariable()) {
                        switchTo(ESCAPED_FIELD);
                        break;
                    } else {
                        switchTo(ESCAPED_IDENTIFIER);
                        break;
                    }
                }
                //fallthrough
            }
            default: {
                if (Character.isDigit(c)) {
                    token = tryMatchNumber();
                    if (token.isMatched()) {
                        break;
                    }
                }

                if (KEYWORDS.startWith(c)) {
                    token = tryMatchKeyword(c);
                    if (token.isMatched()) {
                        break;
                    }
                }

                // when nothing else matched, match identifier
                matchIdentifier();
                break;
            }
        }
        return token;
    }

    private MatchedToken tryMatchKeyword(char c) {
        KEYWORDS.reset();
        if (!KEYWORDS.next(c)) {
            return notMatched();
        }

        int pos = currentPos();
        while(!isEOF(pos) && !IDENTIFIER_TERMINATORS.contains(peek(pos))) {
            if (!KEYWORDS.next(peek(pos))) {
                return notMatched();
            }
            pos++;
        }
        if (KEYWORDS.matchedToken() == null) {
            return notMatched();
        }

        currentPos(pos);
        return matched(KEYWORDS.matchedToken());
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
        return notMatched();
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

        return notMatched();
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
        return notMatched();
    }

    private MatchedToken matchIdentifier() {
        // scan together with current character
        currentPos(currentPos() - 1);

        if (isScanningVariable()) {
            switchTo(FIELD);
        } else {
            switchTo(IDENTIFIER);
        }
        return notMatched();
    }

    private boolean isScanningVariable() {
        return prevToken() == TOKEN_PERIOD || prevToken() == TOKEN_DOLLAR;
    }

    private static Set<Character> nonPrintableASCIICharacters() {
        Set<Character> chars = new HashSet<>();
        for (char c = 0; c < 256; c++) {
            if (!isWhitespace(c) && !isPrintable(c)) {
                chars.add(c);
            }
        }
        return chars;
    }

    private static boolean isPrintable(char c) {
        return c > 32 && c < 127;
    }

    private static boolean isWhitespace(char c) {
        return c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == '\f';
    }

    @SafeVarargs
    private static <T> Set<T> sets(Set<T> set, Set<T> ...others) {
        Set<T> result = new HashSet<>(set);
        for (Set<T> s : others) {
            result.addAll(s);
        }
        return Collections.unmodifiableSet(result);
    }
}
