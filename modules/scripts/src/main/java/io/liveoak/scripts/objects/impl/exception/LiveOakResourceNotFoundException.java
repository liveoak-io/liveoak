package io.liveoak.scripts.objects.impl.exception;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakResourceNotFoundException extends LiveOakException {

    public LiveOakResourceNotFoundException(String message) {
        super(message);
        this.name = "liveoak.ResourceNotFoundError";
    }

    public LiveOakResourceNotFoundException() {
        this(null);
    }

}
