package com.github.razorapid.morpheus.lang;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor(staticName = "of")
class Tape<T> {
    private final T[] data;
    private int currentPos = 0;

    boolean isEOB() {
        return isEOB(currentPos);
    }

    boolean isEOB(int pos) {
        return data.length == 0 || pos >= data.length || pos < 0;
    }

    T peek() {
        return peek(currentPos);
    }

    T peekNext() {
        return peek(currentPos + 1);
    }

    T peekPrev() {
        return peek(currentPos - 1);
    }

    T peek(int pos) {
        return isEOB(pos) ? null : data[pos];
    }

    T next() {
        return peek(forward());
    }

    T prev() {
        return peek(backward());
    }

    boolean match(T e) {
        if (isEOB() || peek() != e) return false;
        forward();
        return true;
    }

    int forward() {
        return pos(pos() + 1);
    }

    int backward() {
        return pos(pos() - 1);
    }

    void rewind() {
        pos(0);
    }

    int pos() {
        return currentPos;
    }

    int pos(int newPos) {
        int ret = currentPos;
        currentPos = capPos(newPos);
        return ret;
    }

    T[] data(int from, int to) {
        return Arrays.copyOfRange(data, from, to);
    }

    private int capPos(int pos) {
        if (pos < 0) {
            return 0;
        } else if (pos > data.length) {
            return data.length;
        }
        return pos;
    }
}
