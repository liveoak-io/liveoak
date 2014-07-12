package io.liveoak.pgsql;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.liveoak.common.DefaultReturnFields;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * See superclass JavaDoc for how to set up PostgreSQL for this test.
 *
 *
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PgSqlReadCollectionTest extends BasePgSqlTest {

    private SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.S");

    @Test
    public void testReadCollection() throws Exception {

        String endpoint = "/testApp/" + BASEPATH + "/addresses";
        ResourceState result = client.read(ctx("*"), endpoint);
        System.out.println(result);
        checkAddresses(endpoint, result, 1);


        // addresses - expand members

        result = client.read(ctx("*(*)"), endpoint);
        System.out.println(result);
        checkAddresses(endpoint, result, 2);


        // addresses - expand item fields

        result = client.read(ctx("*(*(*))"), endpoint);
        System.out.println(result);
        checkAddresses(endpoint, result, 3);



        endpoint = "/testApp/" + BASEPATH + "/" + schema_two + ".orders";

        result = client.read(ctx("*"), endpoint);
        System.out.println(result);
        checkOrders(endpoint, result, 1);


        // orders - expand members

        result = client.read(ctx("*(*)"), endpoint);
        System.out.println(result);
        checkOrders(endpoint, result, 2);


        // orders - expand item fields

        result = client.read(ctx("*(*(*))"), endpoint);
        System.out.println(result);
        checkOrders(endpoint, result, 3);
    }

    private RequestContext ctx(String pat) {
        return new RequestContext.Builder()
                .returnFields(new DefaultReturnFields(pat))
                .build();
    }

    private void checkOrders(String endpoint, ResourceState result, int expandDepth) throws URISyntaxException, ParseException {

        if (expandDepth < 1 || expandDepth > 3) {
            throw new IllegalArgumentException("expandDepth less than 1 or greater than 3: " + expandDepth);
        }

        ResourceState addr1 = null;
        ResourceState addr2 = null;

        if (expandDepth == 3) {
            addr1 = resource("1", "/testApp/" + BASEPATH + "/addresses", new Object[]{
                    "address_id", 1,
                    "name", "John F. Doe",
                    "street", "Liveoak street 7",
                    "postcode", null,
                    "city", "London",
                    "country_iso", "UK",
                    "is_company", false,
                    schema + ".orders", list(),
                    schema_two + ".orders", list(resourceRef("/testApp/sqldata/" + schema_two + ".orders/014-1003095"))
            });

            addr2 = resource("2", "/testApp/" + BASEPATH + "/addresses", new Object[]{
                    "address_id", 2,
                    "name", "Lombaas Inc.",
                    "street", "Liveoak square 1",
                    "postcode", "94114",
                    "city", "San Francisco",
                    "country_iso", "US",
                    "is_company", true,
                    schema + ".orders", list(),
                    schema_two + ".orders", list(
                    resourceRef("/testApp/sqldata/" + schema_two + ".orders/014-2004096"),
                    resourceRef("/testApp/sqldata/" + schema_two + ".orders/014-2004345"))
            });
        }
        ResourceState expected = resource(endpoint, new Object[] {
                        "count", 3,
                        "type", "collection"
                },
                resource("014-1003095", endpoint, expandDepth == 1 ? new Object[0] : new Object[]{
                        "order_id", "014-1003095",
                        "create_date", time("2014-06-07 15:10:15.0"),
                        "total", 18990L,
                        "addresses", expandDepth == 2 ? resourceRef("/testApp/" + BASEPATH + "/addresses/1") : addr1}),

                resource("014-2004096", endpoint, expandDepth == 1 ? new Object[0] : new Object[]{
                        "order_id", "014-2004096",
                        "create_date", time("2014-04-02 11:06:12.0"),
                        "total", 43800L,
                        "addresses", expandDepth == 2 ? resourceRef("/testApp/" + BASEPATH + "/addresses/2") : addr2}),

                resource("014-2004345", endpoint, expandDepth == 1 ? new Object[0] : new Object[]{
                        "order_id", "014-2004345",
                        "create_date", time("2014-06-01 18:06:12.0"),
                        "total", 32500L,
                        "addresses", expandDepth == 2 ? resourceRef("/testApp/" + BASEPATH + "/addresses/2") : addr2})
        );

        checkResource(result, expected);
    }


    private void checkAddresses(String endpoint, ResourceState result, int expandDepth) throws ParseException, URISyntaxException {

        if (expandDepth < 1 || expandDepth > 3) {
            throw new IllegalArgumentException("expandDepth less than 1 or greater than 3: " + expandDepth);
        }

        ResourceState order = null;
        ResourceState order_two = null;
        ResourceState order_three = null;

        if (expandDepth == 3) {
            order = resource("014-1003095", "/testApp/sqldata/" + schema_two + ".orders",
                    new Object[]{
                            "order_id", "014-1003095",
                            "create_date", time("2014-06-07 15:10:15.0"),
                            "total", 18990L,
                            "addresses", resourceRef("/testApp/sqldata/addresses/1")});


            order_two = resource("014-2004096", "/testApp/sqldata/" + schema_two + ".orders",
                    new Object[]{
                            "order_id", "014-2004096",
                            "create_date", time("2014-04-02 11:06:12.0"),
                            "total", 43800L,
                            "addresses", resourceRef("/testApp/sqldata/addresses/2")});

            order_three = resource("014-2004345", "/testApp/sqldata/" + schema_two + ".orders",
                    new Object[]{
                            "order_id", "014-2004345",
                            "create_date", time("2014-06-01 18:06:12.0"),
                            "total", 32500L,
                            "addresses", resourceRef("/testApp/sqldata/addresses/2")});
        }

        ResourceState expected = resource(endpoint, new Object[] {
                        "count", 2,
                        "type", "collection"
                },
                resource("1", endpoint, expandDepth == 1 ? new Object[0] : new Object[]{
                        "address_id", 1,
                        "name", "John F. Doe",
                        "street", "Liveoak street 7",
                        "postcode", null,
                        "city", "London",
                        "country_iso", "UK",
                        "is_company", false,
                        schema + ".orders", list(),
                        schema_two + ".orders", list(
                            expandDepth == 2 ? resourceRef("/testApp/sqldata/" + schema_two + ".orders/014-1003095") : order
                        )
                }),
                resource("2", endpoint, expandDepth == 1 ? new Object[0] : new Object[] {
                        "address_id", 2,
                        "name", "Lombaas Inc.",
                        "street", "Liveoak square 1",
                        "postcode", "94114",
                        "city", "San Francisco",
                        "country_iso", "US",
                        "is_company", true,
                        schema + ".orders", list(),
                        schema_two + ".orders", list(
                            expandDepth == 2 ? resourceRef("/testApp/sqldata/" + schema_two + ".orders/014-2004096") : order_two,
                            expandDepth == 2 ? resourceRef("/testApp/sqldata/" + schema_two + ".orders/014-2004345") : order_three
                        )
                }));

        checkResource(result, expected);
    }

    private Timestamp time(String dt) throws ParseException {
        return new Timestamp(iso.parse(dt).getTime());
    }

    private List list(Object... objs) {
        return new ArrayList(Arrays.asList(objs));
    }

    private void checkResource(ResourceState actual, ResourceState expected) {
        // We could simply do:
        //   assertThat(actual).isEqualTo(expected);
        //
        // But that makes it more difficult to pin down the exact point of difference.
        // Therefore we iterate ourselves ...

        assertThat(actual.id()).isEqualTo(expected.id());
        assertThat(actual.uri()).isEqualTo(expected.uri());
        assertThat(actual.getPropertyNames()).isEqualTo(expected.getPropertyNames());
        for (String key: actual.getPropertyNames()) {
            Object exval = expected.getProperty(key);
            Object val = actual.getProperty(key);
            if (exval instanceof ResourceState) {
                assertThat(val).isInstanceOf(DefaultResourceState.class);
                checkResource((ResourceState) val, (ResourceState) exval);
            } else if (exval instanceof List) {
                List exls = (List) exval;
                assertThat(val).isInstanceOf(ArrayList.class);
                checkList((List) val, exls);
            } else {
                assertThat(val).isEqualTo(exval);
            }
        }

        List<ResourceState> exmembers = expected.members();
        List<ResourceState> members = actual.members();
        assertThat(members.size()).isEqualTo(exmembers.size());

        int i = 0;
        for (ResourceState member: members) {
            checkResource(member, exmembers.get(i));
            i++;
        }
    }

    private void checkList(List actual, List expected) {
        assertThat(actual.size()).isEqualTo(expected.size());
        int i = 0;
        for (Object val: actual) {
            Object exval = expected.get(i);
            if (val instanceof ResourceState) {
                assertThat(exval).isInstanceOf(ResourceState.class);
                checkResource((ResourceState) val, (ResourceState) exval);
            } else {
                assertThat(val).isEqualTo(exval);
            }
            i++;
        }
    }

    private ResourceState resource(String endpoint, Object[] properties, ResourceState ... members) throws URISyntaxException {
        ResourcePath path = new ResourcePath(endpoint);
        return resource(path.tail().toString(), path.parent().toString(), properties, members);
    }

    private ResourceState resource(String id, String parentUri, Object[] properties, ResourceState ... members) throws URISyntaxException {
        DefaultResourceState state = new DefaultResourceState(id);
        state.uri(new URI(parentUri + "/" + id));
        assertThat(properties.length % 2).isEqualTo(0);
        int count = properties.length / 2;
        for (int i = 0; i < count; i++) {
            String key = (String) properties[2*i];
            Object val = properties[2*i + 1];
            state.putProperty(key, val);
        }
        for (ResourceState resource: members) {
            state.members().add(resource);
        }
        return state;
    }
}