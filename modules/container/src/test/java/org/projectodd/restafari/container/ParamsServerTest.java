package org.projectodd.restafari.container;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.junit.Test;
import org.projectodd.restafari.spi.state.CollectionResourceState;
import org.projectodd.restafari.spi.state.ObjectResourceState;
import org.projectodd.restafari.spi.state.ResourceState;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class ParamsServerTest extends BasicServerTest {

    @Test
    public void testServer() throws Exception {

        Header header = new BasicHeader("Accept", "application/json");

        HttpGet getRequest = null;
        HttpPost postRequest = null;
        HttpPut putRequest = null;
        CloseableHttpResponse response = null;


        // create people collection with direct PUT
        System.err.println("CREATE /people collection");

        putRequest = new HttpPut("http://localhost:8080/memory/people");
        putRequest.setEntity(new StringEntity("{ \"content\": [] }"));
        putRequest.setHeader( "Content-Type", "application/json" );
        response = this.httpClient.execute(putRequest);
        System.err.println( "response: " + response );
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);

        response.close();

        System.err.println("PREPARE 2 people ...");
        // Post a person

        postRequest = new HttpPost( "http://localhost:8080/memory/people");
        postRequest.setEntity( new StringEntity("{ \"name\": \"bob\" }" ) );
        postRequest.setHeader( "Content-Type", "application/json" );

        response = httpClient.execute( postRequest );
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);

        ObjectResourceState bobState = (ObjectResourceState) decode(response);
        assertThat(bobState).isNotNull();

        assertThat(bobState.getProperty("id")).isNotNull();
        assertThat(bobState.getProperty("name")).isEqualTo("bob");

        response.close();


        postRequest = new HttpPost( "http://localhost:8080/memory/people");
        postRequest.setEntity( new StringEntity("{ \"name\": \"krusty\" }" ) );
        postRequest.setHeader( "Content-Type", "application/json" );

        response = httpClient.execute( postRequest );
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);

        ObjectResourceState krustyState = (ObjectResourceState) decode(response);
        assertThat(krustyState).isNotNull();

        assertThat(krustyState.getProperty("id")).isNotNull();
        assertThat(krustyState.getProperty("name")).isEqualTo("krusty");

        response.close();


        System.err.println("TEST #1");
        // test pagination

        // now that we have two people we can do paging requests

        // Retrieve first people resource, ensuring only the first one is returned
        // Assumption: unsorted GET on collection returns items in the order they were added to collection
        getRequest = new HttpGet("http://localhost:8080/memory/people?limit=1");
        getRequest.addHeader(header);
        response = httpClient.execute(getRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);

        ResourceState state = decode(response);
        assertThat(state).isNotNull();
        assertThat(state).isInstanceOf(CollectionResourceState.class);
        List<? extends ResourceState> members = ((CollectionResourceState) state).members().collect(Collectors.toList());

        assertThat(members.size()).isEqualTo(1);

        ResourceState memberState = members.get(0);
        assertThat(memberState).isInstanceOf(ObjectResourceState.class);

        ObjectResourceState member = (ObjectResourceState) memberState;
        assertThat(member.id()).isEqualTo(bobState.id());

        response.close();

        // Retrieve second people resource, ensuring only the second one is returned
        // Assumption: unsorted GET on collection returns items in the order they were added to collection
        getRequest = new HttpGet("http://localhost:8080/memory/people?offset=1&limit=1");
        getRequest.addHeader(header);
        response = httpClient.execute(getRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);

        state = decode(response);
        assertThat(state).isNotNull();
        assertThat(state).isInstanceOf(CollectionResourceState.class);

        members = ((CollectionResourceState) state).members().collect(Collectors.toList());
        assertThat(members.size()).isEqualTo(1);

        memberState = members.get(0);
        assertThat(memberState).isInstanceOf(ObjectResourceState.class);

        member = (ObjectResourceState) memberState;
        assertThat(member.id()).isEqualTo(krustyState.id());

        response.close();



        System.err.println("TEST #2");
        // test specifying fields to return

        getRequest = new HttpGet("http://localhost:8080/memory/people/" + krustyState.getProperty("id") + "?fields=id");
        getRequest.addHeader(header);
        response = httpClient.execute(getRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);

        state = decode(response);
        assertThat(state).isNotNull();
        assertThat(state).isInstanceOf(ObjectResourceState.class);

        HashSet fields = new HashSet();
        ((ObjectResourceState) state).members().forEach((f) -> {
            fields.add(f.id());
        });
        // TODO: uncomment when _self, and id encoding takes ReturnFields into account
        //assertThat(fields.size()).isEqualTo(1);
        //assertThat(fields.contains("id")).isTrue();
        //assertThat(fields.contains("name")).isFalse();
    }
}
