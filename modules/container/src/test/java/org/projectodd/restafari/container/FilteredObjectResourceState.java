package org.projectodd.restafari.container;

import org.projectodd.restafari.container.codec.DefaultPropertyResourceState;
import org.projectodd.restafari.spi.ReturnFields;
import org.projectodd.restafari.spi.state.ObjectResourceState;
import org.projectodd.restafari.spi.state.PropertyResourceState;

import java.util.stream.Stream;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class FilteredObjectResourceState implements ObjectResourceState {

    private final ObjectResourceState delegate;
    private final ReturnFields filter;

    public FilteredObjectResourceState(ObjectResourceState delegate, ReturnFields filter) {
        this.delegate = delegate;
        this.filter = filter;
    }

    @Override
    public void addProperty(String name, Object value) {
        delegate.addProperty(name, value);
    }

    @Override
    public Object getProperty(String name) {
        if (filter != null && !filter.included(name)) {
            return null;
        }

        Object val = delegate.getProperty(name);
        if (val == null) {
            return null;
        }

        ReturnFields childFilter = filter.child(name);
        if (childFilter != null && val instanceof ObjectResourceState) {
            val = new FilteredObjectResourceState((ObjectResourceState) val, childFilter);
        }
        return val;
    }

    @Override
    public Stream<? extends PropertyResourceState> members() {
        Stream<? extends PropertyResourceState> stream = delegate.members();
        stream = stream.flatMap((o) -> {
            if (filter != null && !filter.included(o.id())) {
                return null;
            }

            Object val = o.value();
            if (val == null) {
                return Stream.of(o);
            }

            ReturnFields childFilter = filter.child(o.id());
            if (childFilter != null && val instanceof ObjectResourceState) {
                val = new FilteredObjectResourceState((ObjectResourceState) val, childFilter);
                return Stream.of(new DefaultPropertyResourceState(o.id(), val));
            }
            return Stream.of(o);
        });

        return stream;
    }

    @Override
    public String id() {
        return delegate.id();
    }

    @Override
    public void id(String id) {
        delegate.id(id);
    }
}
