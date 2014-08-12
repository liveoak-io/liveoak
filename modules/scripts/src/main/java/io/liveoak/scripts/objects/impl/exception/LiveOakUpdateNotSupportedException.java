package io.liveoak.scripts.objects.impl.exception;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakUpdateNotSupportedException extends LiveOakException {

    public LiveOakUpdateNotSupportedException(String message) {
        super(message);
        this.name = "liveoak.UpdateNotSupportedError";
    }

    public LiveOakUpdateNotSupportedException() {
        this(null);
    }
}
