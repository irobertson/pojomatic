package org.pojomatic.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A thread-safe "map" which generates values on demand, with the guarantee that no more than one
 * value will be auto-created for a given key.
 * Classes extending this class should override {@link #create(Object)}.
 */
public abstract class SelfPopulatingMap<K, V> {

  public V get(final K key) {
    V value = valueMap.get(key);
    if (value == null) {
      final Object mutex = new Object();
      synchronized (mutex) {
        Object existingMutex = mutexMap.putIfAbsent(key, mutex);
        if (existingMutex == null) {
          return tryCreatingValue(key);
        }
        else {
          synchronized (existingMutex) {
            V oldValue = valueMap.get(key);
            // if the previous holder of this mutex failed to create a value, we'll give it a shot.
            return oldValue != null ? oldValue : tryCreatingValue(key);
          }
        }
      }
    }
    return value;
  }

  /**
   * Create a value for a key.  This will be called by {@link #get(Object)} when there is not
   * already an existing value, and no other thread is already creating a value for that key.
   * The value returned must not be null.
   * @param key the key to create the value for
   * @return the value
   */
  protected abstract V create(K key);

  private V tryCreatingValue(K key) {
    V value = create(key);
    valueMap.put(key, value);
    return value;
  }

  /**
   * The values held by this map.
   */
  private final ConcurrentMap<K, V> valueMap = new ConcurrentHashMap<K, V>();

  /**
   * Mutexes created on demand to ensure that only a single value is created for each key.
   */
  private final ConcurrentMap<K, Object> mutexMap = new ConcurrentHashMap<K, Object>();
}

