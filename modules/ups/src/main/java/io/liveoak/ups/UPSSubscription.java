package io.liveoak.ups;

import io.liveoak.spi.ResourcePath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class UPSSubscription {

    private ResourcePath resourcePath;
    private List<String> aliases = new ArrayList<String>();
    private Set<String> categories = new HashSet<>();
    private Integer simplePush = null;
    private List<String> deviceTypes = new ArrayList<String>();
    private List<String> variants = new ArrayList<String>();

    private Map<String, Object> message = new HashMap<String, Object>();

    public UPSSubscription( String resourcePath ) {
        this.resourcePath = new ResourcePath(resourcePath);
    }

    public ResourcePath resourcePath() {
        return this.resourcePath;
    }

    public void aliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public List<String> aliases() {
        return aliases;
    }

    public void categories(Set<String> categories) {
        this.categories = categories;
    }

    public Set<String> categories() {
        return categories;
    }

    public void simplePush(Integer simplePush) {
        this.simplePush = simplePush();
    }

    public Integer simplePush() {
        return simplePush;
    }

    public void deviceTypes(List<String> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }

    public List<String> deviceTypes() {
        return deviceTypes;
    }

    public void variants(List<String> variants) {
        this.variants = variants;
    }

    public List<String> variants() {
        return variants;
    }

    public void setMessage(Map<String, Object> message) {
        this.message = message;
    }

    public Map<String, Object> message() {
        return new HashMap(message);
    }
}
