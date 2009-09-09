package edu.internet2.middleware.grouper.grouperUi.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * simple wrapper of map
 * @version $Id: MapWrapper.java,v 1.1 2009-09-09 15:10:03 mchyzer Exp $
 * @author mchyzer
 * @param <K> 
 * @param <V> 
 */
public class MapWrapper<K,V> implements Map<K,V>, Serializable {

  /**
   * size
   * @see java.util.Map#size()
   */
  public int size() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * isEmpty
   * @see java.util.Map#isEmpty()
   */
  public boolean isEmpty() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * 
   * @see java.util.Map#containsKey(java.lang.Object)
   */
  public boolean containsKey(Object key) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * 
   * @see java.util.Map#containsValue(java.lang.Object)
   */
  public boolean containsValue(Object value) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * 
   * @see java.util.Map#get(java.lang.Object)
   */
  public V get(Object key) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * 
   * @param key
   * @param value
   * @return the object it replaces (or null if you dont care)
   */
  public V put(K key, V value) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * 
   * @see java.util.Map#remove(java.lang.Object)
   */
  public V remove(Object key) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * 
   * @param t
   * @see java.util.Map#putAll(Map)
   */
  public void putAll(Map t) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * 
   * @see java.util.Map#clear()
   */
  public void clear() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * 
   * @see java.util.Map#keySet()
   */
  public Set<K> keySet() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * 
   * @see java.util.Map#values()
   */
  public Collection<V> values() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * 
   * @see java.util.Map#entrySet()
   */
  public Set<Map.Entry<K,V>> entrySet() {
    throw new RuntimeException("Not implemented");
  }

}
