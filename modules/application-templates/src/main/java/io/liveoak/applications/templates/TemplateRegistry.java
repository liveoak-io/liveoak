package io.liveoak.applications.templates;


import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by mwringe on 10/02/15.
 */
public class TemplateRegistry {

    private Map<String, ApplicationTemplateResource> templates = new HashMap<>();

    public void addTemplateResource(ApplicationTemplateResource templateResource) {
        this.templates.put(templateResource.id(), templateResource);
    }

    public ApplicationTemplateResource getTemplateResource(String id) {
        return templates.get(id);
    }

    public ObjectNode getTemplate(String id) {
        ApplicationTemplateResource resource = templates.get(id);
        if (resource != null) {
            return resource.getConfig();
        } else {
            return null;
        }
    }

    public Set<String> getTemplateNames() {
        return templates.keySet();
    }

}
