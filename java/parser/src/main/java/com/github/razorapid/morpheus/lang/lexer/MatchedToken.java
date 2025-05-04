package com.github.razorapid.morpheus.lang.lexer;

import com.github.razorapid.morpheus.lang.Token;
import lombok.Value;

@Value
class MatchedToken {
    boolean matched;
    Token val;

    boolean isMatched() {
        return matched;
    }

    boolean isNotMatched() {
        return !isMatched();
    }

    static MatchedToken matched(Token token) {
        return new MatchedToken(true, token);
    }

    static MatchedToken notMatched() {
        return new MatchedToken(false, null);
    }
}
