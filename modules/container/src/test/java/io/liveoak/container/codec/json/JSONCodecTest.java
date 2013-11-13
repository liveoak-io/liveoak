package io.liveoak.container.codec.json;

import org.junit.After;
import org.junit.Before;

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

    /*
    @Test
    public void testCodec() throws Exception {

        JSONEncoder codec = new JSONEncoder();

        ObjectResourceState object = new SimpleObjectResourceState();

        object.setString("name", "bob" );

        ByteBuf result = codec.encode(object);

        String str = result.toString(Charset.forName("utf-8"));

        assertEquals( "{\"name\":\"bob\"}", str );

        ObjectResourceState o = (ObjectResourceState) codec.decode(result);

        assertEquals( o.getString( "name" ), "bob" );


    }

    @Test
    @SuppressWarnings("unchecked")
    public void testArray() throws Exception {
        JSONEncoder codec = new JSONEncoder();
        ObjectResourceState resource1 = new SimpleObjectResourceState();
        resource1.setString("name", "bob");
        ObjectResourceState resource2 = new SimpleObjectResourceState();
        resource2.setString("foo", "bar");

        Collection<ResourceState> expectedResourceStates = new ArrayList<>(2);
        expectedResourceStates.add(resource1);
        expectedResourceStates.add(resource2);

        ByteBuf result = codec.encode(expectedResourceStates);
        String str = result.toString(Charset.forName("utf-8"));
        assertEquals("[{\"name\":\"bob\"},{\"foo\":\"bar\"}]", str);

        Collection<ResourceState> actualResourceStates = (Collection<ResourceState>) codec.decode(result);
        assertEquals(expectedResourceStates.size(), actualResourceStates.size());
        Iterator<ResourceState> itr = actualResourceStates.iterator();
        for (ResourceState resourceState : expectedResourceStates) {
            ObjectResourceState expected = (ObjectResourceState) resourceState;
            ObjectResourceState actual = (ObjectResourceState) itr.next();
            assertEquals(expected.getPropertyNames(), actual.getPropertyNames());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEmptyArray() throws Exception {
        JSONEncoder codec = new JSONEncoder();
        ByteBuf result = codec.encode(Collections.emptyList());
        String str = result.toString(Charset.forName("utf-8"));
        assertEquals("[]", str);
        Collection<ResourceState> resourceStates = (Collection<ResourceState>) codec.decode(result);
        assertNotNull(resourceStates);
        assertTrue(resourceStates.isEmpty());
    }
    */

}
