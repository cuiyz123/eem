package com.metarnet.core.common.utils;

import java.util.*;


/**
 * Created with IntelliJ IDEA.
 * User: jietianwu
 * Date: 13-5-9
 * Time: 下午7:26
 * 与Map不同的是，具有Set特性，
 * 将指定的值与此映射中的指定键关联（可选操作）。
 * 如果此映射以前包含一个该键的映射关系，则不进行任何处理（当且仅当 m.containsKey(k) 返回 true 时，才能说映射 m 包含键 k 的映射关系）。
 */
public class SetMap<K, V> extends HashMap<K, V> {
    private Set<K> set;

    public SetMap() {
        set = new HashSet<K>();
    }

    @Override
    public V put(K key, V value) {
        if (set.contains(key)) {
            return null;
        }
        return super.put(key, value);
    }

    @Override
    public V remove(Object key) {
        V v = super.remove(key);
        if (null != v) {
            set.remove(key);
        }
        return v;
    }

    @Override
    public void putAll(Map m) {
        if (null != m) {
            for (Iterator<? extends Map.Entry<? extends K, ? extends V>> ii = m.entrySet().iterator(); ii.hasNext(); ) {
                Map.Entry<? extends K, ? extends V> e = ii.next();
                put(e.getKey(), e.getValue());
            }
        }
    }

    @Override
    public void clear() {
        super.clear();
        set.clear();
    }


}
