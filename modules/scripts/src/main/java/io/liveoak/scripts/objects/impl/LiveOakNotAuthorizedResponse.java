package io.liveoak.scripts.objects.impl;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakNotAuthorizedResponse extends LiveOakErrorResponse {

    public LiveOakNotAuthorizedResponse() {
        super();
    }

    public LiveOakNotAuthorizedResponse(String message) {
        super(message);
    }
}