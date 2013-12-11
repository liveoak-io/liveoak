/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.codec;

import io.liveoak.spi.MediaType;
import io.liveoak.spi.MediaTypeMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class DefaultMediaTypeMatcher implements MediaTypeMatcher {

    public DefaultMediaTypeMatcher(String mediaTypes, String extension) {
        this(mediaTypes);
        if (extension != null) {
            MediaType extensionMediaType = MediaType.lookup(extension);
            if (extensionMediaType != null) {
                this.mediaTypes.add(0, extensionMediaType);
            }
        }
    }

    public DefaultMediaTypeMatcher(String mediaTypes) {

        int len = mediaTypes.length();
        int end = mediaTypes.indexOf(",");
        if (end < 0) {
            end = len;
        }
        int cur = 0;

        while (cur <= len) {
            MediaType mediaType = new MediaType(mediaTypes.substring(cur, end).trim());
            this.mediaTypes.add(mediaType);
            cur = end + 1;
            end = mediaTypes.indexOf(",", cur);
            if (end < 0) {
                end = len;
            }
        }

        this.mediaTypes.sort((left, right) -> {
            String leftQstr = left.parameters().get("q");
            String rightQstr = right.parameters().get("q");

            if (leftQstr == null && rightQstr != null) {
                return -1;
            }

            if (leftQstr != null && rightQstr == null) {
                return 1;
            }

            if (leftQstr == null && rightQstr == null) {
                return 0;
            }

            try {
                double leftQ = Double.parseDouble(leftQstr);
                double rightQ = Double.parseDouble(rightQstr);

                if (leftQ == rightQ) {
                    return 0;
                }

                if (leftQ > rightQ) {
                    return -1;
                }

                if (leftQ < rightQ) {
                    return 1;
                }
            } catch (NumberFormatException e) {
                return 0;
            }

            return 0;
        });
    }

    public List<MediaType> mediaTypes() {
        return this.mediaTypes;
    }

    @Override
    public MediaType findBestMatch(List<MediaType> types) {
        for (MediaType mine : this.mediaTypes) {
            for (MediaType other : types) {
                if (other.isCompatible(mine)) {
                    return other;
                }
            }
        }

        return null;
    }

    private List<MediaType> mediaTypes = new ArrayList<>();
}
