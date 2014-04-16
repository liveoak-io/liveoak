/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.impl.interceptor;

import io.liveoak.common.DefaultSecurityContext;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.container.interceptor.DefaultInterceptor;
import io.liveoak.spi.container.interceptor.InboundInterceptorContext;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class MockAuthInterceptor extends DefaultInterceptor {

    private String subject;

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public void onInbound(InboundInterceptorContext context) throws Exception {
        ResourceRequest req = context.request();
        RequestContext requestContext = req.requestContext();
        DefaultSecurityContext securityContext = (DefaultSecurityContext) requestContext.securityContext();
        securityContext.setRealm("foo");
        securityContext.setSubject(this.subject);
        super.onInbound(context);
    }
}
