package com.github.razorapid.morpheus.lang;

import lombok.Data;
import lombok.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

public class Lexer {

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
            ' ', '\t', '\r', '\n', '\f', // whitespace
            '(', ')', '{', '}', '[', ']', ':', ';',
            '=', '/', '+', '-', '*', '%', '!', '^', '|', '&', '<', '>', '~',
            ','
    );
    private static final Set<Character> WHITE_SPACE = Set.of(
            ' ', '\t', '\r', '\n', '\f'
    );
    private static final Set<Character> NEW_LINE = Set.of(
            '\n'
    );
    private final Map<String, TokenType> KEYWORDS = new HashMap<>();

    private static final Set<String> LISTENER_TYPES = Set.of(
            "game", "level", "local", "parm", "self", "group"
    );

    private static final Set<Character> VARIABLE_SCANNING_TERMINATORS = Set.of(
        ':', ';',
        '=', '/', '+', '-', '*', '%', '!', '^', '|', '&', '<', '>', '~',
        ',', '\\'
    );

    @Data
    private static class Scope {
        boolean scanningVariable = false;

        boolean isScanningVariable() {
            return scanningVariable;
        }

        void scanningVariable(boolean val) {
            scanningVariable = val;
        }
    }

    @Value
    private static class Scopes {
        List<Scope> scope = new ArrayList<>();

        void push(Scope newScope) {
            scope.add(newScope);
        }

        Optional<Scope> pop() {
            if (!scope.isEmpty()) {
                return Optional.of(scope.remove(stackTop()));
            }
            return Optional.empty();
        }

        Scope currentScope() {
            return scope.isEmpty() ? new Scope() : scope.get(stackTop());
        }

        private int stackTop() {
            return scope.size() - 1;
        }
    }

    private final Scopes scopes = new Scopes();
    private final Tokens tokens = new Tokens();
    private String source;
    private long startPos = 0;
    private long currentPos = 0;
    private long currentLine = 1;
    private long currentCol = 1;
    private boolean currentLineHasStatement = false;

    public Lexer(Source script) {
        this.source = script != null ? script.source() : null;

        this.KEYWORDS.put("case", TokenType.TOKEN_CASE);
        this.KEYWORDS.put("if", TokenType.TOKEN_IF);
        this.KEYWORDS.put("else", TokenType.TOKEN_ELSE);
        this.KEYWORDS.put("while", TokenType.TOKEN_WHILE);
        this.KEYWORDS.put("for", TokenType.TOKEN_FOR);
        this.KEYWORDS.put("try", TokenType.TOKEN_TRY);
        this.KEYWORDS.put("catch", TokenType.TOKEN_CATCH);
        this.KEYWORDS.put("switch", TokenType.TOKEN_SWITCH);
        this.KEYWORDS.put("break", TokenType.TOKEN_BREAK);
        this.KEYWORDS.put("continue", TokenType.TOKEN_CONTINUE);
        this.KEYWORDS.put("NULL", TokenType.TOKEN_NULL);
        this.KEYWORDS.put("NIL", TokenType.TOKEN_NIL);
        this.KEYWORDS.put("size", TokenType.TOKEN_SIZE);
        this.KEYWORDS.put("end", TokenType.TOKEN_END);
        this.KEYWORDS.put("makeArray", TokenType.TOKEN_MAKEARRAY);
        this.KEYWORDS.put("makearray", TokenType.TOKEN_MAKEARRAY);
        this.KEYWORDS.put("endArray", TokenType.TOKEN_ENDARRAY);
        this.KEYWORDS.put("endarray", TokenType.TOKEN_ENDARRAY);

        pushScope();
    }

    public Optional<Tokens> scan() {
        if (source == null) return Optional.empty();
        // make sure source end with new line
        if (!source.endsWith("\n")) {
            source += "\n";
        }

        while (!isEOF()) {
            startPos = currentPos;
            scanToken();
        }

        return Optional.of(tokens);
    }

    private void scanToken() {
        char c = next();

        switch (c) {
            case ' ': {
                if (peek() == '+' && (!WHITE_SPACE.contains(peekNext()) && peekNext() != '+' && peekNext() != '=' && peekNext() != '\0')) {
                    next();
                    addToken(TOKEN_POS);
                } else if (peek() == '-' && (!WHITE_SPACE.contains(peekNext()) && peekNext() != '-' && peekNext() != '=' && peekNext() != '\0')) {
                    next();
                    addToken(TOKEN_NEG);
                }
                break;
            }
            case '\r':
            case '\t':
                break;
            case '\n': {
                if (currentLineHasStatement) {
                    currentLineHasStatement = false;
                    addToken(TOKEN_EOL);
                }
                currentLine++;
                currentCol = 1;
                break;
            }
            // braces and brackets
            case '(': {
                addToken(TOKEN_LEFT_BRACKET);
                pushScope();
                break;
            }
            case ')': {
                addToken(TOKEN_RIGHT_BRACKET);
                popScope();
                break;
            }
            case '[': {
                addToken(TOKEN_LEFT_SQUARE_BRACKET);
                pushScope();
                break;
            }
            case ']': {
                addToken(TOKEN_RIGHT_SQUARE_BRACKET);
                popScope();
                break;
            }
            case '{': {
                addToken(TOKEN_LEFT_BRACES);
                pushScope();
                break;
            }
            case '}': {
                addToken(TOKEN_RIGHT_BRACES);
                popScope();
                break;
            }

            // single character operators
            case ';': addToken(TOKEN_SEMICOLON); break;
            case '$': {
                addToken(TOKEN_DOLLAR);
                startScanningVariable();
                break;
            }
            case '~': addToken(TOKEN_COMPLEMENT); break;
            case '%': addToken(TOKEN_PERCENTAGE); break;
            case '*': addToken(TOKEN_MULTIPLY); break;
            case '^': addToken(TOKEN_BITWISE_EXCL_OR); break;

            // multi character operators
            case ':': addToken(match(':') ? TOKEN_DOUBLE_COLON : TOKEN_COLON); break;
            case '|': addToken(match('|') ? TOKEN_LOGICAL_OR : TOKEN_BITWISE_OR); break;
            case '&': addToken(match('&') ? TOKEN_LOGICAL_AND : TOKEN_BITWISE_AND); break;
            case '=': addToken(match('=') ? TOKEN_EQUALITY : TOKEN_ASSIGNMENT); break;
            case '!': addToken(match('=') ? TOKEN_INEQUALITY : TOKEN_NOT); break;
            case '<': addToken(match('=') ? TOKEN_LESS_THAN_OR_EQUAL : TOKEN_LESS_THAN); break;
            case '>': addToken(match('=') ? TOKEN_GREATER_THAN_OR_EQUAL : TOKEN_GREATER_THAN); break;
            case '-': {
                if (match('=')) {
                    addToken(TOKEN_MINUS_EQUALS);
                } else if (match('-')) {
                    addToken(TOKEN_DEC);
                } else {
                    addToken(TOKEN_MINUS);
                }
                break;
            }
            case '+': {
                if (match('=')) {
                    addToken(TOKEN_PLUS_EQUALS);
                } else if (match('+')) {
                    addToken(TOKEN_INC);
                } else {
                    addToken(TOKEN_PLUS);
                }
                break;
            }
            case '.': {
                boolean isFloat = tryMatchFloat();
                if (!isFloat) {
                    addToken(TOKEN_PERIOD);
                }
                break;
            }
            case '/': {

                if (match('/')) {
                    while (peek() != '\n' && !isEOF()) next();
                    if (peek() == '\n') {
                        next();
                        if (currentLineHasStatement) {
                            currentLineHasStatement = false;
                            startPos = currentPos - 1;
                            addToken(TOKEN_EOL);
                        }
                        currentLine++;
                        currentCol = 1;
                    }
                } else if (match('*')) {
                    while (!(peek() == '*' && peekNext() == '/') && !isEOF()) {
                        char n = next();
                        if (n == '\n') {
                            if (currentLineHasStatement) {
                                currentLineHasStatement = false;
                                startPos = currentPos - 1;
                                addToken(TOKEN_EOL);
                            }
                            currentLine++;
                            currentCol = 1;
                        }
                    }
                    next();
                    next();
                } else {
                    addToken(TOKEN_DIVIDE);
                }

                break;
            }
            case '\\': {
                if (WHITE_SPACE.contains(peek())) {
                    // line break => ignore and continue line without emitting EOL token on new line
                    currentLineHasStatement = false;
                    break;
                }
                //fallthrough
            }
            default: {

                if (isScanningVariable() && !matchLastToken(TOKEN_DOLLAR, TOKEN_PERIOD)) {
                    stopScanningVariable();
                }

                if (c == '@' || c == '#' || c == '`' || c == '\\' || c == '\'' || c == ',' || c == '?' || c == '_') {
                    matchIdentifier(false);
                    break;
                }

                if (c == '"') {
                    boolean isString = tryMatchString();
                    if (!isString) {
                        matchIdentifier(false);
                    }
                    break;
                }

                if (Character.isDigit(c)) {
                    boolean isNumber = tryMatchNumber();
                    if (!isNumber) {
                        matchIdentifier(false);
                    }
                    break;
                }

                boolean isListener = tryMatchListener(c);
                if (isListener) {
                    if (peek() == '.') startScanningVariable();
                    break;
                }

                matchIdentifier(true);
                break;
            }
        }
    }

    private boolean tryMatchNumber() {
        long pos = currentPos;

        while (Character.isDigit(peek(pos))) pos++;
        if (isEOF(pos) || NUMBER_TERMINATORS.contains(peek(pos))) {
            currentPos(pos);
            addToken(TOKEN_INTEGER);
            return true;
        }

        if (!isEOF(pos) && peek(pos) == '.') {
            long decimalPos = pos + 1;
            while (Character.isDigit(peek(decimalPos))) decimalPos++;
            if (decimalPos > pos + 1 && (isEOF(decimalPos) || NUMBER_TERMINATORS.contains(peek(decimalPos)))) {
                currentPos(decimalPos);
                addToken(TOKEN_FLOAT);
                return true;
            }
        }

        return false;
    }

    private boolean tryMatchFloat() {
        long pos = currentPos;
        while (Character.isDigit(peek(pos))) pos++;
        if (pos != currentPos && (isEOF(pos) || NUMBER_TERMINATORS.contains(peek(pos)))) {
            currentPos(pos);
            addToken(TOKEN_FLOAT);
            return true;
        }
        return false;
    }

    private boolean tryMatchListener(char c) {
        for (String listener : LISTENER_TYPES) {
            char[] chars = listener.toCharArray();
            if (c == chars[0]) {
                boolean isListenerCandidate = true;
                long pos = currentPos;
                for (int i = 1; i < chars.length; i++) {
                    if (peek(pos) != chars[i]) {
                        isListenerCandidate = false;
                        break;
                    }
                    pos++;
                }

                if (isListenerCandidate && (isEOF(pos) || LISTENER_TERMINATORS.contains(peek(pos)))) {
                    currentPos(pos);
                    addToken(TOKEN_LISTENER);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean tryMatchString() {
        long pos = currentPos;
        while (!NEW_LINE.contains(peek(pos)) && !isEOF(pos)) {
            if (peek(pos) == '"' && peek(pos - 1) != '\\') {
                currentPos(pos + 1);
                addToken(TOKEN_STRING);
                return true;
            }
            pos++;
        }
        return false;
    }

    private void matchIdentifier(boolean lookupKeywords) {
        boolean matchedKeyword = false;
        while (!isEOF() && (
                (!isScanningVariable() && !IDENTIFIER_TERMINATORS.contains(peek())) ||
                (isScanningVariable() && !VARIABLE_IDENTIFIER_TERMINATORS.contains(peek()))
        )) {
            next();

            if (lookupKeywords && KEYWORDS.containsKey(source.substring((int) startPos, (int) currentPos))) {
                if (KEYWORD_TERMINATORS.contains(peek())) {
                    matchedKeyword = true;
                    addToken(KEYWORDS.getOrDefault(source.substring((int) startPos, (int) currentPos), TOKEN_IDENTIFIER));
                    break;
                }
            }
        }

        if (!matchedKeyword) {
            addToken(TOKEN_IDENTIFIER);
        }
    }

    private void pushScope() {
        scopes.push(new Scope());
    }

    private void popScope() {
        scopes.pop();
    }

    private void startScanningVariable() {
        scopes.currentScope().scanningVariable(true);
    }

    private void stopScanningVariable() {
        scopes.currentScope().scanningVariable(false);
    }

    private boolean isScanningVariable() {
        return scopes.currentScope().isScanningVariable();
    }

    private TokenType lastScannedToken() {
        if (tokens.list().isEmpty()) {
            return null;
        }
        return tokens.list().get(tokens.list().size() - 1).type();
    }

    private boolean matchLastToken(TokenType ...types) {
        if (lastScannedToken() == null) {
            return false;
        }

        TokenType lastTokenType = lastScannedToken();
        for (var type : types) {
            if (type == lastTokenType) {
                return true;
            }
        }
        return false;
    }

    private char peekNext() {
        return peek(currentPos + 1);
    }

    private char peek(long pos) {
        if (isEOF(pos)) return '\0';
        return source.charAt((int) pos);
    }

    private char peek() {
        return peek(currentPos);
    }

    private boolean match(char c) {
        if (isEOF()) return false;
        if (source.charAt((int) currentPos) != c) return false;

        currentPosInc();
        return true;
    }

    private void addToken(TokenType type) {
        tokens.add(type, source.substring((int) startPos, (int) currentPos), startPos, currentLine, currentCol - (currentPos - startPos));
        if (type != TOKEN_EOL) {
            currentLineHasStatement = true;
        }
    }

    private char next() {
        return source.charAt((int) currentPosInc());
    }

    private long currentPosInc() {
        var old = currentPos;
        currentCol++;
        currentPos++;
        return old;
    }

    private void currentPos(long pos) {
        currentCol += pos - currentPos;
        currentPos = pos;
    }

    private boolean isEOF() {
        return isEOF(currentPos);
    }

    private boolean isEOF(long pos) {
        return source.isEmpty() || pos >= source.length();
    }
}
