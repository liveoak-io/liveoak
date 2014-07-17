package io.liveoak.stomp.client;

/**
 * @author Bob McWhirter
 */
public interface ReceiptReceiver {
    String receiptId();

    void receivedReceipt();
}
