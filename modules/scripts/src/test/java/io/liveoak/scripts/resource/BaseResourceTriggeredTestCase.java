package io.liveoak.scripts.resource;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.scripts.BaseScriptTestCase;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class BaseResourceTriggeredTestCase extends BaseScriptTestCase{
    public static final String RESOURCE_ID = "resource-triggered-scripts";
    public static final String RESOURCE_SCRIPT_PATH = SCRIPT_PATH + "/" + RESOURCE_ID;


    public ResourceState createScriptMetaData(String id, String targetPath) {
        ResourceState resourceState = new DefaultResourceState(id);
        resourceState.putProperty("target-path", targetPath);

        return resourceState;
    }

}
