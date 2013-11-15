/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.mime;

import org.fest.assertions.Condition;
import org.junit.Test;
import io.liveoak.spi.MediaType;

import static org.fest.assertions.Assertions.*;

/**
 * @author Bob McWhirter
 */
public class MediaTypeTest {

    @Test
    public void testCompatibility() throws Exception {
        assertThat(new MediaType("application", "json")).is(compatibleWith(new MediaType("application", "json")));
        assertThat(new MediaType("application", "json")).is(compatibleWith(new MediaType("application", "foo", "json")));
        assertThat(new MediaType("application", "foo", "json")).is(compatibleWith(new MediaType("application", "foo", "json")));
        assertThat(new MediaType("application", "foo", "json")).is(compatibleWith(new MediaType("application", "json")));
    }

    @Test
    public void testCompatibilityWithStars() throws Exception {
        assertThat(new MediaType("application", "json")).is(compatibleWith(new MediaType("*", "*")));
        assertThat(new MediaType("application", "json")).is(compatibleWith(new MediaType("application", "*")));
        assertThat(new MediaType("application", "*")).is(compatibleWith(new MediaType("application", "*")));
        assertThat(new MediaType("application", "json")).is(compatibleWith(new MediaType("*", "json")));
    }

    @Test
    public void testParseWithoutParameters() throws Exception {
        MediaType test = new MediaType("application/json");
        assertThat(test.type()).isEqualTo("application");
        assertThat(test.subtype()).isEqualTo("json");
        assertThat(test.suffix()).isNull();
        assertThat( test.parameters() ).isEmpty();

        test = new MediaType("application/foo+json");
        assertThat(test.type()).isEqualTo("application");
        assertThat(test.subtype()).isEqualTo("foo");
        assertThat(test.suffix()).isEqualTo("json");
        assertThat( test.parameters() ).isEmpty();
    }

    @Test
    public void testParseWithParameters() throws Exception {
        MediaType test = new MediaType("application/json; q=0.8");
        assertThat(test.type()).isEqualTo("application");
        assertThat(test.subtype()).isEqualTo("json");
        assertThat(test.suffix()).isNull();
        assertThat( test.parameters() ).hasSize(1);
        assertThat( test.parameters().get( "q") ).isEqualTo( "0.8" );

        test = new MediaType("application/foo+json; q=0.8");
        assertThat(test.type()).isEqualTo("application");
        assertThat(test.subtype()).isEqualTo("foo");
        assertThat(test.suffix()).isEqualTo("json");
        assertThat( test.parameters() ).hasSize(1);
        assertThat( test.parameters().get("q") ).isEqualTo( "0.8" );
    }

    @Test
    public void testParseWithMultipleParameters() throws Exception {
        MediaType test = new MediaType("application/json; q=0.8 ; bob=tall");
        assertThat(test.type()).isEqualTo("application");
        assertThat(test.subtype()).isEqualTo("json");
        assertThat(test.suffix()).isNull();
        assertThat( test.parameters() ).hasSize(2);
        assertThat( test.parameters().get("q") ).isEqualTo("0.8");
        assertThat( test.parameters().get( "bob") ).isEqualTo( "tall" );

        test = new MediaType("application/foo+json; q=0.8 ; bob=tall");
        assertThat(test.type()).isEqualTo("application");
        assertThat(test.subtype()).isEqualTo("foo");
        assertThat(test.suffix()).isEqualTo("json");
        assertThat( test.parameters().get("q") ).isEqualTo( "0.8" );
        assertThat( test.parameters().get("bob") ).isEqualTo( "tall" );
    }


    public static Condition<Object> compatibleWith(MediaType other) {
        return new Condition<Object>() {
            @Override
            public boolean matches(Object value) {
                if (value instanceof MediaType) {
                    return other.isCompatible((MediaType) value);
                }
                return false;
            }
        };

    }
}
