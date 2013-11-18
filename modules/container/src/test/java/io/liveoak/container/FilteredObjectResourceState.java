/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.state.ResourceState;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class FilteredObjectResourceState implements ResourceState {

    private final ResourceState delegate;
    private final ReturnFields filter;

    public FilteredObjectResourceState( ResourceState delegate, ReturnFields filter ) {
        this.delegate = delegate;
        this.filter = filter;
    }

    @Override
    public void uri( URI uri ) {
        this.delegate.uri( uri );
    }

    @Override
    public URI uri() {
        return this.delegate.uri();
    }

    @Override
    public void putProperty( String name, Object value ) {
        delegate.putProperty( name, value );
    }

    @Override
    public Object getProperty( String name ) {
        if ( filter != null && !filter.included( name ) ) {
            return null;
        }

        Object val = delegate.getProperty( name );
        if ( val == null ) {
            return null;
        }

        ReturnFields childFilter = filter.child( name );
        if ( childFilter != null && val instanceof ResourceState ) {
            val = new FilteredObjectResourceState( ( ResourceState ) val, childFilter );
        }
        return val;
    }

    @Override
    public Set<String> getPropertyNames() {
        Set<String> filteredNames = new HashSet<>();

        Set<String> names = delegate.getPropertyNames();
        for ( String name : names ) {
            if ( filter == null ) {
                filteredNames.add( name );
            }

            if ( filter.included( name ) ) {
                filteredNames.add( name );
            }
        }

        return filteredNames;
    }

    @Override
    public void addMember( ResourceState member ) {
        delegate.addMember( member );
    }

    @Override
    public List<ResourceState> members() {
        return delegate.members();
    }

    @Override
    public String id() {
        return delegate.id();
    }

    @Override
    public void id( String id ) {
        delegate.id( id );
    }
}
