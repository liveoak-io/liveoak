/*
 * Copyright 2011 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.projectodd.restafari.stomp;

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
