package org.projectodd.restafari.container.requests;

import io.netty.handler.codec.http.FullHttpRequest;
import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.spi.Pagination;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public interface HttpResourceRequest extends FullHttpRequest {

    ResourcePath getResourcePath();

    Pagination getPagination();

    String getMimeType();

    default String getResourceType() {
        return getResourcePath().getType();
    }

    default boolean isCollectionRequest() {
        return getResourcePath().isCollectionPath();
    }

    default boolean isResourceRequest() {
        return getResourcePath().isResourcePath();
    }

    default String getHttpMethod() {
        return getMethod().name().toUpperCase(); // Uppercase it just in case :)
    }
}
