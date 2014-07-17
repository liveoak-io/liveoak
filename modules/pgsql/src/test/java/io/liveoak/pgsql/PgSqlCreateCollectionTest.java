package io.liveoak.pgsql;

import java.net.URI;
import java.util.Comparator;

import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PgSqlCreateCollectionTest extends BasePgSqlTest {

    @Test
    public void testCreateCollection() throws Exception {

        String endpoint = "/testApp/" + BASEPATH;

        // list existing tables as unexpanded members
        ResourceState result = client.read(ctx("*"), endpoint);
        System.out.println(result);

        ResourceState expected = resource(BASEPATH, "/testApp", new Object[] {
                "count", 3,
                "type", "database"},

                sorted((o1, o2) -> {
                           return o1.id().compareTo(o2.id());
                       },

                       resource("addresses", endpoint, new Object[]{}),
                       resource(schema + ".orders", endpoint, new Object[]{}),
                       resource(schema_two + ".orders", endpoint, new Object[]{}))
        );

        checkResource(result, expected);


        ResourceState body = resource("items", endpoint, new Object[]{
                "columns", list(
                    obj("name", "item_id",
                            "type", "varchar",
                            "size", 40),
                    obj("name", "name",
                            "type", "varchar",
                            "size", 255,
                            "modifiers", list("NOT NULL")),
                    obj("name", "quantity",
                            "type", "int4",
                            "modifiers", list("NOT NULL")),
                    obj("name", "price",
                            "type", "int4",
                            "modifiers", list("NOT NULL")),
                    obj("name", "vat",
                            "type", "int4",
                            "modifiers", list("NOT NULL")),
                    obj("name", "order_id",
                            "type", "varchar",
                            "size", 40,
                            "modifiers", list("NOT NULL"))

                ),

                "primary-key", list("item_id"),

                "foreign-keys", list(
                    obj("table", schema_two + ".orders",
                            "columns", list("order_id"))
                )
        });

        result = client.create(ctx("*"), endpoint, body);
        System.out.println(result);

        // TODO: items;schema in response is invalid - it should be items/;schema
        ResourceState schemaBody = resource("items;schema", endpoint, new Object[] {
                "columns", list(
                        obj("name", "item_id",
                                "type", "varchar",
                                "size", 40,
                                "modifiers", list("NOT NULL", "UNIQUE")),
                        obj("name", "name",
                                "type", "varchar",
                                "size", 255,
                                "modifiers", list("NOT NULL")),
                        obj("name", "quantity",
                                "type", "int4",
                                "size", 10,
                                "modifiers", list("NOT NULL")),
                        obj("name", "price",
                                "type", "int4",
                                "size", 10,
                                "modifiers", list("NOT NULL")),
                        obj("name", "vat",
                                "type", "int4",
                                "size", 10,
                                "modifiers", list("NOT NULL")),
                        obj("name", "order_id",
                                "type", "varchar",
                                "size", 40,
                                "modifiers", list("NOT NULL"))

                ),

                "primary-key", list("item_id"),

                "foreign-keys", list(
                        obj("table", schema_two + ".orders",
                        "columns", list("order_id"))
                )}
        );
        expected = schemaBody;
        checkResource(result, expected);


        // list existing tables as unexpanded members
        // there is now an extra table there
        // list existing tables as unexpanded members
        result = client.read(ctx("*"), endpoint);
        System.out.println(result);

        expected = resource(BASEPATH, "/testApp", new Object[] {
                        "count", 4,
                        "type", "database"},

                sorted((o1, o2) -> {
                            return o1.id().compareTo(o2.id());
                        },
                        resource("addresses", endpoint, new Object[]{}),
                        resource("items", endpoint, new Object[]{}),
                        resource(schema + ".orders", endpoint, new Object[]{}),
                        resource(schema_two + ".orders", endpoint, new Object[]{}))
        );
        checkResource(result, expected);


        // read schema:
        String schemaEndpoint = endpoint + "/items;schema";
        ResourcePath path = new ResourcePath(schemaEndpoint);
        result = client.read(ctx("*", path), endpoint + "/items;schema");
        System.out.println(result);

        expected = schemaBody;
        checkResource(result, expected);


        // create a new item, linking to the first order
        endpoint = endpoint + "/items";
        body = resource("I39845355", endpoint, new Object[] {
                "name", "The Gadget",
                "quantity", 1,
                "price", 39900,
                "vat", 20,
                schema_two + ".orders", resource("014-2004096", "/testApp/" + BASEPATH + "/" + schema_two + ".orders", new Object[] {})
        });
        result = client.create(ctx("*(*)"), endpoint, body);
        System.out.println(result);

        expected = resource("I39845355", endpoint, new Object[] {
                "item_id", "I39845355",
                "name", "The Gadget",
                "quantity", 1,
                "price", 39900,
                "vat", 20,
                schema_two + ".orders", resource("014-2004096", "/testApp/" + BASEPATH + "/" + schema_two + ".orders", new Object[]{
                        "order_id", "014-2004096",
                        "create_date", time("2014-04-02 11:06:12.0"),
                        "total", 43800L,
                        "addresses", resourceRef("/testApp/" + BASEPATH + "/addresses/2"),
                        "items", list(resourceRef("/testApp/" + BASEPATH + "/items/I39845355"))
                })
        });

        checkResource(result, expected);
    }
}
