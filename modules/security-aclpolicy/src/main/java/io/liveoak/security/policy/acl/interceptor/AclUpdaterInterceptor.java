/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.policy.acl.interceptor;

import io.liveoak.common.DefaultRequestAttributes;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.security.policy.acl.AclPolicyConstants;
import io.liveoak.spi.RequestAttributes;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.client.ClientResourceResponse;
import io.liveoak.spi.container.interceptor.DefaultInterceptor;
import io.liveoak.spi.container.interceptor.OutboundInterceptorContext;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AclUpdaterInterceptor extends DefaultInterceptor {

    private static final Logger log = Logger.getLogger(AclUpdaterInterceptor.class);

    private final Client client;

    public AclUpdaterInterceptor(Client client) {
        this.client = client;
    }

    private String getPrefix(ResourcePath path) {
        if (path.segments().size() < 1) {
            return null;
        }
        String prefix = "/" + path.head().name();
        return prefix;
    }

    @Override
    public void onOutbound(OutboundInterceptorContext context) throws Exception {
        ResourceResponse response = context.response();

        if (response.responseType() == ResourceResponse.ResponseType.CREATED || response.responseType() == ResourceResponse.ResponseType.DELETED) {
            String prefix = getPrefix(context.request().resourcePath());
            RequestAttributes attribs = new DefaultRequestAttributes();
            attribs.setAttribute(AclPolicyConstants.ATTR_CREATED_RESOURCE_RESPONSE, context.response());
            RequestContext aclUpdateRequest = new RequestContext.Builder().requestAttributes(attribs).build();

            // TODO: For now it's hardcoded to 'acl-policy' . We should be able to handle the situation when resourceId is different
            client.update(aclUpdateRequest, prefix + "/acl-policy/" + AclPolicyConstants.RESOURCE_LISTENER_RESOURCE_ID, new DefaultResourceState(), (updateResponse) -> {
                if (updateResponse.responseType() == ClientResourceResponse.ResponseType.NO_SUCH_RESOURCE) {
                    log.info("No acl-policy resource available. Listener ignored");
                } else {
                    log.debug("ACL Rules updated: " + updateResponse.state());
                }

                context.forward();
            });

        } else {
            context.forward();
        }
    }

}
