package io.liveoak.scripts.objects;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public interface Application {

    String getId();

    String getName();

    String getDirectory();

    Boolean getVisible();
}
