/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi.resource.async;

import java.util.function.BiFunction;

/**
 * A sink to asynchronously capture a resource's properties.
 *
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public interface PropertySink {

    /**
     * Accept a name/value pair representing a single
     * property of the resource.
     *
     * @param name  The name of the property.
     * @param value The value of the property.
     */
    void accept(String name, Object value);

    /**
     * Close the sink, indicating all properties have been sunk.
     */
    void close() throws Exception;

    /**
     * Specify a function to replace the config value that will be returned.
     *
     * @param function Function to use for replacement.
     */
    void replaceConfig(BiFunction<String[], Object, Object> function);
}
