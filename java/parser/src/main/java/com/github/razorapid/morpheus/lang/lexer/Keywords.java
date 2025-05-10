package com.github.razorapid.morpheus.lang.lexer;

import com.github.razorapid.morpheus.lang.TokenType;
import lombok.RequiredArgsConstructor;

class Keywords {
    @RequiredArgsConstructor
    private static class Node {
        private final Node[] children = new Node[256];
        private TokenType val;

        boolean contains(char character) {
            return children[character] != null;
        }

        void add(char character) {
            children[character] = new Node();
        }

        Node child(char character) {
            return children[character];
        }

        void end(TokenType val) {
            this.val = val;
        }
    }

    private final Node root = new Node();
    private Node pos = root;

    void add(String keyword, TokenType token) {
        char[] characters = keyword.toCharArray();
        Node current = root;
        for (char character : characters) {
            if (!current.contains(character)) {
                current.add(character);
            }
            current = current.child(character);
        }
        current.end(token);
    }

    public boolean startWith(char c) {
        return root.child(c) != null;
    }

    TokenType find(String text) {
        char[] characters = text.toCharArray();
        Node current = root;
        for (char c : characters) {
            Node next = current.child(c);
            if (next == null) {
                return null;
            }
            current = next;
        }
        return current.val;
    }

    void reset() {
        pos = root;
    }

    boolean next(char c) {
        if (pos.child(c) != null) {
            pos = pos.child(c);
            return true;
        }
        return false;
    }

    TokenType matchedToken() {
        return pos.val;
    }
}
