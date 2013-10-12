package org.projectodd.restafari.container.responses;

import io.netty.buffer.ByteBuf;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public interface HttpResourceResponse {

    String getContentType();

    String getHttpMethod();

    ByteBuf content();
}
