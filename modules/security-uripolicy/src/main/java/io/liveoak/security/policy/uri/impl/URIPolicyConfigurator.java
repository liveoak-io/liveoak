package io.liveoak.security.policy.uri.impl;

import java.util.Collection;

import io.liveoak.common.util.ObjectsTree;
import io.liveoak.security.policy.uri.integration.URIPolicyConfig;
import io.liveoak.security.policy.uri.integration.URIPolicyConfigRule;
import io.liveoak.spi.ResourcePath;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIPolicyConfigurator {

    private static final Logger log = Logger.getLogger(URIPolicyConfigurator.class);
    private ObjectsTree<URIPolicyRule> rulesTree;

    public void configure(URIPolicy policy, URIPolicyConfig uriPolicyConfig) {
        this.rulesTree = new ObjectsTree<>();

        for (URIPolicyConfigRule cfgRule : uriPolicyConfig.getRules()) {
            addURIPolicyRule(new ResourcePath(cfgRule.getUriPattern()), cfgRule.getRequestTypes(),
                    cfgRule.getAllowedRoles(), cfgRule.getDeniedRoles(),
                    cfgRule.getAllowedUsers(), cfgRule.getDeniedUsers());
        }

        policy.setRulesTree(this.rulesTree);
        log.info("URIPolicy configuration updated successfully");
    }

    protected void addURIPolicyRule(ResourcePath resourcePath, Collection<String> requestTypes, Collection<String> allowedRoles, Collection<String> deniedRoles,
                                 Collection<String> allowedUsers, Collection<String> deniedUsers) {
        RolesContainer rolesContainer = new RolesContainer()
                .addAllAllowedRoles(allowedRoles).addAllDeniedRoles(deniedRoles)
                .addAllAllowedUsers(allowedUsers).addAllDeniedUsers(deniedUsers);
        URIPolicyRule rule = new URIPolicyRule(resourcePath, requestTypes, rolesContainer);

        // We want to support '/foo/bar*' which is mapped to both '/foo/bar' and '/foo/bar/*'
        String tailName = resourcePath.tail().name();
        if (tailName.endsWith("*") && !tailName.equals("*")) {
            String lastSegment = tailName.substring(0, tailName.length() - 1);
            resourcePath = resourcePath.parent();
            resourcePath.appendSegment(lastSegment);

            ResourcePath wildcardPath = new ResourcePath(resourcePath);
            wildcardPath.appendSegment("*");

            this.rulesTree.addObject(rule, resourcePath);
            this.rulesTree.addObject(rule, wildcardPath);
        } else {
            this.rulesTree.addObject(rule, resourcePath);
        }

        log.debug("Added new URIPolicyRule for resourcePath " + resourcePath + ". Rule: " + rule);
    }
}
