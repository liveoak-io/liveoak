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
public class MongoConfigCredentialsTest extends BaseMongoConfigTest {

    @Test
    public void testDefault() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testDefaultDB");
        setUpSystem( config );

        ResourceState result = client.read(new RequestContext.Builder().build(), BASEPATH + ";config");
        assertThat( result.getProperty( "db" ) ).isEqualTo("testDefaultDB");

        // by default it should exist but be empty
        assertThat(result.getProperty("credentials")).isNotNull();
        assertThat((List )result.getProperty("credentials")).isEmpty();
    }

    @Test
    public void testConfigureCredentialCR() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testConfigureCRDB");

        List<ResourceState> credentials = new ArrayList<ResourceState>();
        ResourceState credential = new DefaultResourceState();
        credential.putProperty("mechanism", "MONGODB-CR");
        credential.putProperty("username", "foo");
        credential.putProperty("password", "bar");
        credential.putProperty("database", "testConfigureCRDB");

        credentials.add(credential);
        config.putProperty( "credentials", credentials );

        setUpSystem( config );

        ResourceState result = client.read(new RequestContext.Builder().build(), BASEPATH + ";config");
        assertThat( result.getProperty( "db" ) ).isEqualTo("testConfigureCRDB");

        // by default it should exist but be empty
        assertThat(result.getProperty("credentials")).isNotNull();
        assertThat(((List )result.getProperty("credentials")).size()).isEqualTo(1);
        ResourceState credentialResult = (ResourceState)(( List ) result.getProperty("credentials") ).get(0);
        assertThat(credentialResult.getPropertyNames().size()).isEqualTo( 4 );
        assertThat( credentialResult.getProperty( "username" ) ).isEqualTo( "foo" );
        assertThat(credentialResult.getProperty( "password" )).isEqualTo( "bar");
        assertThat(credentialResult.getProperty( "database" )).isEqualTo( "testConfigureCRDB" );
        assertThat(credentialResult.getProperty("mechanism")).isEqualTo("MONGODB-CR");
    }

    @Test
    public void testConfigureCredentialGSS() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testConfigureGSSDB");

        List<ResourceState> credentials = new ArrayList<ResourceState>();
        ResourceState credential = new DefaultResourceState();
        credential.putProperty("mechanism", "GSSAPI");
        credential.putProperty("username", "foobar");

        credentials.add(credential);
        config.putProperty( "credentials", credentials );

        setUpSystem( config );

        ResourceState result = client.read(new RequestContext.Builder().build(), BASEPATH + ";config");
        assertThat( result.getProperty( "db" ) ).isEqualTo("testConfigureGSSDB");

        // by default it should exist but be empty
        assertThat(result.getProperty("credentials")).isNotNull();
        assertThat(((List )result.getProperty("credentials")).size()).isEqualTo(1);
        ResourceState credentialResult = (ResourceState)(( List ) result.getProperty("credentials") ).get(0);
        assertThat(credentialResult.getPropertyNames().size()).isEqualTo( 2 );
        assertThat(credentialResult.getProperty( "username" )).isEqualTo( "foobar" );
        assertThat(credentialResult.getProperty("mechanism")).isEqualTo("GSSAPI");
    }

    @Test
    public void testConfigureMultipleCredentials() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "ConfigureMultipleCredentialsDB");

        List<ResourceState> credentials = new ArrayList<ResourceState>();
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

        config.putProperty( "credentials", credentials );
        setUpSystem( config );

        ResourceState result = client.read(new RequestContext.Builder().build(), BASEPATH + ";config");
        assertThat( result.getProperty( "db" ) ).isEqualTo("ConfigureMultipleCredentialsDB");

        // by default it should exist but be empty
        assertThat(result.getProperty("credentials")).isNotNull();
        assertThat(((List )result.getProperty("credentials")).size()).isEqualTo(3);
        ResourceState credentialResultA = (ResourceState)(( List ) result.getProperty("credentials") ).get(1);
        assertThat(credentialResultA.getPropertyNames().size()).isEqualTo( 2 );
        assertThat(credentialResultA.getProperty( "username" )).isEqualTo( "userA" );
        assertThat(credentialResultA.getProperty("mechanism")).isEqualTo("GSSAPI");

        ResourceState credentialResultB = (ResourceState)(( List ) result.getProperty("credentials") ).get(2);
        assertThat(credentialResultB.getPropertyNames().size()).isEqualTo( 4 );
        assertThat(credentialResultB.getProperty( "username" )).isEqualTo( "userB" );
        assertThat(credentialResultB.getProperty( "password" )).isEqualTo( "pwB");
        assertThat(credentialResultB.getProperty( "database" )).isEqualTo( "databaseB" );
        assertThat(credentialResultB.getProperty("mechanism")).isEqualTo("MONGODB-CR");

        ResourceState credentialResultC = (ResourceState)(( List ) result.getProperty("credentials") ).get(0);
        assertThat(credentialResultC.getPropertyNames().size()).isEqualTo( 4 );
        assertThat(credentialResultC.getProperty( "username" )).isEqualTo( "userC" );
        assertThat(credentialResultC.getProperty( "password" )).isEqualTo( "pwC");
        assertThat(credentialResultC.getProperty( "database" )).isEqualTo( "databaseC" );
        assertThat(credentialResultC.getProperty("mechanism")).isEqualTo("MONGODB-CR");
    }

    @Test
    public void testUpdateCredentials() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "ConfigureMultipleCredentialsDB");

        List<ResourceState> credentials = new ArrayList<ResourceState>();
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

        config.putProperty( "credentials", credentials );
        setUpSystem( config );

        ResourceState result = client.read(new RequestContext.Builder().build(), BASEPATH + ";config");
        assertThat( result.getProperty( "db" ) ).isEqualTo("ConfigureMultipleCredentialsDB");


        ResourceState updatedConfig = new DefaultResourceState();
        List<ResourceState> updatedCredentials = new ArrayList<ResourceState>();

        ResourceState updatedCredentialA = new DefaultResourceState();
        updatedCredentialA.putProperty("mechanism", "GSSAPI");
        updatedCredentialA.putProperty("username", "userX");
        updatedCredentials.add(credentialA);

        updatedConfig.putProperty("credentials", updatedCredentials);

        ResourceState updatedResult = client.update(new RequestContext.Builder().build(), BASEPATH + ";config", updatedConfig);


        // by default it should exist but be empty
        assertThat( result.getProperty( "credentials" ) ).isNotNull();
        assertThat(((List )result.getProperty("credentials")).size()).isEqualTo(3);
        ResourceState credentialResultA = (ResourceState)(( List ) result.getProperty("credentials") ).get(1);
        assertThat(credentialResultA.getPropertyNames().size()).isEqualTo( 2 );
        assertThat(credentialResultA.getProperty( "username" )).isEqualTo( "userA" );
        assertThat(credentialResultA.getProperty("mechanism")).isEqualTo("GSSAPI");

        ResourceState credentialResultB = (ResourceState)(( List ) result.getProperty("credentials") ).get(2);
        assertThat(credentialResultB.getPropertyNames().size()).isEqualTo( 4 );
        assertThat(credentialResultB.getProperty( "username" )).isEqualTo( "userB" );
        assertThat(credentialResultB.getProperty( "password" )).isEqualTo( "pwB");
        assertThat(credentialResultB.getProperty( "database" )).isEqualTo( "databaseB" );
        assertThat(credentialResultB.getProperty("mechanism")).isEqualTo("MONGODB-CR");

        ResourceState credentialResultC = (ResourceState)(( List ) result.getProperty("credentials") ).get(0);
        assertThat(credentialResultC.getPropertyNames().size()).isEqualTo( 4 );
        assertThat(credentialResultC.getProperty( "username" )).isEqualTo( "userC" );
        assertThat(credentialResultC.getProperty( "password" )).isEqualTo( "pwC");
        assertThat(credentialResultC.getProperty( "database" )).isEqualTo( "databaseC" );
        assertThat(credentialResultC.getProperty("mechanism")).isEqualTo("MONGODB-CR");
    }


}
