/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi.resource.async;

/** A sink to asynchronously capture a resource's properties.
 *
 * @author Bob McWhirter
 */
public interface PropertySink {

    /** Accept a name/value pair representing a single
     * property of the resource.
     *
     * @param name The name of the property.
     * @param value The value of the property.
     */
    void accept( String name, Object value );


    /** Close the sink, indicating all properties have been sunk.
     */
    void close();

}
