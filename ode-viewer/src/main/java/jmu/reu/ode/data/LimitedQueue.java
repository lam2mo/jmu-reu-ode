package jmu.reu.ode.data;

import java.util.Iterator;

/**
 * A class that defines a Queue with limited space.  When you add a value to the queue and it is
 * full, the queue will delete the first entry put in.  Think FIFD (First in, First deleted).
 * @author Benjamin Huber
 * @version 7/9/2022
 */

public class LimitedQueue<E> implements Iterable<E> {
    private E[] data;
    @SuppressWarnings("unchecked")
    public LimitedQueue(int length) {
        data = (E[])(new Object[length]);
    }

    /**
     * Adds an instance of an object into the Queue, and deletes the last element off the queue.
     * @param object the new item to add
     */
    public void add(E object) {
        if (object == null) {
            throw new IllegalArgumentException("Cannot add null value into LimitedQueue");
        }
        if (data.length == 0) {
            return;
        }
        for (int i = data.length-1; i > 0; i--) {
            data[i] = data[i-1];
        }
        data[0] = object;
    }

    public E get(int index) {
        return data[index];
    }

    public Iterator<E> iterator() {
        Iterator<E> iterator = new Iterator<E>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < data.length;
            }

            @Override
            public E next() {
                return data[index++];
            }

            @Override
            public void remove() {

            }
        };
        return iterator;
    }
}