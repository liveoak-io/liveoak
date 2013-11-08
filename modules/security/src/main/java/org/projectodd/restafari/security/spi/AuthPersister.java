package org.projectodd.restafari.security.spi;

import java.util.List;

/**
 * Component responsible for save/load of all policies and security related data
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface AuthPersister {

    /**
     * Save/override applicationMetadata
     *
     * @param appMetadata to save
     */
    void registerApplicationMetadata(ApplicationMetadata appMetadata);

    /**
     * Obtain applicationMetadata for given application
     *
     * @param applicationKey key/ID of application
     * @return loaded applicationMetadata or null if metadata not found for given key
     */
    ApplicationMetadata getApplicationMetadata(String applicationKey);

    /**
     * Will either add new policy or edit existing policy for given application
     *
     * @param applicationKey key/ID of application
     * @param policy policy to add or update
     */
    void registerPolicy(String applicationKey, AuthorizationPolicyEntry policy);

    /**
     * Return list of registered Authorization policies for given application
     *
     * @param applicationKey key/ID of application
     * @return all Authorization policies or empty list if no policies are registered for this application
     */
    List<AuthorizationPolicyEntry> getRegisteredPolicies(String applicationKey);
}
