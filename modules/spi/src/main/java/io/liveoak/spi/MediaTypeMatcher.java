package io.liveoak.spi;

import java.util.List;

/**
 * @author Bob McWhirter
 */
public interface MediaTypeMatcher {

    static MediaTypeMatcher singleton(MediaType mediaType) {
        return new MediaTypeMatcher() {
            @Override
            public MediaType findBestMatch(List<MediaType> types) {
                for ( MediaType other : types ) {
                    if ( other.isCompatible( mediaType ) ) {
                        return other;
                    }
                }
                return null;
            }

            public String toString() {
                return mediaType.toString();
            }
        };
    }

    MediaType findBestMatch(List<MediaType> types);
}
