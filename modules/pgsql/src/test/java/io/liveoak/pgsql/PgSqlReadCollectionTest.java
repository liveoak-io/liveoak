package io.liveoak.pgsql;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

import io.liveoak.common.DefaultReturnFields;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PgSqlReadCollectionTest extends BasePgSqlTest {

    private SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.S");

    @Test
    public void testReadCollection() throws Exception {

        RequestContext requestContext = new RequestContext.Builder()
                .returnFields(new DefaultReturnFields("*"))
                .build();

        String endpointParent = "/testApp/" + BASEPATH;
        String endpoint = "/testApp/" + BASEPATH + "/addresses";
        ResourceState result = client.read(requestContext, endpoint);
        System.out.println(result);

        List<ResourceState> members = result.members();

        checkMemberCount(result, 2);
        checkMemberIdPropertyAndMemberCount(endpointParent, result, "addresses", 2, 2);

        checkMemberIdPropertyAndMemberCount(endpoint, members.get(0), "1", 0, 0);
        checkMemberIdPropertyAndMemberCount(endpoint, members.get(1), "2", 0, 0);


        endpoint = "/testApp/" + BASEPATH + "/" + schema_two + ".orders";
        result = client.read(requestContext, endpoint);
        System.out.println(result);

        members = result.members();
        checkMemberCount(result, 3);
        checkMemberIdPropertyAndMemberCount(endpointParent, result, schema_two + ".orders", 2, 3);

        checkMemberIdPropertyAndMemberCount(endpoint, members.get(0), "014-1003095", 0, 0);
        checkMemberIdPropertyAndMemberCount(endpoint, members.get(1), "014-2004096", 0, 0);
        checkMemberIdPropertyAndMemberCount(endpoint, members.get(2), "014-2004345", 0, 0);


        // addresses - expand members
        requestContext = new RequestContext.Builder()
                .returnFields(new DefaultReturnFields("*(*)"))
                .build();

        endpoint = "/testApp/" + BASEPATH + "/addresses";
        result = client.read(requestContext, endpoint);
        System.out.println(result);

        checkAddressItems(endpointParent, endpoint, result);


        // addresses - expand item fields
        requestContext = new RequestContext.Builder()
                .returnFields(new DefaultReturnFields("*(*(*))"))
                .build();

        result = client.read(requestContext, endpoint);
        System.out.println(result);

        checkAddressItems(endpointParent, endpoint, result);


        // orders - expand members
        requestContext = new RequestContext.Builder()
                .returnFields(new DefaultReturnFields("*(*)"))
                .build();

        endpoint = "/testApp/" + BASEPATH + "/" + schema_two + ".orders";
        result = client.read(requestContext, endpoint);
        System.out.println(result);

        members = checkOrdersExpanded(endpointParent, endpoint, result);

        // check that all properties are returned on items, and have proper values of proper types
        checkMember(members.get(0), new Object[]{
                "order_id", "014-1003095",
                "create_date", new Timestamp(iso.parse("2014-06-07 15:10:15.0").getTime()),
                "total", 18990L,
                "addresses", resourceRef("/testApp/" + BASEPATH + "/addresses/1")}); // TODO: addresses should return a {self: {href: '...'}}

        checkMember(members.get(1), new Object[]{
                "order_id", "014-2004096",
                "create_date", new Timestamp(iso.parse("2014-04-02 11:06:12.0").getTime()),
                "total", 43800L,
                "addresses", resourceRef("/testApp/" + BASEPATH + "/addresses/2")}); // TODO: addresses should return a {self: {href: '...'}}

        checkMember(members.get(2), new Object[]{
                "order_id", "014-2004345",
                "create_date", new Timestamp(iso.parse("2014-06-01 18:06:12.0").getTime()),
                "total", 32500L,
                "addresses", resourceRef("/testApp/" + BASEPATH + "/addresses/2")}); // TODO: addresses should return a {self: {href: '...'}}


        // orders - expand item fields
        endpoint = "/testApp/" + BASEPATH + "/" + schema_two + ".orders";
        requestContext = new RequestContext.Builder()
                .returnFields(new DefaultReturnFields("*(*(*))"))
                .build();

        result = client.read(requestContext, endpoint);
        System.out.println(result);

        members = checkOrdersExpanded(endpointParent, endpoint, result);

        // expected expanded address items
        ResourceState addr1 = buildResourceState("1", "/testApp/" + BASEPATH + "/addresses/1", new Object[]{
                "address_id", 1,
                "name", "John F. Doe",
                "street", "Liveoak street 7",
                "postcode", null,
                "city", "London",
                "country_iso", "UK",
                "is_company", false});

        ResourceState addr2 = buildResourceState("2", "/testApp/" + BASEPATH + "/addresses/2", new Object[]{
                "address_id", 2,
                "name", "Lombaas Inc.",
                "street", "Liveoak square 1",
                "postcode", "94114",
                "city", "San Francisco",
                "country_iso", "US",
                "is_company", true});

        // check that all properties are returned with proper values
        checkMember(members.get(0), new Object[]{
                "order_id", "014-1003095",
                "create_date", new Timestamp(iso.parse("2014-06-07 15:10:15.0").getTime()),
                "total", 18990L,
                "addresses", addr1});

        checkMember(members.get(1), new Object[]{
                "order_id", "014-2004096",
                "create_date", new Timestamp(iso.parse("2014-04-02 11:06:12.0").getTime()),
                "total", 43800L,
                "addresses", addr2});

        checkMember(members.get(2), new Object[]{
                "order_id", "014-2004345",
                "create_date", new Timestamp(iso.parse("2014-06-01 18:06:12.0").getTime()),
                "total", 32500L,
                "addresses", addr2});

    }

    public List<ResourceState> checkOrdersExpanded(String endpointParent, String endpoint, ResourceState result) {
        List<ResourceState> members;
        members = result.members();
        checkMemberCount(result, 3);
        checkMemberIdPropertyAndMemberCount(endpointParent, result, schema_two + ".orders", 2, 3);

        checkMemberIdPropertyAndMemberCount(endpoint, members.get(0), "014-1003095", 4, 0);
        checkMemberIdPropertyAndMemberCount(endpoint, members.get(1), "014-2004096", 4, 0);
        checkMemberIdPropertyAndMemberCount(endpoint, members.get(2), "014-2004345", 4, 0);
        return members;
    }

    public void checkAddressItems(String endpointParent, String endpoint, ResourceState result) {
        List<ResourceState> members;
        members = result.members();
        checkMemberCount(result, 2);
        checkMemberIdPropertyAndMemberCount(endpointParent, result, "addresses", 2, 2);

        checkMemberIdPropertyAndMemberCount(endpoint, members.get(0), "1", 7, 0);
        checkMemberIdPropertyAndMemberCount(endpoint, members.get(1), "2", 7, 0);

        // check that all properties are returned on items, and have proper values of proper types
        checkMember(members.get(0), new Object[]{
                "address_id", 1,
                "name", "John F. Doe",
                "street", "Liveoak street 7",
                "postcode", null,
                "city", "London",
                "country_iso", "UK",
                "is_company", false});

        checkMember(members.get(1), new Object[] {
                "address_id", 2,
                "name", "Lombaas Inc.",
                "street", "Liveoak square 1",
                "postcode", "94114",
                "city", "San Francisco",
                "country_iso", "US",
                "is_company", true});
    }

    private void checkMember(ResourceState resourceState, Object[] objects) {
        int count = objects.length / 2;
        assertThat(resourceState.getPropertyNames().size()).isEqualTo(count);
        for (int i = 0; i < count; i++) {
            String key = (String) objects[2*i];
            Object val = objects[2*i + 1];
            assertThat(resourceState.getProperty(key)).isEqualTo(val).describedAs(key + " == " + val);
        }
    }

    private ResourceState buildResourceState(String id, String uri, Object[] objects) throws URISyntaxException {
        DefaultResourceState state = new DefaultResourceState(id);
        state.uri(new URI(uri));
        assertThat(objects.length % 2).isEqualTo(0).describedAs("even objects count - as it really represents key-value pairs");
        int count = objects.length / 2;
        for (int i = 0; i < count; i++) {
            String key = (String) objects[2*i];
            Object val = objects[2*i + 1];
            state.putProperty(key, val);
        }
        return state;
    }

    public void checkMemberCount(ResourceState result, int count) {
        assertThat(result.getPropertyAsInteger("count")).isEqualTo(count);
        assertThat(result.members().size()).isEqualTo(count);
    }

    public void checkMemberIdPropertyAndMemberCount(String endpoint, ResourceState member, String id, int propCount, int memberCount) {
        assertThat(member.id()).isEqualTo(id);
        assertThat(member.uri().toString()).isEqualTo(endpoint + "/" + id);
        assertThat(member.getPropertyNames().size()).isEqualTo(propCount);
        assertThat(member.members().size()).isEqualTo(memberCount);
    }
}