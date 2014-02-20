package io.liveoak.filesystem.aggregating.service;

import io.liveoak.filesystem.FilesystemResource;
import io.liveoak.filesystem.aggregating.AggregatingFilesystemResource;
import io.liveoak.filesystem.service.FilesystemResourceService;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author Bob McWhirter
 */
public class AggregatingFilesystemResourceService extends FilesystemResourceService {

    public AggregatingFilesystemResourceService(String id) {
        super(id);
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new AggregatingFilesystemResource(
                this.adminResourceInjector.getValue(),
                this.id,
                this.vertxInjector.getValue() );
    }
}
