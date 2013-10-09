package org.projectodd.restafari.stomp.common;

import org.projectodd.restafari.stomp.Headers;
import org.projectodd.restafari.stomp.Stomp;

/**
 * @author Bob McWhirter
 */
public class StompControlFrame extends StompFrame {

    public StompControlFrame(Stomp.Command command) {
        super( command );
    }

    public StompControlFrame(Stomp.Command command, Headers headers) {
        super( command, headers );
    }

    public StompControlFrame(FrameHeader header) {
        super( header );
    }

}