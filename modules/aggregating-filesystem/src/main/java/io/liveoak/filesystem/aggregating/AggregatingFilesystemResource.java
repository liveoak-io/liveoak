package io.liveoak.filesystem.aggregating;

import io.liveoak.filesystem.FileResource;
import io.liveoak.filesystem.FilesystemResource;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.DelegatingResponder;
import io.liveoak.spi.resource.async.Responder;

/**
 * @author Bob McWhirter
 */
public class AggregatingFilesystemResource extends FilesystemResource {

    @Override
    public void readMember(RequestContext ctx, String originalId, Responder originalResponder) {
        super.readMember(ctx, originalId, new DelegatingResponder(originalResponder) {
            @Override
            public void noSuchResource(String id) {
                String aggrId = originalId + ".aggr";

                AggregatingFilesystemResource.super.readMember(ctx, aggrId, new DelegatingResponder(originalResponder) {
                    @Override
                    public void noSuchResource(String id) {
                        super.noSuchResource(originalId);
                    }

                    @Override
                    public void resourceRead(Resource resource) {
                        if (resource instanceof FileResource) {
                            super.resourceRead(new AggregatingResource(AggregatingFilesystemResource.this, originalId, (FileResource) resource));
                        } else {
                            super.noSuchResource(originalId);
                        }
                    }
                });
            }
        });
    }


}
