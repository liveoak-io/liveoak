package io.liveoak.spi;

import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public interface ResourceResponse {

    public enum ResponseType {
        CREATED,
        READ,
        UPDATED,
        DELETED,
        ERROR,
    }

    ResponseType responseType();
    Resource resource();

    ResourceRequest inReplyTo();


}
