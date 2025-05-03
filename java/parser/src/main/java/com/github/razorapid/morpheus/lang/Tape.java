package com.github.razorapid.morpheus.lang;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor(staticName = "of")
public class Tape<T> {
    private final T[] data;
    private int currentPos = 0;

    public boolean isEOB() {
        return isEOB(currentPos);
    }

    public boolean isEOB(int pos) {
        return data.length == 0 || pos >= data.length || pos < 0;
    }

    public T peek() {
        return peek(currentPos);
    }

    public T peekNext() {
        return peek(currentPos + 1);
    }

    public T peekPrev() {
        return peek(currentPos - 1);
    }

    public T peek(int pos) {
        return isEOB(pos) ? null : data[pos];
    }

    public T next() {
        return peek(forward());
    }

    public T prev() {
        return peek(backward());
    }

    public boolean match(T e) {
        if (isEOB() || peek() != e) return false;
        forward();
        return true;
    }

    public int forward() {
        return pos(pos() + 1);
    }

    public int backward() {
        return pos(pos() - 1);
    }

    public void rewind() {
        pos(0);
    }

    public int pos() {
        return currentPos;
    }

    public int pos(int newPos) {
        int ret = currentPos;
        currentPos = capPos(newPos);
        return ret;
    }

    public T[] data(int from, int to) {
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
