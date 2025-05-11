package com.github.razorapid.morpheus.lang;

import java.util.ArrayList;
import java.util.List;

public class Tape<T> {
    private final List<T> data;
    private int currentPos = 0;

    private Tape(List<T> data) {
        this.data = new ArrayList<>(data);
    }

    public static <T> Tape<T> of(List<T> data) {
        return new Tape<>(data);
    }

    public boolean isEOB() {
        return isEOB(currentPos);
    }

    public boolean isEOB(int pos) {
        return data.isEmpty() || pos >= data.size() || pos < 0;
    }

    public T peek() {
        return peek(currentPos);
    }

    public T peekNext() {
        return peekNext(1);
    }

    public T peekNext(int offset) {
        return peek(currentPos + offset);
    }

    public T peekPrev() {
        return peek(currentPos - 1);
    }

    public T peek(int pos) {
        return isEOB(pos) ? null : data.get(pos);
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
        return backward(1);
    }

    public int backward(int offset) {
        return pos(pos() - offset);
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

    public List<T> data() {
        return data(0, data.size());
    }

    public List<T> data(int from, int to) {
        return data.subList(from, to);
    }

    public int size() {
        return data.size();
    }

    public void add(T elem) {
        data.add(elem);
    }

    private int capPos(int pos) {
        if (pos < 0) {
            return 0;
        } else if (pos > data.size()) {
            return data.size();
        }
        return pos;
    }
}
