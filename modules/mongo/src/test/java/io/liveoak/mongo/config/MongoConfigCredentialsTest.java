package io.liveoak.mongo.config;

import java.util.ArrayList;
import java.util.List;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @author Ken Finnigan
 */
public class MongoConfigCredentialsTest extends BaseMongoConfigTest {

    @Test
    public void testDefault() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testDefaultDB");
        config.putProperty("servers", new ArrayList());
        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);
        assertThat(result.getProperty("db")).isEqualTo("testDefaultDB");

        // by default it should exist but be empty
        assertThat(result.getProperty("credentials")).isNotNull();
        assertThat((List) result.getProperty("credentials")).isEmpty();
    }

    @Test
    public void testConfigureCredentialCR() throws Exception {
        ResourceState config = new DefaultResourceState();
        config = new DefaultResourceState();
        config.putProperty("db", "testConfigureCRDB");
        config.putProperty("servers", new ArrayList());

        List<ResourceState> credentials = new ArrayList<>();
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
        assertThat(result.getProperty("credentials")).isNotNull();
        assertThat(((List) result.getProperty("credentials")).size()).isEqualTo(1);

        ResourceState credentialResult = (ResourceState) ((List) result.getProperty("credentials")).get(0);
        assertThat(credentialResult.getPropertyNames().size()).isEqualTo(4);
        assertThat(credentialResult.getProperty("username")).isEqualTo("foo");
        assertThat(credentialResult.getProperty("password")).isEqualTo("bar");
        assertThat(credentialResult.getProperty("database")).isEqualTo("testConfigureCRDB");
        assertThat(credentialResult.getProperty("mechanism")).isEqualTo("MONGODB-CR");
    }

    @Test
    public void testConfigureCredentialGSS() throws Exception {
        ResourceState config = new DefaultResourceState();
        config = new DefaultResourceState();
        config.putProperty("db", "testConfigureGSSDB");
        config.putProperty("servers", new ArrayList());

        List<ResourceState> credentials = new ArrayList<>();
        ResourceState credential = new DefaultResourceState();
        credential.putProperty("mechanism", "GSSAPI");
        credential.putProperty("username", "foobar");

        credentials.add(credential);
        config.putProperty("credentials", credentials);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);
        assertThat(result.getProperty("db")).isEqualTo("testConfigureGSSDB");

        // by default it should exist but be empty
        assertThat(result.getProperty("credentials")).isNotNull();
        assertThat(((List) result.getProperty("credentials")).size()).isEqualTo(1);
        ResourceState credentialResult = (ResourceState) ((List) result.getProperty("credentials")).get(0);
        assertThat(credentialResult.getPropertyNames().size()).isEqualTo(2);
        assertThat(credentialResult.getProperty("username")).isEqualTo("foobar");
        assertThat(credentialResult.getProperty("mechanism")).isEqualTo("GSSAPI");
    }

    @Test
    public void testConfigureMultipleCredentials () throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "ConfigureMultipleCredentialsDB");
        config.putProperty("servers", new ArrayList());

        List<ResourceState> credentials = new ArrayList<>();
        ResourceState credentialA = new DefaultResourceState();
        credentialA.putProperty("mechanism", "GSSAPI");
        credentialA.putProperty("username", "userA");
        credentials.add(credentialA);

        ResourceState credentialB = new DefaultResourceState();
        credentialB.putProperty("mechanism", "MONGODB-CR");
        credentialB.putProperty("username", "userB");
        credentialB.putProperty("password", "pwB");
        credentialB.putProperty("database", "databaseB");
        credentials.add(credentialB);

        ResourceState credentialC = new DefaultResourceState();
        credentialC.putProperty("mechanism", "MONGODB-CR");
        credentialC.putProperty("username", "userC");
        credentialC.putProperty("password", "pwC");
        credentialC.putProperty("database", "databaseC");
        credentials.add(credentialC);

        config.putProperty("credentials", credentials);
        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);
        assertThat(result.getProperty("db")).isEqualTo("ConfigureMultipleCredentialsDB");

        // by default it should exist but be empty
        assertThat(result.getProperty("credentials")).isNotNull();
        assertThat(((List) result.getProperty("credentials")).size()).isEqualTo(3);
        ResourceState credentialResultA = (ResourceState) ((List) result.getProperty("credentials")).get(1);
        assertThat(credentialResultA.getPropertyNames().size()).isEqualTo(2);
        assertThat(credentialResultA.getProperty("username")).isEqualTo("userA");
        assertThat(credentialResultA.getProperty("mechanism")).isEqualTo("GSSAPI");

        ResourceState credentialResultB = (ResourceState) ((List) result.getProperty("credentials")).get(2);
        assertThat(credentialResultB.getPropertyNames().size()).isEqualTo(4);
        assertThat(credentialResultB.getProperty("username")).isEqualTo("userB");
        assertThat(credentialResultB.getProperty("password")).isEqualTo("pwB");
        assertThat(credentialResultB.getProperty("database")).isEqualTo("databaseB");
        assertThat(credentialResultB.getProperty("mechanism")).isEqualTo("MONGODB-CR");

        ResourceState credentialResultC = (ResourceState) ((List) result.getProperty("credentials")).get(0);
        assertThat(credentialResultC.getPropertyNames().size()).isEqualTo(4);
        assertThat(credentialResultC.getProperty("username")).isEqualTo("userC");
        assertThat(credentialResultC.getProperty("password")).isEqualTo("pwC");
        assertThat(credentialResultC.getProperty("database")).isEqualTo("databaseC");
        assertThat(credentialResultC.getProperty("mechanism")).isEqualTo("MONGODB-CR");
    }

    @Test
    public void testUpdateCredentials() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "UpdateCredentialsDB");
        config.putProperty("servers", new ArrayList());

        List<ResourceState> credentials = new ArrayList<>();
        ResourceState credentialA = new DefaultResourceState();
        credentialA.putProperty("mechanism", "GSSAPI");
        credentialA.putProperty("username", "userA");
        credentials.add(credentialA);

        ResourceState credentialB = new DefaultResourceState();
        credentialB.putProperty("mechanism", "MONGODB-CR");
        credentialB.putProperty("username", "userB");
        credentialB.putProperty("password", "pwB");
        credentialB.putProperty("database", "databaseB");
        credentials.add(credentialB);

        ResourceState credentialC = new DefaultResourceState();
        credentialC.putProperty("mechanism", "MONGODB-CR");
        credentialC.putProperty("username", "userC");
        credentialC.putProperty("password", "pwC");
        credentialC.putProperty("database", "databaseC");
        credentials.add(credentialC);

        config.putProperty("credentials", credentials);
        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);
        assertThat(result.getProperty("db")).isEqualTo("UpdateCredentialsDB");

        ResourceState updatedConfig = new DefaultResourceState();
        updatedConfig.putProperty("db", "UpdateCredentialsDB");
        updatedConfig.putProperty("servers", new ArrayList());

        List<ResourceState> updatedCredentials = new ArrayList<ResourceState>();

        ResourceState updatedCredentialA = new DefaultResourceState();
        updatedCredentialA.putProperty("mechanism", "GSSAPI");
        updatedCredentialA.putProperty("username", "userX");
        updatedCredentials.add(updatedCredentialA);

        updatedConfig.putProperty("credentials", updatedCredentials);

        ResourceState updatedResult = client.update(new RequestContext.Builder().build(), ADMIN_PATH, updatedConfig);

        assertThat(updatedResult.getProperty("credentials")).isNotNull();
        assertThat(((List) updatedResult.getProperty("credentials")).size()).isEqualTo(1);
        ResourceState credentialResultA = (ResourceState) ((List) updatedResult.getProperty("credentials")).get(0);
        assertThat(credentialResultA.getPropertyNames().size()).isEqualTo(2);
        assertThat(credentialResultA.getProperty("username")).isEqualTo("userX");
        assertThat(credentialResultA.getProperty("mechanism")).isEqualTo("GSSAPI");
    }

    @Test
    public void testClearCredentials() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testClearCredentials");
        config.putProperty("servers", new ArrayList());

        List<ResourceState> credentials = new ArrayList<>();
        ResourceState credential = new DefaultResourceState();
        credential.putProperty("mechanism", "MONGODB-CR");
        credential.putProperty("username", "foo");
        credential.putProperty("password", "bar");
        credential.putProperty("database", "testDB1");

        credentials.add(credential);
        config.putProperty("credentials", credentials);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);
        assertThat(result.getProperty("db")).isEqualTo("testClearCredentials");

        assertThat(result.getProperty("credentials")).isNotNull();
        assertThat(((List) result.getProperty("credentials")).size()).isEqualTo(1);
        ResourceState credentialResult = (ResourceState) ((List) result.getProperty("credentials")).get(0);
        assertThat(credentialResult.getPropertyNames().size()).isEqualTo(4);
        assertThat(credentialResult.getProperty("username")).isEqualTo("foo");
        assertThat(credentialResult.getProperty("password")).isEqualTo("bar");
        assertThat(credentialResult.getProperty("database")).isEqualTo("testDB1");
        assertThat(credentialResult.getProperty("mechanism")).isEqualTo("MONGODB-CR");

        result.putProperty("credentials", new ArrayList());
        ResourceState updatedResult = client.update(new RequestContext.Builder().build(), ADMIN_PATH, result);

        assertThat(updatedResult.getProperty("db")).isEqualTo("testClearCredentials");
        assertThat((List) updatedResult.getProperty("credentials")).isEmpty();
    }

}
