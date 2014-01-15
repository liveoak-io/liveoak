package io.liveoak.common.codec;

import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;

import java.util.Date;
import java.util.Map;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public interface StateEncoder extends Encoder<ResourceState> {
}
