package com.github.razorapid.morpheus.lang;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Lexer {

    private static final Set<Character> KEYWORD_TERMINATORS = Set.of(
            ' ', '\t', '\r', '\n', '\f',
            '(', ')', '[', ']', '{', '}', ':', ';'
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

    private final Tokens tokens = new Tokens();
    private String source;
    private long startPos = 0;
    private long currentPos = 0;
    private long currentLine = 1;
    private long currentCol = 1;
    private boolean currentLineHasStatement = false;
    private boolean scanningVariable = false;

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
                if (peek() == '+' && (!WHITE_SPACE.contains(peekNext()) && peekNext() != '=' && peekNext() != '\0')) {
                    next();
                    addToken(TokenType.TOKEN_POS);
                } else if (peek() == '-' && (!WHITE_SPACE.contains(peekNext()) && peekNext() != '=' && peekNext() != '\0')) {
                    next();
                    addToken(TokenType.TOKEN_NEG);
                }
                break;
            }
            case '\r':
            case '\t':
                break;
            case '\n': {
                if (currentLineHasStatement) {
                    currentLineHasStatement = false;
                    addToken(TokenType.TOKEN_EOL);
                }
                currentLine++;
                currentCol = 1;
                break;
            }
            // braces and brackets
            case '(': addToken(TokenType.TOKEN_LEFT_BRACKET); break;
            case ')': addToken(TokenType.TOKEN_RIGHT_BRACKET); break;
            case '[': addToken(TokenType.TOKEN_LEFT_SQUARE_BRACKET); break;
            case ']': addToken(TokenType.TOKEN_RIGHT_SQUARE_BRACKET); break;
            case '{': addToken(TokenType.TOKEN_LEFT_BRACES); break;
            case '}': addToken(TokenType.TOKEN_RIGHT_BRACES); break;

            // single character operators
            case ';': addToken(TokenType.TOKEN_SEMICOLON); break;
            case '$': {
                addToken(TokenType.TOKEN_DOLLAR);
                scanningVariable = true;
                break;
            }
            case '~': addToken(TokenType.TOKEN_COMPLEMENT); break;
            case '%': addToken(TokenType.TOKEN_PERCENTAGE); break;
            case '*': addToken(TokenType.TOKEN_MULTIPLY); break;
            case '^': addToken(TokenType.TOKEN_BITWISE_EXCL_OR); break;

            // multi character operators
            case ':': addToken(match(':') ? TokenType.TOKEN_DOUBLE_COLON : TokenType.TOKEN_COLON); break;
            case '|': addToken(match('|') ? TokenType.TOKEN_LOGICAL_OR : TokenType.TOKEN_BITWISE_OR); break;
            case '&': addToken(match('&') ? TokenType.TOKEN_LOGICAL_AND : TokenType.TOKEN_BITWISE_AND); break;
            case '=': addToken(match('=') ? TokenType.TOKEN_EQUALITY : TokenType.TOKEN_ASSIGNMENT); break;
            case '!': addToken(match('=') ? TokenType.TOKEN_INEQUALITY : TokenType.TOKEN_NOT); break;
            case '<': addToken(match('=') ? TokenType.TOKEN_LESS_THAN_OR_EQUAL : TokenType.TOKEN_LESS_THAN); break;
            case '>': addToken(match('=') ? TokenType.TOKEN_GREATER_THAN_OR_EQUAL : TokenType.TOKEN_GREATER_THAN); break;
            case '-': {
                if (match('=')) {
                    addToken(TokenType.TOKEN_MINUS_EQUALS);
                } else if (match('-')) {
                    addToken(TokenType.TOKEN_DEC);
                } else {
                    addToken(TokenType.TOKEN_MINUS);
                }
                break;
            }
            case '+': {
                if (match('=')) {
                    addToken(TokenType.TOKEN_PLUS_EQUALS);
                } else if (match('+')) {
                    addToken(TokenType.TOKEN_INC);
                } else {
                    addToken(TokenType.TOKEN_PLUS);
                }
                break;
            }
            case '.': {
                boolean isFloat = tryMatchFloat();
                if (!isFloat) {
                    addToken(TokenType.TOKEN_PERIOD);
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
                            addToken(TokenType.TOKEN_EOL);
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
                                addToken(TokenType.TOKEN_EOL);
                            }
                            currentLine++;
                            currentCol = 1;
                        }
                    }
                    next();
                    next();
                } else {
                    addToken(TokenType.TOKEN_DIVIDE);
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
                    if (peek() == '.') scanningVariable = true;
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
            addToken(TokenType.TOKEN_INTEGER);
            return true;
        }

        if (!isEOF(pos) && peek(pos) == '.') {
            long decimalPos = pos + 1;
            while (Character.isDigit(peek(decimalPos))) decimalPos++;
            if (decimalPos > pos + 1 && (isEOF(decimalPos) || NUMBER_TERMINATORS.contains(peek(decimalPos)))) {
                currentPos(decimalPos);
                addToken(TokenType.TOKEN_FLOAT);
                return true;
            }
        }

        return false;
    }

    private boolean tryMatchFloat() {
        long pos = currentPos;
        while (Character.isDigit(peek(pos))) pos++;
        if (pos != currentPos && (isEOF(pos) || WHITE_SPACE.contains(peek(pos)))) {
            currentPos(pos);
            addToken(TokenType.TOKEN_FLOAT);
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
                    addToken(TokenType.TOKEN_LISTENER);
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
                addToken(TokenType.TOKEN_STRING);
                return true;
            }
            pos++;
        }
        return false;
    }

    private void matchIdentifier(boolean lookupKeywords) {
        boolean matchedKeyword = false;
        while (!isEOF() && (
                (!scanningVariable && !IDENTIFIER_TERMINATORS.contains(peek())) ||
                (scanningVariable && !VARIABLE_IDENTIFIER_TERMINATORS.contains(peek()))
        )) {
            next();

            if (lookupKeywords && KEYWORDS.containsKey(source.substring((int) startPos, (int) currentPos))) {
                if (!Character.isLetterOrDigit(peek()) || Character.isWhitespace(peek())) {
                    matchedKeyword = true;
                    addToken(KEYWORDS.getOrDefault(source.substring((int) startPos, (int) currentPos), TokenType.TOKEN_IDENTIFIER));
                    break;
                }
            }
        }

        if (scanningVariable && peek() != '.') {
            scanningVariable = false;
        }

        if (!matchedKeyword) {
            addToken(TokenType.TOKEN_IDENTIFIER);
        }
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
        if (type != TokenType.TOKEN_EOL) {
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
