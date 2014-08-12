package io.liveoak.scripts.objects;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public interface Response {

    String getType();

    Resource getResource();

    Request getRequest();
}
