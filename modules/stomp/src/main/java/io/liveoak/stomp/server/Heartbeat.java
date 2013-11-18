/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp.server;

/**
 * @author Bob McWhirter
 */
public class Heartbeat {

    public int calculateDuration( int senderDuration, int receiverDuration ) {
        return Math.max( senderDuration, receiverDuration );
    }

    public int getClientReceive() {
        return clientReceive;
    }

    public int getClientSend() {
        return clientSend;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public int getServerReceive() {
        return serverReceive;
    }

    public int getServerSend() {
        return serverSend;
    }

    public void setClientReceive( int clientReceive ) {
        this.clientReceive = clientReceive;
    }

    public void setClientSend( int clientSend ) {
        this.clientSend = clientSend;
    }

    public void setServerReceive( int serverReceive ) {
        this.serverReceive = serverReceive;
    }

    public void setServerSend( int serverSend ) {
        this.serverSend = serverSend;
    }

    public synchronized void touch() {
        lastUpdate = System.currentTimeMillis();
    }

    private int clientSend;
    private int clientReceive;
    private int serverSend = 1000;
    private int serverReceive = 1000;
    private long lastUpdate = System.currentTimeMillis();

}

