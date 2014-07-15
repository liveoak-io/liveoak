package io.liveoak.pgsql;

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

        ResourceState body = resource("items", "/testApp/" + BASEPATH, new Object[]{
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

        // list existing tables as unexpanded members
        // there is now an extra table there
        // list existing tables as unexpanded members
        result = client.read(ctx("*"), endpoint);
        System.out.println(result);


        // create a new item, linking to the first order
        body = resource("", "", new Object[] {});
        result = client.create(ctx("*"), endpoint + "/items", body);

        // list order expanded, to see its items
    }
}
