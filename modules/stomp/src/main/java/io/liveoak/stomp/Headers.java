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


    public static final String CONTENT_LENGTH = "content-length";
    public static final String CONTENT_TYPE = "content-type";
    public static final String SESSION = "session";
    public static final String DESTINATION = "destination";
    public static final String ID = "id";
    public static final String RECEIPT = "receipt";
    public static final String RECEIPT_ID = "receipt-id";
    public static final String ACK = "ack";
    public static final String SELECTOR = "selector";
    public static final String TRANSACTION = "transaction";
    public static final String SUBSCRIPTION = "subscription";
    public static final String MESSAGE_ID = "message-id";
    public static final String HOST = "host";
    public static final String ACCEPT_VERSION = "accept-version";
    public static final String VERSION = "version";
    public static final String SERVER = "server";
    public static final String MESSAGE = "message";
    public static final String HEARTBEAT = "heart-beat";
}
