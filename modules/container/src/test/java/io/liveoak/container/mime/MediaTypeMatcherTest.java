/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.mime;

import io.liveoak.container.codec.DefaultMediaTypeMatcher;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.MediaTypeMatcher;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class MediaTypeMatcherTest {

    @Test
    public void testMatcherOneType() throws Exception {
        DefaultMediaTypeMatcher matcher = new DefaultMediaTypeMatcher("application/json");

        assertThat(matcher.mediaTypes()).hasSize(1);
        assertThat(matcher.mediaTypes().get(0).type()).isEqualTo("application");
        assertThat(matcher.mediaTypes().get(0).subtype()).isEqualTo("json");
        assertThat(matcher.mediaTypes().get(0).suffix()).isNull();
    }

    @Test
    public void testMatcherMultipleTypes() throws Exception {
        DefaultMediaTypeMatcher matcher = new DefaultMediaTypeMatcher("application/json, text/xml, */*");

        assertThat(matcher.mediaTypes()).hasSize(3);
        assertThat(matcher.mediaTypes().get(0).type()).isEqualTo("application");
        assertThat(matcher.mediaTypes().get(0).subtype()).isEqualTo("json");
        assertThat(matcher.mediaTypes().get(0).suffix()).isNull();

        assertThat(matcher.mediaTypes().get(1).type()).isEqualTo("text");
        assertThat(matcher.mediaTypes().get(1).subtype()).isEqualTo("xml");
        assertThat(matcher.mediaTypes().get(1).suffix()).isNull();

        assertThat(matcher.mediaTypes().get(2).type()).isEqualTo("*");
        assertThat(matcher.mediaTypes().get(2).subtype()).isEqualTo("*");
        assertThat(matcher.mediaTypes().get(2).suffix()).isNull();
    }

    @Test
    public void testMatcherMultipleTypesWithQuality() throws Exception {
        DefaultMediaTypeMatcher matcher = new DefaultMediaTypeMatcher("application/json; q=0.5, text/xml; q=0.8, */*; q=0.2");

        assertThat(matcher.mediaTypes()).hasSize(3);

        assertThat(matcher.mediaTypes().get(0).type()).isEqualTo("text");
        assertThat(matcher.mediaTypes().get(0).subtype()).isEqualTo("xml");
        assertThat(matcher.mediaTypes().get(0).suffix()).isNull();

        assertThat(matcher.mediaTypes().get(1).type()).isEqualTo("application");
        assertThat(matcher.mediaTypes().get(1).subtype()).isEqualTo("json");
        assertThat(matcher.mediaTypes().get(1).suffix()).isNull();

        assertThat(matcher.mediaTypes().get(2).type()).isEqualTo("*");
        assertThat(matcher.mediaTypes().get(2).subtype()).isEqualTo("*");
        assertThat(matcher.mediaTypes().get(2).suffix()).isNull();
    }

    @Test
    public void testFindBestMatch() throws Exception {
        MediaTypeMatcher matcher = new DefaultMediaTypeMatcher("application/json; q=0.5, text/xml; q=0.8, */*; q=0.2");

        List<MediaType> candidates = new ArrayList<>();

        candidates.add(new MediaType("application/json"));
        candidates.add(new MediaType("text/xml"));
        candidates.add(new MediaType("text/html"));

        MediaType match = matcher.findBestMatch(candidates);

        assertThat(match).isNotNull();
        assertThat(match.type()).isEqualTo("text");
        assertThat(match.subtype()).isEqualTo("xml");
    }

    @Test
    public void testFindBestMatchWithSuffixes() throws Exception {
        MediaTypeMatcher matcher = new DefaultMediaTypeMatcher("application/json; q=0.5, text/foo+xml; q=0.8, */*; q=0.2");

        List<MediaType> candidates = new ArrayList<>();

        candidates.add(new MediaType("application/json"));
        candidates.add(new MediaType("text/xml"));
        candidates.add(new MediaType("text/html"));

        MediaType match = matcher.findBestMatch(candidates);

        assertThat(match).isNotNull();
        assertThat(match.type()).isEqualTo("text");
        assertThat(match.subtype()).isEqualTo("xml");
    }
}
