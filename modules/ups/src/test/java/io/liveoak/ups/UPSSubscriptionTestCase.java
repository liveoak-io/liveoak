package io.liveoak.ups;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class UPSSubscriptionTestCase extends BaseUPSTestCase {

    @Test
    public void testGetEmptySubscriptions() throws Exception {

        ResourceState result = client.read( new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/subscriptions" );

        // verify response
        assertThat( result ).isNotNull();
        assertThat( result.id() ).isEqualTo( "subscriptions" );
        assertThat( result.members() ).isEmpty();
        assertThat( result.getPropertyNames() ).isEmpty();
    }

    @Test
    public void testAddSubscriptions() throws Exception {

        ResourceState subscription = new DefaultResourceState();
        // add the resource path
        subscription.putProperty( "resourcePath", "/foo/bar" );

        // add some aliases
        List aliases = Arrays.asList( "myAliasA", "myAliasB" );
        subscription.putProperty( "alias", aliases );

        // add some variants
        List variants = Arrays.asList( "variantA", "variantB" );
        subscription.putProperty( "variants", variants );

        // add some categories
        List categories = Arrays.asList( "phone", "small tablet", "large tablet" );
        subscription.putProperty( "categories", categories );

        // add some device types
        List deviceTypes = Arrays.asList( "Android Phone", "iOS Phone" );
        subscription.putProperty( "deviceType", deviceTypes );

        // add the message
        ResourceState message = new DefaultResourceState();
        message.putProperty( "alert", "Hello" );
        message.putProperty( "myCustomProp", "foobar" );
        subscription.putProperty( "message", message );

        ResourceState result = client.create( new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/subscriptions", subscription );

        // verify response
        assertThat( result ).isNotNull();
        assertThat( result.members() ).isEmpty();
        assertThat( result.getPropertyNames() ).isNotEmpty();
        assertThat( result.getProperty( "resourcePath" ) ).isEqualTo( "/foo/bar" );
        assertThat( result.getProperty( "alias" ) ).isEqualTo( Arrays.asList( "myAliasA", "myAliasB" ) );
        assertThat( result.getProperty( "variants" ) ).isEqualTo( Arrays.asList( "variantA", "variantB" ) );
        assertThat( ( List ) result.getProperty( "categories" ) ).containsOnly( "phone", "small tablet", "large tablet" );
        assertThat( result.getProperty( "deviceType" ) ).isEqualTo( Arrays.asList( "Android Phone", "iOS Phone" ) );
        ResourceState messageResult = ( ResourceState ) result.getProperty( "message" );
        assertThat( messageResult.getProperty( "alert" ) ).isEqualTo( "Hello" );
        assertThat( messageResult.getProperty( "myCustomProp" ) ).isEqualTo( "foobar" );
    }

    @Test
    public void testConfig() throws Exception {
        ResourceState result = client.read( new RequestContext.Builder().build(), ADMIN_PATH );

        //verify response
        assertThat( result ).isNotNull();
        assertThat( result.members() ).isEmpty();
        assertThat( result.getPropertyNames() ).isNotEmpty();
        assertThat( result.getProperty( "upsServerURL" ) ).isEqualTo( "http://localhost:8080/my_ups_server" );
        assertThat( result.getProperty( "applicationId" ) ).isEqualTo( "my-application-id" );
        assertThat( result.getProperty( "masterSecret" ) ).isEqualTo( "shhhh-its-a-secret" );

    }


}
