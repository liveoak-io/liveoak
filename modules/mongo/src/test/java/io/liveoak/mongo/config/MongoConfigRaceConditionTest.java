package io.liveoak.mongo.config;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoConfigRaceConditionTest extends BaseMongoConfigTest {

    @Test
    public void testConfigureCredentialCR() throws Exception {
        for (int i = 0; i < 4000 ; ++i) {
            if ( i % 100 == 0 ) {
                System.gc();
            }
            ResourceState config = new DefaultResourceState();
            config.putProperty("db", "testConfigureCRDB");

            List<ResourceState> credentials = new ArrayList<ResourceState>();
            ResourceState credential = new DefaultResourceState();
            credential.putProperty("mechanism", "MONGODB-CR");
            credential.putProperty("username", "foo");
            credential.putProperty("password", "bar");
            credential.putProperty("database", "testConfigureCRDB");

            credentials.add(credential);
            config.putProperty("credentials", credentials);

            setUpSystem(config);

            ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);
            assertThat(result.getProperty("db")).isEqualTo("testConfigureCRDB");

            // by default it should exist but be empty
            assertThat(result.getProperty("credentials")).isNotNull();
            assertThat(((List) result.getProperty("credentials")).size()).isEqualTo(1);
            ResourceState credentialResult = (ResourceState) ((List) result.getProperty("credentials")).get(0);
            assertThat(credentialResult.getPropertyNames().size()).isEqualTo(4);
            assertThat(credentialResult.getProperty("username")).isEqualTo("foo");
            assertThat(credentialResult.getProperty("password")).isEqualTo("bar");
            assertThat(credentialResult.getProperty("database")).isEqualTo("testConfigureCRDB");
            assertThat(credentialResult.getProperty("mechanism")).isEqualTo("MONGODB-CR");
            removeAllResources();
        }
    }


}

