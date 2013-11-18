/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp;

/**
 * @author Bob McWhirter
 */
public class Stomp {
    /**
     * STOMP versions
     */
    public enum Version {

        VERSION_1_0( "1.0", 1.0F ),
        VERSION_1_1( "1.1", 1.1F ),
        VERSION_1_2( "1.2", 1.1F );

        private String versionString;
        private float versionValue;

        Version( String versionString, float versionValue ) {
            this.versionString = versionString;
            this.versionValue = versionValue;
        }

        public boolean isAfter( Version version ) {
            return versionValue > version.versionValue;
        }

        public boolean isBefore( Version version ) {
            return versionValue < version.versionValue;
        }

        public static Version forVersionString( String versionString ) {
            for ( Version version : Version.values() ) {
                if ( versionString.equals( version.versionString ) )
                    return version;
            }
            return null;
        }

        public static String supportedVersions() {
            StringBuffer buf = new StringBuffer();
            Version[] versions = Version.values();
            for ( int i = 0; i < versions.length; i++ ) {
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

    public enum Command {
        STOMP( false ),
        CONNECT( false ),
        CONNECTED( false ),
        DISCONNECT( false ),

        SEND( true ),
        MESSAGE( true ),

        SUBSCRIBE( false ),
        UNSUBSCRIBE( false ),

        BEGIN( false ),
        COMMIT( false ),
        ACK( false ),
        NACK( false ),
        ABORT( false ),

        RECEIPT( false ),

        ERROR( true );

        private boolean hasContent;

        Command( boolean hasContent ) {
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

}
