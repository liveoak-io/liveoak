package io.liveoak.container.protocols;

import java.util.UUID;

/**
 * @author Bob McWhirter
 */
public class RequestCompleteEvent {

    public RequestCompleteEvent(UUID requestId) {
        this.requestId = requestId;
    }

    public UUID requestId() {
        return this.requestId;
    }

    private UUID requestId;
}
