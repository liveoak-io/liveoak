package org.projectodd.restafari.container.mime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class MediaTypeMatcher {

    public MediaTypeMatcher(String mediaTypes) {

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

                if ( leftQ > rightQ ) {
                    return -1;
                }

                if ( leftQ < rightQ ) {
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

    public MediaType findBestMatch(Collection<MediaType> types) {
        for ( MediaType mine : this.mediaTypes ) {
            for ( MediaType other : types ) {
                if ( other.isCompatible( mine ) ) {
                    return other;
                }
            }
        }

        return null;
    }

    private List<MediaType> mediaTypes = new ArrayList<>();
}
