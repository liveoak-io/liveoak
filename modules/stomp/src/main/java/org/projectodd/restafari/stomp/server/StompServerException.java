package org.projectodd.restafari.stomp.server;

import org.projectodd.restafari.stomp.StompException;

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
