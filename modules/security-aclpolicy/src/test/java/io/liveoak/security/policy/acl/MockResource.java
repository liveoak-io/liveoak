package io.liveoak.security.policy.acl;

import java.util.Arrays;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class MockResource implements RootResource {

    private Resource parent;
    private String id;

    public MockResource(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {
        switch (id) {
            case "1":
                responder.resourceRead(new Resource() {

                    @Override
                    public Resource parent() {
                        return MockResource.this;
                    }

                    @Override
                    public String id() {
                        return "1";
                    }

                    @Override
                    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
                        sink.accept("user", "john");
                        sink.accept("roles", Arrays.asList("test-app/admin", "test-app/foo"));
                        sink.accept("something", "something-which-does-not-matter");
                        sink.close();
                    }
                });
                break;
            case "2":
                responder.resourceRead(new Resource() {

                    @Override
                    public Resource parent() {
                        return MockResource.this;
                    }

                    @Override
                    public String id() {
                        return "2";
                    }

                    // Doesn't have user or roles attribute
                    @Override
                    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
                        sink.accept("something", "something-which-does-not-matter");
                        sink.close();
                    }
                });
                break;
            default:
                responder.noSuchResource(id);
        }

    }

}
