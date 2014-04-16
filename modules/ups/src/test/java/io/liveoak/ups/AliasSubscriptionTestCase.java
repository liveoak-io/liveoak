package io.liveoak.ups;

import io.liveoak.common.DefaultSecurityContext;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.NotAcceptableException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;

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
        assertThat(result.getPropertyNames().contains("user"));
        assertThat(result.getProperty( "user" )).isEqualTo( "User A" );

        // check that other calls to create a resource return the same one
        ResourceState anotherResult = client.create(requestContext, "/testApp/" + BASEPATH + "/aliases", new DefaultResourceState());
        ResourceState aThirdResult =  client.create(requestContext, "/testApp/" + BASEPATH + "/aliases", new DefaultResourceState());

        assertThat(result.toString()).isEqualTo( anotherResult.toString() );
        assertThat(anotherResult.toString()).isEqualTo( aThirdResult.toString() );
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
        // check that the resource path matches what we specified
        assertThat(subscriptionResponse.getProperty( "resource-path" )).isEqualTo("/testApp/storage/*");
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

        ResourceState subscriptionResponse = client.create( new RequestContext.Builder().build(),"/testApp/" + BASEPATH + "/aliases/" + alias, subscriptionRequest);

        assertThat( subscriptionResponse.id() ).isEqualTo("mySubscription");
        assertThat( subscriptionResponse.members() ).isEmpty();

        // check that the alias matches the alias got back from the system
        String aliasResponse = (String)((List )subscriptionResponse.getProperty( "alias" )).get(0);
        assertThat(aliasResponse).isEqualTo( alias );
        // check that the resource path matches what we specified
        assertThat(subscriptionResponse.getProperty( "resource-path" )).isEqualTo("/testApp/storage/*");
    }

}
