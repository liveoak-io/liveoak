/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.codec;

import io.liveoak.spi.resource.async.Resource;
import io.netty.buffer.ByteBuf;

import java.util.Date;

/**
 * @author Bob McWhirter
 */
public interface Encoder extends AutoCloseable {

    void initialize(ByteBuf buffer) throws Exception;

    void close() throws Exception;

    void startResource(Resource resource) throws Exception;

    void endResource(Resource resource) throws Exception;

    void writeLink(Resource link) throws Exception;

    void startProperties() throws Exception;

    void endProperties() throws Exception;

    void startProperty(String propertyName) throws Exception;

    void endProperty(String propertyName) throws Exception;

    void startMembers() throws Exception;

    void endMembers() throws Exception;

    void startList() throws Exception;

    void endList() throws Exception;

    void writeValue(String value) throws Exception;

    void writeValue(Integer value) throws Exception;

    void writeValue(Double value) throws Exception;

    void writeValue(Date value) throws Exception;

}
