package io.liveoak.stomp.common;

import io.liveoak.stomp.Headers;
import io.liveoak.stomp.Stomp;

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