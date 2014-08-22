package io.liveoak.spi;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public interface MediaTypeMatcher {

    static MediaTypeMatcher singleton(MediaType mediaType) {
        return new MediaTypeMatcher() {
            @Override
            public List<MediaType> mediaTypes() {
                List<MediaType> types = new ArrayList<>();
                types.add(mediaType);
                return types;
            }

            public String toString() {
                return mediaType.toString();
            }
        };
    }

    List<MediaType> mediaTypes();

    default MediaType findBestMatch(List<MediaType> types) {
        if (MediaType.ANY.equals(mediaTypes().get(0))) {
            // If we're looking for */*, then return application/json if it's an option
            if (types.contains(MediaType.JSON)) {
                return MediaType.JSON;
            }
        }

        MediaType compatible = null;
        for (MediaType mine : mediaTypes()) {
            for (MediaType other : types) {
                if (other.equals(mine)) {
                    return other;
                } else if (compatible == null && other.isCompatible(mine)) {
                    compatible = other;
                }
            }

            if (compatible != null) {
                return compatible;
            }
        }

        return null;
    }
}
