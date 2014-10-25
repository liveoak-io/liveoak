/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.analytics;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class AnalyticsEvent {

    private String userId;
    private String application;
    private String clientAddress;
    private String uri;
    private String method;
    private long requestBytes;
    private long responseBytes;
    private int status;
    private long timestamp;
    private long duration;
    private boolean apiRequest;
    private String notification;

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public void setRequestBytes(long bytes) {
        this.requestBytes = bytes;
    }

    public long getRequestBytes() {
        return requestBytes;
    }

    public void setResponseBytes(long bytes) {
        this.responseBytes = bytes;
    }

    public long getResponseBytes() {
        return responseBytes;
    }

    public void setDuration(long millis) {
        this.duration = millis;
    }

    public long getDuration() {
        return duration;
    }

    public void setTimestamp(long millis) {
        this.timestamp = millis;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setApplication(String appId) {
        this.application = appId;
    }

    public String getApplication() {
        return application;
    }

    public void setApiRequest(boolean isApi) {
        this.apiRequest = isApi;
    }

    public boolean getApiRequest() {
        return this.apiRequest;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return this.method;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setUserId(String id) {
        this.userId = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setNotification(String action) {
        this.notification = action;
    }

    public String getNotification() {
        return notification;
    }

    public void clientAddress(SocketAddress addr) {
        if (addr instanceof InetSocketAddress) {
            this.clientAddress = ((InetSocketAddress) addr).getHostString();
        } else {
            setClientAddress(addr.toString());
        }
    }

    public void setClientAddress(String addr) {
        if (addr.startsWith("/")) {
            addr = addr.substring(1);
        }
        int pos = addr.lastIndexOf(":");
        if (pos != -1) {
            addr = addr.substring(0, pos);
        }
        this.clientAddress = addr;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    @Override
    public String toString() {
        return notification == null ?
                "[" + clientAddress + "] " + status + " " + method + " " + uri + " (" + requestBytes + " / " + responseBytes + ") - " + duration + "ms"
                :
                "[" + clientAddress + "] " + status + " " + notification + " " + uri;
    }
}
