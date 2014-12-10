package io.liveoak.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Ken Finnigan
 */
public final class JsonFilterUtils {

    private JsonFilterUtils() {
    }

    public static ResourceState filter(ResourceState src, Properties environmentProperties) {
        ResourceState dest = new DefaultResourceState(src.id());

        for (String name : src.getPropertyNames()) {
            dest.putProperty(name, filter(src.getProperty(name), environmentProperties));
        }

        src.members().forEach(member -> dest.addMember(filter(member, environmentProperties)));

        return dest;
    }

    private static Object filter(Object src, Properties environmentProperties) {
        if (src instanceof String) {
            return filter((String) src, environmentProperties);
        }
        if (src instanceof ResourceState) {
            return filter((ResourceState) src, environmentProperties);
        }
        if (src instanceof List) {
            List<Object> array = new ArrayList<Object>();
            ((List) src).forEach((e) -> array.add(filter(e, environmentProperties)));
            return array;
        }
        return src;
    }

    private static String filter(String src, Properties environmentProperites) {
        return StringPropertyReplacer.replaceProperties(src, environmentProperites);
    }
}
