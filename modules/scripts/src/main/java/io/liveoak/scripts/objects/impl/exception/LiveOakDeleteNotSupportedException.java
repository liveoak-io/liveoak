package io.liveoak.scripts.objects.impl.exception;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakDeleteNotSupportedException extends LiveOakException {

    public LiveOakDeleteNotSupportedException(String message) {
        super(message);
        this.name = "liveoak.DeleteNotSupportedError";
    }

    public LiveOakDeleteNotSupportedException() {
        this(null);
    }

}
