package io.liveoak.scripts.resource;

import org.junit.Test;

import static io.liveoak.testtools.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class PriorityPropertyTest extends BaseResourceTriggeredTestCase {

    @Test
    public void priorityProperty() throws Exception {
        // Test #1 - ensureScriptCreatedWithDefaultValue
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"default\", \"target-path\": \"targetPath\" }")).hasStatus(201);
        assertThat(toJSON(httpResponse.getEntity()).get("priority").asInt()).isEqualTo(5);

        // Test #2 - negative priority fails
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"negative\", \"target-path\": \"targetPath\", \"priority\": -1 }")).hasStatus(406);
        assertThat(httpResponse).isNotAcceptable();

        // Test #3 - zero priority fails
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"zero\", \"target-path\": \"targetPath\", \"priority\": 0 }")).hasStatus(406);
        assertThat(httpResponse).isNotAcceptable();

        // Test #4 - > 10 priority fails
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"toolarge\", \"target-path\": \"targetPath\", \"priority\": 11 }")).hasStatus(406);
        assertThat(httpResponse).isNotAcceptable();

        // Test #5 - valid priority
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"valid\", \"target-path\": \"targetPath\", \"priority\": 7 }")).hasStatus(201);
        assertThat(toJSON(httpResponse.getEntity()).get("priority").asInt()).isEqualTo(7);

        // Test #6 - updating a valid priority with invalid fails
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"invalidupdate\", \"target-path\": \"targetPath\", \"priority\": 2 }")).hasStatus(201);
        assertThat(toJSON(httpResponse.getEntity()).get("priority").asInt()).isEqualTo(2);
        assertThat(execPut(RESOURCE_SCRIPT_PATH + "/invalidupdate", "{ \"priority\": 12 }")).hasStatus(406);
        assertThat(httpResponse).isNotAcceptable();

        // Test #7 - updating a valid priority with null sets it to default
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"validtodefault\", \"target-path\": \"targetPath\", \"priority\": 2 }")).hasStatus(201);
        assertThat(toJSON(httpResponse.getEntity()).get("priority").asInt()).isEqualTo(2);
        assertThat(execPut(RESOURCE_SCRIPT_PATH + "/validtodefault", "{ \"target-path\": \"targetPath\", \"priority\": null }")).hasStatus(200);
        assertThat(toJSON(httpResponse.getEntity()).get("priority").asInt()).isEqualTo(5);
    }
}
