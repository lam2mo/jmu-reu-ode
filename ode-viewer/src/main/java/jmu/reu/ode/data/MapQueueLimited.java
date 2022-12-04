package jmu.reu.ode.data;
/**
 * A class that implements a Map that contains a Key, LimitedQueue pair.
 * @author Benjamin Huber
 * @version 7/9/2022
 */
import java.util.HashMap;

public class MapQueueLimited<K, V> {
    private HashMap<K, LimitedQueue<V>> map;
    private int size;
    public MapQueueLimited(int size) {
        map = new HashMap<K, LimitedQueue<V>>();
        this.size = size;
    }

    /**
     * Puts the value in the appropriate key-associated LimitedQueue. Creating one if it doesn't
     * already exist.
     * @param key
     * @param value
     * @return
     */
    public V put(K key, V value) {
        if (!map.containsKey(key)) {
            map.put(key, new LimitedQueue<V>(size));
        }
        map.get(key).add(value);
        return value;
    }

    /**
     * Gets the associated LimitedQueue object.
     * @param key
     * @return the associated LimitedQueue object
     */
    public LimitedQueue<V> getQueue(K key) {
        return map.get(key);
    }
}