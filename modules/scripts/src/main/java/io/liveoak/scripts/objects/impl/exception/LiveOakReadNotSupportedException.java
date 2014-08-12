package io.liveoak.scripts.objects.impl.exception;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakReadNotSupportedException extends LiveOakException {

    public LiveOakReadNotSupportedException() {
        this(null);
    }

    public LiveOakReadNotSupportedException(String message) {
        super(message);
        this.name = "liveoak.ReadNotSupportedError";
    }

}
