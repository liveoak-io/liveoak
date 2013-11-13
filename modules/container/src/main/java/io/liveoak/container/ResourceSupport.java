package io.liveoak.container;

import io.liveoak.spi.resource.async.Resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class ResourceSupport {

    static public URI uriFor(Resource resource) {
        List<String> segments = new ArrayList<>();
        Resource current = resource;

        while (current != null) {
            segments.add(0, current.id());
            current = resource.parent();
        }

        StringBuilder buf = new StringBuilder();

        segments.forEach((s) -> {
            buf.append( "/" );
            buf.append( s );
        });

        return URI.create( buf.toString() );
    }
}
