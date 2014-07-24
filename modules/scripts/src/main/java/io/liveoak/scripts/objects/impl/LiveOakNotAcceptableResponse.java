package io.liveoak.scripts.objects.impl;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakNotAcceptableResponse extends LiveOakErrorResponse {

    public LiveOakNotAcceptableResponse() {
        super();
    }

    public LiveOakNotAcceptableResponse(String message) {
        super(message);
    }
}
