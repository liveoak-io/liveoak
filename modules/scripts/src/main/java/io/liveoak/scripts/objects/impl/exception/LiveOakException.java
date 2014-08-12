package io.liveoak.scripts.objects.impl.exception;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakException extends Exception {

    String message;
    String name;

    public LiveOakException() {
        this(null);
    }

    public LiveOakException(String message) {
        this.message = message;
        this.name = "liveoak.Error";
    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " : " + message;
    }
}
