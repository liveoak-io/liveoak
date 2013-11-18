/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp.common;

import io.liveoak.stomp.Headers;
import io.liveoak.stomp.Stomp;

/**
 * @author Bob McWhirter
 */
public class StompControlFrame extends StompFrame {

    public StompControlFrame( Stomp.Command command ) {
        super( command );
    }

    public StompControlFrame( Stomp.Command command, Headers headers ) {
        super( command, headers );
    }

    public StompControlFrame( FrameHeader header ) {
        super( header );
    }

}