package org.projectodd.restafari.container.auth.spi;

import org.projectodd.restafari.container.ResourceRequest;

/**
 * Temporary class to resolve applicationId from ResourceRequest until it's clear how to obtain applicationId from request. TODO: Probably remove...
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@FunctionalInterface
public interface ApplicationIdResolver {

    String resolveAppId(ResourceRequest req);
}
