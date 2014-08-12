package io.liveoak.scripts.objects.impl.exception;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakCreateNotSupportedException extends LiveOakException {

    public LiveOakCreateNotSupportedException(String message) {
        super(message);
        this.name = "liveoak.CreateNotSupportedError";
    }

    public LiveOakCreateNotSupportedException() {
        this(null);
    }

}
