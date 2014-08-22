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

    private List<Spec> specs;

    public Sorting() {
        specs = Collections.emptyList();
    }

    public Sorting(List<Spec> specs) {
        this.specs = Collections.unmodifiableList(specs);
    }

    //TODO: remove this. This exposes the implementation details of how the client specifies the
    // sorting in the request. This class should only deal with Specs directly, not a specialized string.
    // The class which reads the query parameter is the one which should be separating out the values.
    public Sorting(String sortingSpec) {
        String[] spec = sortingSpec.split(",");
        List<Spec> specList = new LinkedList<>();
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
            specList.add(new Spec(s, asc));
        }
        specs = Collections.unmodifiableList(specList);
    }

    public List<Spec> specs() {
        return this.specs;
    }

    public Iterator<Spec> iterator() {
        return specs.iterator();
    }

    public static class Spec {
        private String name;
        private boolean ascending;

        public Spec(String name, boolean ascending) {
            this.name = name;
            this.ascending = ascending;
        }

        public boolean ascending() {
            return ascending;
        }

        public void ascending(boolean ascending) {
            this.ascending = ascending;
        }

        public String name() {
            return name;
        }

        public void name(String name) {
            this.name = name;
        }
    }

    public static class Builder {
        private List<Spec> specList = new LinkedList<>();

        public Builder addSpec(String name, boolean ascending) {
            specList.add(new Spec(name, ascending));
            return this;
        }

        public Sorting build() {
            return new Sorting(specList);
        }
    }
}