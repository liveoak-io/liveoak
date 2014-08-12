package io.liveoak.scripts.objects.impl.exception;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakNotAcceptableException extends LiveOakException {

    public LiveOakNotAcceptableException(String message) {
        super(message);
        this.name = "liveoak.NotAcceptableError";
    }

    public LiveOakNotAcceptableException() {
        this(null);
    }

}
