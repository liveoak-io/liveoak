/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * TODO: Maybe introduce another (simpler) object into SPI instead of this one
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class JsonWebToken {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final byte[] headerBytes;
    private final byte[] claimsBytes;
    private final byte[] signatureBytes;

    private final Header header;
    private final Claims claims;

    public JsonWebToken( String token ) {
        String[] t = token.split( "\\." );
        if ( t.length < 2 || t.length > 3 ) {
            throw new IllegalArgumentException();
        }

        headerBytes = Base64.getUrlDecoder().decode( t[0] );
        claimsBytes = Base64.getUrlDecoder().decode( t[1] );
        signatureBytes = t.length == 3 ? Base64.getUrlDecoder().decode( t[2] ) : null;

        header = parseHeader();
        claims = parseClaims();
    }

    public Header getHeader() {
        return header;
    }

    public byte[] getHeaderBytes() {
        return headerBytes;
    }

    public Claims getClaims() {
        return claims;
    }

    public byte[] getClaimsBytes() {
        return claimsBytes;
    }

    public byte[] getSignatureBytes() {
        return signatureBytes;
    }

    private Header parseHeader() {
        try {
            return mapper.readValue( headerBytes, Header.class );
        } catch ( IOException e ) {
            throw new IllegalArgumentException( e );
        }
    }

    private Claims parseClaims() {
        try {
            return mapper.readValue( claimsBytes, Claims.class );
        } catch ( IOException e ) {
            throw new IllegalArgumentException( e );
        }
    }

    @JsonIgnoreProperties( ignoreUnknown = true )
    public static class Header {
        @JsonProperty( value = "typ", required = false )
        private String type;
        @JsonProperty( value = "alg", required = false )
        private String algorithm;
        @JsonProperty( value = "cty", required = false )
        private String contentType;

        public String getType() {
            return type;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public String getContentType() {
            return contentType;
        }
    }

    @JsonIgnoreProperties( ignoreUnknown = true )
    public static class Claims {
        @JsonProperty( value = "iss", required = false )
        private String issuer;

        // TODO Renamed to 'sub'
        @JsonProperty( value = "prn", required = false )
        private String subject;

        @JsonProperty( value = "aud", required = false )
        private String audience;

        @JsonProperty( value = "exp", required = false )
        private long expiration;

        @JsonProperty( value = "nbf", required = false )
        private long notBefore;

        @JsonProperty( value = "iat", required = false )
        private long issuedAt;

        @JsonProperty( value = "jti", required = false )
        private String id;

        @JsonProperty( value = "typ", required = false )
        private String type;

        @JsonProperty( value = "issuedFor", required = false )
        public String issuedFor;

        @JsonProperty( value = "trusted-certs", required = false )
        protected Set<String> trustedCertificates;

        @JsonProperty( value = "realm_access", required = false )
        private Access realmAccess;

        @JsonProperty( value = "resource_access", required = false )
        private Map<String, Access> resourceAccess = new HashMap<String, Access>();

        public String getIssuer() {
            return issuer;
        }

        public String getSubject() {
            return subject;
        }

        public String getAudience() {
            return audience;
        }

        public long getExpiration() {
            return expiration;
        }

        public long getNotBefore() {
            return notBefore;
        }

        public long getIssuedAt() {
            return issuedAt;
        }

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getIssuedFor() {
            return issuedFor;
        }

        public Set<String> getTrustedCertificates() {
            return trustedCertificates;
        }

        public Access getRealmAccess() {
            return realmAccess;
        }

        public Map<String, Access> getResourceAccess() {
            return resourceAccess;
        }

        @JsonIgnore
        public boolean isExpired() {
            long time = System.currentTimeMillis() / 1000;
            return time > expiration;
        }

        @JsonIgnore
        public boolean isNotBefore() {
            return ( System.currentTimeMillis() / 1000 ) >= notBefore;
        }

        @JsonIgnore
        public boolean isActive() {
            return ( !isExpired() || expiration == 0 ) && ( isNotBefore() || notBefore == 0 );
        }
    }

    @JsonIgnoreProperties( ignoreUnknown = true )
    public static class Access {
        @JsonProperty( "roles" )
        protected java.util.Set<String> roles;
        @JsonProperty( "verify_caller" )
        protected Boolean verifyCaller;

        public Set<String> getRoles() {
            return roles;
        }

        public Boolean getVerifyCaller() {
            return verifyCaller;
        }
    }

}
