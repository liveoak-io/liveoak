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

package org.projectodd.restafari.container.stomp.protocol;

import org.projectodd.restafari.container.stomp.Headers;

import java.util.Set;

/**
 * A base STOMP frame.
 *
 * @author Bob McWhirter
 */
public class StompFrame {

    /** STOMP versions */
    public enum Version {

        VERSION_1_0("1.0", 1.0F),
        VERSION_1_1("1.1", 1.1F),
        VERSION_1_2("1.2", 1.1F);

        private String versionString;
        private float versionValue;

        Version(String versionString, float versionValue) {
            this.versionString = versionString;
            this.versionValue = versionValue;
        }

        public boolean isAfter(Version version) {
            return versionValue > version.versionValue;
        }

        public boolean isBefore(Version version) {
            return versionValue < version.versionValue;
        }

        public static Version forVersionString(String versionString) {
            for (Version version : Version.values()) {
                if (versionString.equals( version.versionString ))
                    return version;
            }
            return null;
        }

        public static String supportedVersions() {
            StringBuffer buf = new StringBuffer();
            Version[] versions = Version.values();
            for (int i = 0; i < versions.length; i++) {
                if ( i > 0 ) {
                    buf.append( "," );
                }
                buf.append( versions[i].versionString );
            }
            return buf.toString();
        }

        public String versionString() {
            return versionString;
        }

    }

    /** Specification-supplied headers names. */
    public static class Header {

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

    public enum Command {
        STOMP( false ),
        CONNECT( false ),
        CONNECTED( false ),
        DISCONNECT( false ),

        SEND( true ),
        MESSAGE( true ),

        SUBSCRIBE( true ),
        UNSUBSCRIBE( true ),

        BEGIN( false ),
        COMMIT( false ),
        ACK( false ),
        NACK( false ),
        ABORT( false ),

        RECEIPT( false ),

        ERROR( true );

        private boolean hasContent;

        Command(boolean hasContent) {
            this.hasContent = hasContent;
        }

        public boolean hasContent() {
            return this.hasContent;
        }

        public byte[] getBytes() {
            return this.name().getBytes();
        }

        public String toString() {
            return this.name();
        }
    }

    /**
     * Create a new outbound frame.
     *
     * @param command
     */
    public StompFrame(Command command) {
        this.header = new FrameHeader( command );
    }

    public StompFrame(Command command, Headers headers) {
        this.header = new FrameHeader( command, headers );
    }

    public StompFrame(FrameHeader header) {
        this.header = header;
    }

    public Command getCommand() {
        return this.header.getCommand();
    }

    public String getHeader(String name) {
        return this.header.get( name );
    }

    public void setHeader(String name, String value) {
        this.header.set( name, value );
    }

    public Set<String> getHeaderNames() {
        return this.header.getNames();
    }

    public Headers getHeaders() {
        return this.header.getMap();
    }

    public String toString() {
        return "[" + getClass().getSimpleName() + ": header=" + this.header
                + "]";
    }

    private FrameHeader header;

}
