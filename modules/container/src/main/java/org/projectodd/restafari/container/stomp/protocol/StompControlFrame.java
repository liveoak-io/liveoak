package org.projectodd.restafari.container.stomp.protocol;

import org.projectodd.restafari.container.stomp.Headers;

/**
 * @author Bob McWhirter
 */
public class StompControlFrame extends StompFrame {

    public StompControlFrame(Command command) {
        super( command );
    }

    public StompControlFrame(Command command, Headers headers) {
        super( command, headers );
    }

    public StompControlFrame(FrameHeader header) {
        super( header );
    }

}