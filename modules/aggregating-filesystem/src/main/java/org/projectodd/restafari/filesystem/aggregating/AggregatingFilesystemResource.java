package org.projectodd.restafari.filesystem.aggregating;

import org.projectodd.restafari.filesystem.FileResource;
import org.projectodd.restafari.filesystem.FilesystemResource;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.BinaryResource;
import org.projectodd.restafari.spi.resource.async.DelegatingResponder;
import org.projectodd.restafari.spi.resource.async.Responder;

/**
 * @author Bob McWhirter
 */
public class AggregatingFilesystemResource extends FilesystemResource {

    @Override
    public void read(String originalId, Responder originalResponder) {
        super.read( originalId, new DelegatingResponder(originalResponder) {
            @Override
            public void noSuchResource(String id) {
                String aggrId = originalId + ".aggr";

                AggregatingFilesystemResource.super.read( aggrId, new DelegatingResponder(originalResponder) {
                    @Override
                    public void noSuchResource(String id) {
                        super.noSuchResource(originalId);
                    }

                    @Override
                    public void resourceRead(Resource resource) {
                        if ( resource instanceof FileResource ) {
                            super.resourceRead( new AggregatingResource( AggregatingFilesystemResource.this, originalId, (FileResource) resource ) );
                        } else {
                            super.noSuchResource(originalId);
                        }
                    }
                });
            }
        });
    }


}
