package org.projectodd.restafari.container.codec.json;

import io.netty.buffer.ByteBuf;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.projectodd.restafari.container.SimpleObjectResource;
import org.projectodd.restafari.spi.ObjectResource;
import org.projectodd.restafari.spi.Resource;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * @author Bob McWhirter
 */
public class JSONCodecTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testCodec() throws Exception {

        JSONCodec codec = new JSONCodec();

        ObjectResource resource = new SimpleObjectResource();

        resource.setString("name", "bob" );

        ByteBuf result = codec.encode(resource);

        String str = result.toString(Charset.forName("utf-8"));

        assertEquals( "{\"name\":\"bob\"}", str );

        ObjectResource o = (ObjectResource) codec.decode(result);

        assertEquals( o.getString( "name" ), "bob" );


    }

    @Test
    @SuppressWarnings("unchecked")
    public void testArray() throws Exception {
        JSONCodec codec = new JSONCodec();
        ObjectResource resource1 = new SimpleObjectResource();
        resource1.setString("name", "bob");
        ObjectResource resource2 = new SimpleObjectResource();
        resource2.setString("foo", "bar");

        Collection<Resource> expectedResources = new ArrayList<>(2);
        expectedResources.add(resource1);
        expectedResources.add(resource2);

        ByteBuf result = codec.encode(expectedResources);
        String str = result.toString(Charset.forName("utf-8"));
        assertEquals("[{\"name\":\"bob\"},{\"foo\":\"bar\"}]", str);

        Collection<Resource> actualResources = (Collection<Resource>) codec.decode(result);
        assertEquals(expectedResources.size(), actualResources.size());
        Iterator<Resource> itr = actualResources.iterator();
        for (Resource resource : expectedResources) {
            ObjectResource expected = (ObjectResource) resource;
            ObjectResource actual = (ObjectResource) itr.next();
            assertEquals(expected.getPropertyNames(), actual.getPropertyNames());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEmptyArray() throws Exception {
        JSONCodec codec = new JSONCodec();
        ByteBuf result = codec.encode(Collections.emptyList());
        String str = result.toString(Charset.forName("utf-8"));
        assertEquals("[]", str);
        Collection<Resource> resources = (Collection<Resource>) codec.decode(result);
        assertNotNull(resources);
        assertTrue(resources.isEmpty());
    }

}
