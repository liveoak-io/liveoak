/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a sorting specification for results.
 *
 * <p>Sorting specification is usually passed as a query parameter 'sort' with fields in dot notation separated by commas.
 * A descending order can be specified for a field by pre-pending the field name with minus sign '-'.</p>
 *
 * <p>Example:</p>
 * <pre>-score,date</pre>
 *
 * <p>That expresses that items of the collection should be order by score from higher score to lower score, and if many have
 * the same score, those would be further ordered by date with earlier date returned first.</p>
 *
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class Sorting implements Iterable<Sorting.Spec> {

    private List<Spec> specs = new LinkedList<>();

    public Sorting(String sortingSpec) {
        String[] spec = sortingSpec.split(",");

        for (String s : spec) {
            boolean asc = true;
            s = s.trim();

            if (s.startsWith("-")) {
                s = s.substring(1);
                asc = false;
            }

            if (s.equals("")) {
                throw new IllegalArgumentException("Invalid sorting specification: " + sortingSpec);
            }
            specs.add(new Spec(s, asc));
        }
    }

    public Iterator<Spec> iterator() {
        return Collections.unmodifiableList(specs).iterator();
    }

    public static class Spec {
        private String name;
        private boolean ascending;

        private Spec(String name, boolean ascending) {
            this.name = name;
            this.ascending = ascending;
        }

        public boolean ascending() {
            return ascending;
        }

        public String name() {
            return name;
        }
    }
}