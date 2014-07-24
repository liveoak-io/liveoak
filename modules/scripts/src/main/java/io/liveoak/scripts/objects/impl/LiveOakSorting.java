package io.liveoak.scripts.objects.impl;


import java.util.AbstractList;
import java.util.List;

import io.liveoak.scripts.objects.Sorting;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */

public class LiveOakSorting extends AbstractList<Sorting.Spec> {

    List<io.liveoak.spi.Sorting.Spec> specs;

    public LiveOakSorting(io.liveoak.spi.Sorting sorting) {
        this.specs = sorting.specs();
    }

    @Override
    public Sorting.Spec get(int index) {
        io.liveoak.spi.Sorting.Spec spec = specs.get(index);
        return new LiveOakSpec(spec);
    }

    @Override
    public int size() {
        return specs.size();
    }

    @Override
    public Sorting.Spec set(int index, Sorting.Spec element) {
        io.liveoak.spi.Sorting.Spec spec = new io.liveoak.spi.Sorting.Spec(element.getName(), element.getAscending());
        io.liveoak.spi.Sorting.Spec savedSpec = specs.set(index, spec);

        return new LiveOakSpec(savedSpec);
    }

    @Override
    public void add(int index, Sorting.Spec element) {
        io.liveoak.spi.Sorting.Spec spec = new io.liveoak.spi.Sorting.Spec(element.getName(), element.getAscending());
        specs.add(index, spec);
    }

    @Override
    public Sorting.Spec remove(int index) {
        io.liveoak.spi.Sorting.Spec spec = specs.remove(index);
        return new LiveOakSpec(spec);
    }


    public class LiveOakSpec implements Sorting.Spec {

        io.liveoak.spi.Sorting.Spec spec;

        public LiveOakSpec(io.liveoak.spi.Sorting.Spec spec) {
            this.spec = spec;
        }

        @Override
        public String getName() {
            return spec.name();
        }

        @Override
        public void setName(String name) {
            spec.name(name);
        }

        @Override
        public Boolean getAscending() {
            return spec.ascending();
        }

        @Override
        public void setAscending(Boolean ascending) {
            spec.ascending(ascending);
        }

        @Override
        public String toString() {
            return "{ name:'" + spec.name() + "', ascending:'" + spec.ascending() + "'}";
        }


    }
}
