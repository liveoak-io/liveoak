package io.liveoak.scripts.objects.impl;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakErrorResponse {

    String message;

    public LiveOakErrorResponse() {

    }

    public LiveOakErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
