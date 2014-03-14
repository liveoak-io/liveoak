/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.stomp.server;

import io.liveoak.stomp.Stomp;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class StompServerSecurityException extends StompServerException {

    public StompServerSecurityException(String message) {
        super(message);
    }

    public StompServerSecurityException(Throwable cause) {
        super(cause);
    }

    public StompServerSecurityException withCommand(Stomp.Command command) {
        this.command = command;
        return this;
    }

    public StompServerSecurityException withStatus(int status) {
        this.status = status;
        return this;
    }

    public Stomp.Command getCommand() {
        return command;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public StompServerSecurityException withReceiptId(String receiptId) {
        super.withReceiptId(receiptId);
        return this;
    }

    private Stomp.Command command;
    private int status;
}
