package com.github.razorapid.morpheus.lang.lexer;

import lombok.RequiredArgsConstructor;

import java.util.Set;

import static com.github.razorapid.morpheus.lang.TokenType.TOKEN_IDENTIFIER;
import static com.github.razorapid.morpheus.lang.lexer.LexerStateName.BEGIN;
import static com.github.razorapid.morpheus.lang.lexer.MatchedToken.notMatched;

@RequiredArgsConstructor
class IdentifierState implements LexerState {
    private static final Set<Character> NEW_LINE = Set.of(
        '\n'
    );
    private static final Set<Character> BAD_TOKEN_CHARS = Set.of(
        ' ', '\t', '\r', '(', ')', '[', ']', '{', '}',
        ':', ';', ','
    );
    private static final Set<Character> IDENTIFIER_TERMINATORS = Set.of(
        '\n', '\t', '\r', ' ', '(', ')', ',', ':', ';', '[', ']', '{', '}'
    );

    private final Lexer lexer;

    @Override
    public Lexer lexer() {
        return lexer;
    }

    @Override
    public MatchedToken nextToken() {
        if (NEW_LINE.contains(peek())) { // ignore the character that put us in Identifier state and continue
            next();
            return notMatched();
        } else if (BAD_TOKEN_CHARS.contains(peek())) {
            next();
            throw new IllegalStateException("bad token");
        }
        while (!isEOF() && !IDENTIFIER_TERMINATORS.contains(peek())) {
            next();
        }
        switchTo(BEGIN);
        return matched(TOKEN_IDENTIFIER);
    }
}
