/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.mime;

import java.util.ArrayList;
import java.util.List;

import io.liveoak.common.DefaultMediaTypeMatcher;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.MediaTypeMatcher;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
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

        candidates.add(MediaType.JSON);
        candidates.add(MediaType.XML);
        candidates.add(MediaType.HTML);

        MediaType match = matcher.findBestMatch(candidates);

        assertThat(match).isNotNull();
        assertThat(match.type()).isEqualTo("text");
        assertThat(match.subtype()).isEqualTo("xml");
    }

    @Test
    public void testFindBestMatchWithSuffixes() throws Exception {
        MediaTypeMatcher matcher = new DefaultMediaTypeMatcher("application/json; q=0.5, text/foo+xml; q=0.8, */*; q=0.2");

        List<MediaType> candidates = new ArrayList<>();

        candidates.add(MediaType.JSON);
        candidates.add(MediaType.XML);
        candidates.add(MediaType.HTML);

        MediaType match = matcher.findBestMatch(candidates);

        assertThat(match).isNotNull();
        assertThat(match.type()).isEqualTo("text");
        assertThat(match.subtype()).isEqualTo("xml");
    }

    @Test
    public void testFindBestMatchWithCustomTypeNotRequested() throws Exception {
        MediaTypeMatcher matcher = new DefaultMediaTypeMatcher("application/json; q=0.81, text/xml; q=0.8, */*; q=0.2");

        List<MediaType> candidates = new ArrayList<>();

        candidates.add(MediaType.LOCAL_APP_JSON);
        candidates.add(MediaType.XML);
        candidates.add(MediaType.JSON);
        candidates.add(MediaType.HTML);

        MediaType match = matcher.findBestMatch(candidates);

        assertThat(match).isNotNull();
        assertThat(match.type()).isEqualTo("application");
        assertThat(match.subtype()).isEqualTo("json");
    }

    @Test
    public void testFindBestMatchWithCustomTypeRequested() throws Exception {
        MediaTypeMatcher matcher = new DefaultMediaTypeMatcher("application/vnd.liveoak.local-app+json; q=0.8, */*; q=0.2");

        List<MediaType> candidates = new ArrayList<>();

        candidates.add(MediaType.JSON);
        candidates.add(MediaType.XML);
        candidates.add(MediaType.HTML);
        candidates.add(MediaType.LOCAL_APP_JSON);

        MediaType match = matcher.findBestMatch(candidates);

        assertThat(match).isNotNull();
        assertThat(match.type()).isEqualTo("application");
        assertThat(match.subtype()).isEqualTo("vnd.liveoak.local-app");
    }

    @Test
    public void testFindBestMatchWhenAcceptAny() throws Exception {
        DefaultMediaTypeMatcher matcher = new DefaultMediaTypeMatcher("text/html; q=0.4, */*; q=0.9");

        List<MediaType> candidates = new ArrayList<>();

        candidates.add(MediaType.LOCAL_APP_JSON);
        candidates.add(MediaType.XML);
        candidates.add(MediaType.JSON);
        candidates.add(MediaType.HTML);

        MediaType match = matcher.findBestMatch(candidates);

        assertThat(match).isNotNull();
        assertThat(match.type()).isEqualTo("application");
        assertThat(match.subtype()).isEqualTo("json");
    }
}
