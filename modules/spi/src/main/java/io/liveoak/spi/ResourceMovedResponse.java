package io.liveoak.spi;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public interface ResourceMovedResponse extends ResourceResponse {

    public enum MovedType {
        MOVED_PERMANENTLY,
        MOVED_TEMPORARILY,
    }

    @Override
    default ResponseType responseType() {
        return ResponseType.MOVED;
    }

    String redirectURL();

    MovedType movedType();

    Integer maxAge();
}
