/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp.server;

import io.liveoak.stomp.StompException;

/**
 * @author Bob McWhirter
 */
public class StompServerException extends StompException {

    public StompServerException(String message) {
        super( message );
    }

    public StompServerException(Throwable cause) {
        super( cause );
    }

    public StompServerException withReceiptId(String receiptId) {
        this.receiptId = receiptId;
        return this;
    }

    public String getReceiptId() {
        return this.receiptId;
    }

    private String receiptId;

}
