/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.pgsql.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class Row {
    private List<String> columnNames;
    private List<Object> values;

    public Row(List<String> columnNames, List<Object> cols) {
        this.columnNames = columnNames;
        this.values = Collections.unmodifiableList(cols);
    }

    public Map<String, Object> asMap() {
        return new Map<String, Object>() {
            @Override
            public int size() {
                return columnNames.size();
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean containsKey(Object key) {
                return columnNames.contains(key);
            }

            @Override
            public boolean containsValue(Object value) {
                return values.contains(value);
            }

            @Override
            public Object get(Object key) {
                int pos = columnNames.indexOf(key);
                if (pos == -1) {
                    return null;
                } else {
                    return values.get(pos);
                }
            }

            @Override
            public Object put(String key, Object value) {
                throw new UnsupportedOperationException("This map implementation is immutable");
            }

            @Override
            public Object remove(Object key) {
                throw new UnsupportedOperationException("This map implementation is immutable");
            }

            @Override
            public void putAll(Map m) {
                throw new UnsupportedOperationException("This map implementation is immutable");
            }

            @Override
            public void clear() {
                throw new UnsupportedOperationException("This map implementation is immutable");
            }

            @Override
            public Set keySet() {
                return new ColumnSet();
            }

            @Override
            public Collection values() {
                return values;
            }

            @Override
            public Set<Entry<String, Object>> entrySet() {
                return new EntrySet();
            }
        };
    }

    public Object value(String columnName) {
        int pos = columnNames.indexOf(columnName);
        if (pos == -1) {
            throw new IllegalArgumentException("No such column: " + columnName);
        }
        return values.get(pos);
    }

    class ColumnSet implements Set<String> {

        @Override
        public int size() {
            return columnNames.size();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return columnNames.contains(o);
        }

        @Override
        public Iterator<String> iterator() {
            return columnNames.iterator();
        }

        @Override
        public Object[] toArray() {
            return columnNames.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return columnNames.toArray(a);
        }

        @Override
        public boolean add(String s) {
            throw new UnsupportedOperationException("This set implementation is immutable");
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("This set implementation is immutable");
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return columnNames.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends String> c) {
            throw new UnsupportedOperationException("This set implementation is immutable");
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("This set implementation is immutable");
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("This set implementation is immutable");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("This set implementation is immutable");
        }
    }

    class EntrySet implements Set<Map.Entry<String, Object>> {

        @Override
        public int size() {
            return columnNames.size();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Iterator<Map.Entry<String, Object>> iterator() {
            return new Iterator<Map.Entry<String, Object>>() {

                Iterator<String> keyIt;
                Iterator<Object> valIt;
                {
                    keyIt = columnNames.iterator();
                    valIt = values.iterator();
                }

                @Override
                public boolean hasNext() {
                    return keyIt.hasNext();
                }

                @Override
                public Map.Entry<String, Object> next() {
                    final String key = keyIt.next();
                    final Object val = valIt.next();

                    return new Map.Entry<String, Object>() {

                        @Override
                        public String getKey() {
                            return key;
                        }

                        @Override
                        public Object getValue() {
                            return val;
                        }

                        @Override
                        public Object setValue(Object value) {
                            throw new UnsupportedOperationException("This set implementation is immutable");
                        }
                    };
                }
            };
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public boolean add(Map.Entry<String, Object> stringObjectEntry) {
            throw new UnsupportedOperationException("This set implementation is immutable");
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("This set implementation is immutable");
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public boolean addAll(Collection<? extends Map.Entry<String, Object>> c) {
            throw new UnsupportedOperationException("This set implementation is immutable");
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("This set implementation is immutable");
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("This set implementation is immutable");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("This set implementation is immutable");
        }
    }
}
