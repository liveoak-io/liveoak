
/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.stomp.common;

import io.liveoak.stomp.Headers;
import io.liveoak.stomp.Stomp;

import java.util.Set;

/**
 * A Frame-header encompasses the STOMP command keyword, along with
 * message headers.
 *
 * @author Bob McWhirter
 */
public class FrameHeader {

    public FrameHeader() {

    }

    public FrameHeader( Stomp.Command command ) {
        this.command = command;
    }

    public FrameHeader( Stomp.Command command, Headers headers ) {
        this.command = command;
        this.headers.putAll( headers );
    }

    public boolean isContentFrame() {
        return this.command.hasContent();
    }

    public void setCommand( Stomp.Command command ) {
        this.command = command;
    }

    public Stomp.Command command() {
        return this.command;
    }

    public void set( String name, String value ) {
        this.headers.put( name, value );
    }

    public String get( String name ) {
        return this.headers.get( name );
    }

    public Set<String> getNames() {
        return this.headers.keySet();
    }

    public Headers getMap() {
        return this.headers;
    }

    public int getContentLength() {
        String value = get( Headers.CONTENT_LENGTH.toString() );
        if ( value == null ) {
            return -1;
        }

        return Integer.parseInt( value );
    }

    public String toString() {
        return "[FrameHeader: command=" + this.command + "; headers=" + this.headers + "]";
    }

    private Stomp.Command command;
    private HeadersImpl headers = new HeadersImpl();
}
