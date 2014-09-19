package io.liveoak.scripts;

import io.liveoak.spi.resource.RootResource;
import io.liveoak.testtools.resources.MockInMemoryResource;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MockInMemoryRootResource extends MockInMemoryResource implements RootResource {

    public MockInMemoryRootResource(String id) {
        super(id);
    }
}
