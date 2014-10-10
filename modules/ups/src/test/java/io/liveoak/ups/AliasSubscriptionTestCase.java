package io.liveoak.ups;

import io.liveoak.common.security.DefaultSecurityContext;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.DefaultReturnFields;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.exceptions.NotAcceptableException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.ResourceNotFoundException;
import io.liveoak.spi.state.ResourceState;
import org.fest.assertions.Fail;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class AliasSubscriptionTestCase extends BaseUPSTestCase {

    @Test
    public void testUnauthenticatedUsers() throws Exception {
        // unauthenticated users should all get random aliases
        ResourceState result = client.create( new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/aliases", new DefaultResourceState());

        // verify response
        assertThat( result ).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.id()).isNotEmpty();
        assertThat( result.members() ).isEmpty();
        assertThat( result.getPropertyNames().size()).isEqualTo(1);
        assertThat(result.getPropertyNames().contains("user"));
        assertThat(result.getProperty( "user" )).isEqualTo( null );

        // check that other unauthenticated users get different aliases
        ResourceState anotherResult = client.create( new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/aliases", new DefaultResourceState());
        ResourceState aThirdResult =  client.create( new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/aliases", new DefaultResourceState());

        assertThat(result.id()).isNotEqualTo(anotherResult.id());
        assertThat(anotherResult.id()).isNotEqualTo(aThirdResult.id());
        assertThat(aThirdResult.id()).isNotEqualTo(result.id());
    }

    @Test
    public void testAuthenticatedUsers() throws Exception {
        // authenticated users should be assigned a static alias

        DefaultSecurityContext userASecurityContext = new DefaultSecurityContext();
        userASecurityContext.setSubject("User A");
        userASecurityContext.setRealm("TestRealm");

        RequestContext requestContext = new RequestContext.Builder().securityContext(userASecurityContext).build();

        ResourceState result = client.create(requestContext, "/testApp/" + BASEPATH + "/aliases", new DefaultResourceState());

        // verify response
        assertThat( result ).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.id()).isNotEmpty();
        assertThat(result.members()).isEmpty();
        assertThat(result.getPropertyNames().size()).isEqualTo(1);
        assertThat(result.getPropertyNames().contains("subject"));
        assertThat(result.getProperty( "subject" )).isEqualTo( "User A" );

        // check that other calls to create a resources return the same one
        ResourceState anotherResult = client.create(requestContext, "/testApp/" + BASEPATH + "/aliases", new DefaultResourceState());
        ResourceState aThirdResult =  client.create(requestContext, "/testApp/" + BASEPATH + "/aliases", new DefaultResourceState());

        assertThat( result.toString() ).isEqualTo( anotherResult.toString() );
        assertThat(anotherResult.toString()).isEqualTo( aThirdResult.toString() );
    }

    @Test
    public void testDeleteAlias() throws Exception {
        // unauthenticated users should all get random aliases
        ResourceState result = client.create( new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/aliases", new DefaultResourceState());

        ResourceState deleteResponse = client.delete(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/aliases/" + result.id());
        assertThat( deleteResponse ).isNotNull();
        assertThat(deleteResponse.id()).isNotNull();
        assertThat(deleteResponse.id()).isNotEmpty();
        assertThat( deleteResponse.members() ).isEmpty();
        assertThat( deleteResponse.getPropertyNames().size()).isEqualTo(1);
        assertThat(deleteResponse.getPropertyNames().contains("user"));
        assertThat(deleteResponse.getProperty( "user" )).isEqualTo( null );
    }
      
    @Test
    public void testSettingAlias() throws Exception {
        // user should not be able to set their own alias, it should be an opaque value returned by the system
        // test with unauthenticated user
        try {
            client.create( new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/aliases", new DefaultResourceState( "myAlias" ) );
            fail();
        } catch ( NotAcceptableException e ) {
            //expected
        }

        // test with authenticated user
        DefaultSecurityContext userASecurityContext = new DefaultSecurityContext();
        userASecurityContext.setSubject("User A");
        userASecurityContext.setRealm("TestRealm");
        RequestContext requestContext = new RequestContext.Builder().securityContext(userASecurityContext).build();
        try {
            client.create( requestContext, "/testApp/" + BASEPATH + "/aliases", new DefaultResourceState( "myAlias" ) );
            fail();
        } catch ( NotAcceptableException e ) {
            //expected
        }
    }

    @Test
    public void testSimpleAliasSubscription() throws Exception {
        // test setup an subscription for a particular user.
        // this involves two steps:
        // 1) getting the alias from the system
        // 2) creating the subscription as a child of the alias

        // unauthenticated user
        ResourceState aliasResult = client.create( new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/aliases", new DefaultResourceState());
        String alias = aliasResult.id();

        ResourceState subscriptionRequest = new DefaultResourceState();
        subscriptionRequest.putProperty( "resource-path", "/testApp/storage/*" );
        ResourceState subscriptionResponse = client.create( new RequestContext.Builder().build(),"/testApp/" + BASEPATH + "/aliases/" + alias, subscriptionRequest);

        assertThat( subscriptionResponse.id() ).isNotNull();
        assertThat(subscriptionResponse.members()).isEmpty();

        // check that the alias matches the alias got back from the system
        String aliasResponse = (String)((List )subscriptionResponse.getProperty( "alias" )).get(0);
        assertThat(aliasResponse).isEqualTo( alias );
        // check that the resources path matches what we specified
        assertThat(subscriptionResponse.getProperty( "resource-path" )).isEqualTo("/testApp/storage/*");

        ResourceState readResponse = client.read( new RequestContext.Builder().returnFields( ( new DefaultReturnFields( "*" ).withExpand( LiveOak.MEMBERS ) ) ).build(), "/testApp/" + BASEPATH + "/aliases/" + alias );
        assertThat(readResponse.id()).isEqualTo(aliasResult.id());
        assertThat(readResponse.members().size()).isEqualTo(1);
        assertThat( readResponse.members().get( 0 ).toString() ).isEqualTo(subscriptionResponse.toString());
    }

    @Test
    public void testSettingAliasInSubscription() throws Exception {
        // test setup an subscription for a particular user.
        // this involves two steps:
        // 1) getting the alias from the system
        // 2) creating the subscription as a child of the alias

        // unauthenticated user
        ResourceState aliasResult = client.create( new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/aliases", new DefaultResourceState());
        String alias = aliasResult.id();

        // setting a custom alias here is not allowed!
        ResourceState subscriptionRequest = new DefaultResourceState();
        subscriptionRequest.putProperty( "resource-path", "/testApp/storage/*" );
        subscriptionRequest.putProperty( "alias", "myAlias");
        try {
            ResourceState subscriptionResponse = client.create( new RequestContext.Builder().build(),"/testApp/" + BASEPATH + "/aliases/" + alias, subscriptionRequest);
            fail();
        } catch (NotAcceptableException e) {
            //expected
        }

        // having the alias already set to the actual alias should not cause a problem
        // otherwise updates would always fail if included
        subscriptionRequest.putProperty( "alias", alias );
        ResourceState subscriptionResponse = client.create( new RequestContext.Builder().build(),"/testApp/" + BASEPATH + "/aliases/" + alias, subscriptionRequest);
    }

    @Test
    public void testComplexAliasSubscription() throws Exception {
        // unauthenticated user
        ResourceState aliasResult = client.create( new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/aliases", new DefaultResourceState());
        String alias = aliasResult.id();

        ResourceState subscriptionRequest = new DefaultResourceState("mySubscription");
        subscriptionRequest.putProperty( "resource-path", "/testApp/storage/*" );

        // add some variants
        List variants = Arrays.asList( "variantA", "variantB" );
        subscriptionRequest.putProperty( "variants", variants );

        // add some categories
        List categories = Arrays.asList( "phone", "small tablet", "large tablet" );
        subscriptionRequest.putProperty( "categories", categories );

        // add some device types
        List deviceTypes = Arrays.asList( "Android Phone", "iOS Phone" );
        subscriptionRequest.putProperty( "device-type", deviceTypes );

        // add the message
        ResourceState message = new DefaultResourceState();
        message.putProperty( "alert", "Hello" );
        message.putProperty( "myCustomProp", "foobar" );
        subscriptionRequest.putProperty( "message", message );

        ResourceState subscriptionResponse = client.create( new RequestContext.Builder().build(),"/testApp/" + BASEPATH + "/aliases/" + alias, subscriptionRequest);

        assertThat( subscriptionResponse.id() ).isEqualTo("mySubscription");
        assertThat( subscriptionResponse.members() ).isEmpty();

        // check that the alias matches the alias got back from the system
        String aliasResponse = (String)((List )subscriptionResponse.getProperty( "alias" )).get(0);
        assertThat(aliasResponse).isEqualTo( alias );
        // check that the resources path matches what we specified
        assertThat(subscriptionResponse.getProperty( "resource-path" )).isEqualTo("/testApp/storage/*");
        assertThat( subscriptionResponse.getProperty( "variants" ) ).isEqualTo( Arrays.asList( "variantA", "variantB" ) );
        assertThat( ( List ) subscriptionResponse.getProperty( "categories" ) ).containsOnly( "phone", "small tablet", "large tablet" );
        assertThat( subscriptionResponse.getProperty( "device-type" ) ).isEqualTo( Arrays.asList( "Android Phone", "iOS Phone" ) );
        ResourceState messageResult = ( ResourceState ) subscriptionResponse.getProperty( "message" );
        assertThat( messageResult.getProperty( "alert" ) ).isEqualTo( "Hello" );
        assertThat( messageResult.getProperty( "myCustomProp" ) ).isEqualTo( "foobar" );
    }

    @Test
    public void testDeleteAliasSubscription() throws Exception {
        // unauthenticated user
        ResourceState aliasResult = client.create( new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/aliases", new DefaultResourceState());
        String alias = aliasResult.id();

        ResourceState subscriptionRequest = new DefaultResourceState();
        subscriptionRequest.putProperty( "resource-path", "/testApp/storage/*" );
        ResourceState subscriptionResponse = client.create( new RequestContext.Builder().build(),"/testApp/" + BASEPATH + "/aliases/" + alias, subscriptionRequest);

        assertThat( subscriptionResponse.id() ).isNotNull();
        assertThat(subscriptionResponse.members()).isEmpty();

        ResourceState deleteResponse = client.delete( new RequestContext.Builder().build(),"/testApp/" + BASEPATH + "/aliases/" + alias + "/" + subscriptionResponse.id());
        assertThat(deleteResponse).isNotNull();
        assertThat(deleteResponse.id()).isEqualTo( subscriptionResponse.id());
        assertThat(deleteResponse.getProperty( "resource-path" )).isEqualTo( subscriptionResponse.getProperty( "resource-path" ) );

        try {
            client.delete( new RequestContext.Builder().build(),"/testApp/" + BASEPATH + "/aliases/" + alias + "/" + subscriptionResponse.id());
            Fail.fail();
        } catch (ResourceNotFoundException e) {
            //expected
        }
    }

    @Test
    public void testUpdateAliasSubscription() throws Exception {
        // unauthenticated user
        ResourceState aliasResult = client.create( new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/aliases", new DefaultResourceState());
        String alias = aliasResult.id();

        ResourceState subscriptionRequest = new DefaultResourceState();
        subscriptionRequest.putProperty( "resource-path", "/testApp/storage/*" );
        ResourceState subscriptionResponse = client.create( new RequestContext.Builder().build(),"/testApp/" + BASEPATH + "/aliases/" + alias, subscriptionRequest);

        assertThat( subscriptionResponse.id() ).isNotNull();
        assertThat(subscriptionResponse.members()).isEmpty();

        subscriptionResponse.putProperty( "resource-path", "/some/new/resources/path" );

        // check the update and its response
        ResourceState updateResponse = client.update( new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/aliases/" + alias + "/" + subscriptionResponse.id(), subscriptionResponse );
        assertThat(updateResponse).isNotNull();
        assertThat(updateResponse.id()).isEqualTo( subscriptionResponse.id());
        assertThat(updateResponse.getProperty( "resource-path" )).isEqualTo("/some/new/resources/path");

        // check a read of the updated resources
        ResourceState readResponse = client.read( new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/aliases/" + alias + "/" + subscriptionResponse.id() );
        assertThat(readResponse).isNotNull();
        assertThat(readResponse.id()).isEqualTo( subscriptionResponse.id());
        assertThat(readResponse.getProperty( "resource-path" )).isEqualTo("/some/new/resources/path");
    }

}
