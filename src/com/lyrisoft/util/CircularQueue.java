/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.util;

import java.util.Iterator;
import java.util.LinkedList;

public class CircularQueue {
    private int _size;
    private LinkedList _list;

    public CircularQueue(int size) {
        _size = size;
        _list = new LinkedList();
    }

    public synchronized void push(Object o) {
        if (_list.size() >= _size) {
            _list.removeFirst();
        }
        _list.add(o);
    }

    public synchronized Object pop(Object o) {
        return _list.removeFirst();
    }

    public synchronized Object peek(Object o) {
        return _list.getFirst();
    }

    public synchronized Object get(int idx) {
        return _list.get(idx);
    }

    public synchronized int size() {
        return _list.size();
    }

    public Iterator iterator() {
        return _list.iterator();
    }

    public static void main(String[] args) {
        CircularQueue q = new CircularQueue(3);
        q.push("1");
        q.push("2");
        q.push("3");

        for (Iterator i = q.iterator(); i.hasNext(); ) {
            System.err.println("> " + i.next());
        }
        
        System.err.println();

        q.push("4");
        q.push("5");
        for (Iterator i = q.iterator(); i.hasNext(); ) {
            System.err.println("> " + i.next());
        }
    }
}
