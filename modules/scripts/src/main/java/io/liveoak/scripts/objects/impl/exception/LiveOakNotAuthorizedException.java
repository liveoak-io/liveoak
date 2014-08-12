package io.liveoak.scripts.objects.impl.exception;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakNotAuthorizedException extends LiveOakException {

    public LiveOakNotAuthorizedException(String message) {
        super(message);
        this.name = "liveoak.NotAuthorizedError";
    }

    public LiveOakNotAuthorizedException() {
        this(null);
    }

}