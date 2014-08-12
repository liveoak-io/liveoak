package io.liveoak.scripts.objects.impl.exception;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakResourceAlreadyExistsException extends LiveOakException {

    public LiveOakResourceAlreadyExistsException(String message) {
        super(message);
        this.name="liveoak.ResourceAlreadyExistsError";
    }

    public LiveOakResourceAlreadyExistsException() {
        this(null);
    }
}
