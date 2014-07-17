/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.stomp;

import java.util.Set;

public interface Headers {

    Set<String> getHeaderNames();

    String get(String headerName);

    String put(String headerName, String headerValue);

    void putAll(Headers headers);

    void remove(String headerName);

    Headers duplicate();


    String CONTENT_LENGTH = "content-length";
    String CONTENT_TYPE = "content-type";
    String SESSION = "session";
    String DESTINATION = "destination";
    String ID = "id";
    String RECEIPT = "receipt";
    String RECEIPT_ID = "receipt-id";
    String ACK = "ack";
    String SELECTOR = "selector";
    String TRANSACTION = "transaction";
    String SUBSCRIPTION = "subscription";
    String MESSAGE_ID = "message-id";
    String HOST = "host";
    String ACCEPT_VERSION = "accept-version";
    String VERSION = "version";
    String SERVER = "server";
    String MESSAGE = "message";
    String HEARTBEAT = "heart-beat";
    String LOGIN = "login";
    String PASSCODE = "passcode";
}
