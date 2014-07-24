package io.liveoak.scripts.objects.impl;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakForbiddenResponse extends LiveOakErrorResponse {

    public LiveOakForbiddenResponse() {
        super();
    }

    public LiveOakForbiddenResponse(String message) {
        super(message);
    }
}
