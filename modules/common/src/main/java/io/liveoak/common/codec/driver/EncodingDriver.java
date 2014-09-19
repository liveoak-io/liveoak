/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common.codec.driver;

import io.liveoak.common.codec.Encoder;
import io.liveoak.spi.RequestContext;

/**
 * @author Bob McWhirter
 */
public interface EncodingDriver {

    Encoder encoder();

    Object object();

    RequestContext requestContext();

    void encode() throws Exception;

    void close() throws Exception;

    EncodingDriver parent();

    void encodeNext() throws Exception;

    void error(Throwable e);
}
