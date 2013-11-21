/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi.state;

import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * Opaque state of a resource.
 *
 * <p>State objects are used to instill new state into a server-side resource.</p>
 *
 * @author Bob McWhirter
 */
public interface ResourceState {

    /**
     * Retrieve the ID of the resource.
     *
     * @return The ID of the resource.
     */
    String id();

    /**
     * Set the ID of the resource.
     *
     * @param id The ID of the resource.
     */
    void id( String id );

    void uri( URI uri );

    URI uri();

    /**
     * Add a property to the state.
     *
     * <p>Property values may be either simple scalar
     * values, or complex {@link ResourceState} objects</p>
     *
     * @param name  The name of the property.
     * @param value The value of the property.
     */
    void putProperty( String name, Object value );

    /**
     * Retrieve a property value.
     *
     * @param name The property name.
     * @return The value of the property, as either a simple scalar, or as a
     *         more complex {@link ResourceState}.
     */
    Object getProperty( String name );

    Object removeProperty(String name);

    Set<String> getPropertyNames();

    void addMember( ResourceState member );

    List<ResourceState> members();


}
