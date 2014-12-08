/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import io.liveoak.common.DefaultReturnFields;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class MongoDBAggregateCollectionReadTest extends BaseMongoDBTest {

    @Test
    public void testGetStorageCollectionsQuery() throws Exception {
        DBCollection collection = db.getCollection("testQueryCollection");
        if (collection != null) {
            collection.drop();
        }
        collection = db.createCollection("testQueryCollection", new BasicDBObject("count", 0));

        // insert data records for the test
        setupPeopleData(collection);
        assertThat(collection.count()).isEqualTo(6);

        // This should return 3 items
        //
        SimpleResourceParams resourceParams = new SimpleResourceParams();
        resourceParams.put("q", "[{$group:{_id:{country:'$country'},numPeople:{$sum:1}}}]");
        RequestContext requestContext = new RequestContext.Builder()
                .returnFields(new DefaultReturnFields("*(*)"))
                .resourceParams(resourceParams)
                .build();


        ResourceState result = client.read(requestContext, "/testApp/" + BASEPATH + "/testQueryCollection/_aggregate");

        // verify response
        assertThat(result).isNotNull();

        assertThat(result.id()).isEqualTo("_aggregate");
        assertThat(result.getProperty("result")).isNotNull();
        assertThat(result.getProperty("result")).isInstanceOf(List.class);

        List results = (List) result.getProperty("result");
        assertThat(results.size()).isEqualTo(3);

        Set countries = checkAggregateItems(results);
        Set expected = new HashSet(Arrays.asList(new String[] { "DE", "FR", "US" }));
        assertThat(countries).isEqualTo(expected);
    }

    private Set checkAggregateItems(List results) {
        HashSet<String> keys = new HashSet<>();
        for (Object item : results) {
            assertThat(item).isInstanceOf(ResourceState.class);
            keys.add(checkAggregateItem((ResourceState) item));
        }
        return keys;
    }

    private String checkAggregateItem(ResourceState item) {
        assertThat(item).isInstanceOf(ResourceState.class);
        Object val = item.getProperty("_id");
        assertThat(val).isNotNull();
        assertThat(val).isInstanceOf(ResourceState.class);

        ResourceState id = (ResourceState) val;
        val = id.getProperty("country");
        assertThat(val).isNotNull();
        assertThat(val).isInstanceOf(String.class);

        val = item.getProperty("numPeople");
        assertThat(val).isNotNull();
        assertThat(val).isEqualTo(2);

        return (String) id.getProperty("country");
    }
}
