package io.liveoak.ups;

import java.util.Arrays;
import java.util.List;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.ResourceNotFoundException;
import io.liveoak.spi.state.ResourceState;
import org.fest.assertions.Fail;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class UPSSubscriptionTestCase extends BaseUPSTestCase {

    @Test
    public void upsSubscriptionTests() throws Exception {
        // Test #1 - Get empty subscriptions
        ResourceState result = client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/subscriptions");

        // verify response
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("subscriptions");
        assertThat(result.members()).isEmpty();
        assertThat(result.getPropertyNames()).isEmpty();


        // Test #2 - Add subscriptions
        ResourceState subscription = new DefaultResourceState();
        // add the resource path
        subscription.putProperty("resource-path", "/foo/bar");

        // add some aliases
        List aliases = Arrays.asList("myAliasA", "myAliasB");
        subscription.putProperty("alias", aliases);

        // add some variants
        List variants = Arrays.asList("variantA", "variantB");
        subscription.putProperty("variants", variants);

        // add some categories
        List categories = Arrays.asList("phone", "small tablet", "large tablet");
        subscription.putProperty("categories", categories);

        // add some device types
        List deviceTypes = Arrays.asList("Android Phone", "iOS Phone");
        subscription.putProperty("device-type", deviceTypes);

        // add the message
        ResourceState message = new DefaultResourceState();
        message.putProperty("alert", "Hello");
        message.putProperty("myCustomProp", "foobar");
        subscription.putProperty("message", message);

        result = client.create(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/subscriptions", subscription);

        // verify response
        assertThat(result).isNotNull();
        assertThat(result.members()).isEmpty();
        assertThat(result.getPropertyNames()).isNotEmpty();
        assertThat(result.getProperty("resource-path")).isEqualTo("/foo/bar");
        assertThat(result.getProperty("alias")).isEqualTo(Arrays.asList("myAliasA", "myAliasB"));
        assertThat(result.getProperty("variants")).isEqualTo(Arrays.asList("variantA", "variantB"));
        assertThat((List) result.getProperty("categories")).containsOnly("phone", "small tablet", "large tablet");
        assertThat(result.getProperty("device-type")).isEqualTo(Arrays.asList("Android Phone", "iOS Phone"));
        ResourceState messageResult = (ResourceState) result.getProperty("message");
        assertThat(messageResult.getProperty("alert")).isEqualTo("Hello");
        assertThat(messageResult.getProperty("myCustomProp")).isEqualTo("foobar");


        // Test #3 - Delete subscription
        subscription = new DefaultResourceState("testDelete");
        // add the resource path
        subscription.putProperty("resource-path", "/foo/bar");

        // add some aliases
        aliases = Arrays.asList("myAliasA", "myAliasB");
        subscription.putProperty("alias", aliases);

        client.create(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/subscriptions", subscription);

        ResourceState readResult = client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/subscriptions/testDelete");
        assertThat(readResult.id()).isEqualTo("testDelete");

        ResourceState deleteResult = client.delete(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/subscriptions/testDelete");
        assertThat(deleteResult).isNotNull();

        try {
            client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/subscriptions/testDelete");
            Fail.fail();
        } catch (ResourceNotFoundException e) {
            //expected
        }


        // Test #4 - Update subscription
        subscription = new DefaultResourceState("testUpdate");
        // add the resources path
        subscription.putProperty("resource-path", "/foo/bar");

        // add some aliases
        aliases = Arrays.asList("myAliasA", "myAliasB");
        subscription.putProperty("alias", aliases);

        ResourceState createResponse = client.create(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/subscriptions", subscription);
        assertThat(createResponse).isNotNull();
        assertThat(createResponse.id()).isEqualTo("testUpdate");
        assertThat(createResponse.getProperty("resource-path")).isEqualTo("/foo/bar");

        // update the resource-path
        createResponse.putProperty("resource-path", "/foo/baz");

        ResourceState updateResponse = client.update(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/subscriptions/testUpdate", createResponse);
        assertThat(updateResponse.getProperty("resource-path")).isEqualTo("/foo/baz");

        ResourceState readResponse = client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/subscriptions/testUpdate");
        assertThat(readResponse.getProperty("resource-path")).isEqualTo("/foo/baz");


        // Test #5 - Config
        result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        //verify response
        assertThat(result).isNotNull();
        assertThat(result.getPropertyNames()).isNotEmpty();
        assertThat(result.getProperty("upsURL")).isEqualTo("http://localhost:8080/my_ups_server");
        assertThat(result.getProperty("applicationId")).isEqualTo("my-application-id");
        assertThat(result.getProperty("masterSecret")).isEqualTo("shhhh-its-a-secret");
    }

}
