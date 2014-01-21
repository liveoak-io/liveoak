package io.liveoak.keycloak;

import io.liveoak.spi.MediaType;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.BinaryContentSink;
import io.liveoak.spi.resource.async.BinaryResource;
import io.liveoak.spi.resource.async.Resource;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.keycloak.models.RealmModel;

import java.nio.ByteBuffer;

/**
 * @author Bob McWhirter
 */
public class PublicKeyResource implements BinaryResource {

    private final Resource parent;
    private final RealmModel realmModel;

    public PublicKeyResource(Resource parent, RealmModel realmModel) {
        this.parent = parent;
        this.realmModel = realmModel;
    }

    @Override
    public String id() {
        return "public-key";
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public MediaType mediaType() {
        return new MediaType("application/x-pem-file");
    }

    @Override
    public long contentLength() {
        return realmModel.getPublicKeyPem().getBytes().length;
    }

    @Override
    public void readContent(RequestContext ctx, BinaryContentSink sink) throws Exception {
        String pem = this.realmModel.getPublicKeyPem();
        byte[] bytes = pem.getBytes();
        ByteBuf buf = Unpooled.copiedBuffer( bytes );
        sink.accept(buf);
        sink.close();
    }

}
