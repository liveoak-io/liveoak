package org.projectodd.restafari.container.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class JsonWebToken {

    private static final Base64 base64 = new Base64(true);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final byte[] headerBytes;
    private final byte[] claimsBytes;
    private final byte[] signatureBytes;

    private final Header header;
    private final Claims claims;

    public JsonWebToken(String token) {
        String[] t = token.split("\\.");
        if (t.length < 2 || t.length > 3) {
            throw new IllegalArgumentException();
        }

        headerBytes = base64.decode(t[0]);
        claimsBytes = base64.decode(t[1]);
        signatureBytes = t.length == 3 ? base64.decode(t[2]) : null;

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
            return mapper.readValue(headerBytes, Header.class);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Claims parseClaims() {
        try {
            return mapper.readValue(claimsBytes, Claims.class);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        @JsonProperty(value = "typ", required = false)
        private String type;
        @JsonProperty(value = "alg", required = false)
        private String algorithm;

        public String getType() {
            return type;
        }

        public String getAlgorithm() {
            return algorithm;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Claims {
        @JsonProperty(value = "iss", required = false)
        private String issuer;

        // TODO Renamed to 'sub'
        @JsonProperty(value = "prn", required = false)
        private String subject;

        @JsonProperty(value = "aud", required = false)
        private String audience;

        @JsonProperty(value = "exp", required = false)
        private long expiration;

        @JsonProperty(value = "nbf", required = false)
        private long notBefore;

        @JsonProperty(value = "iat", required = false)
        private long issuedAt;

        @JsonProperty(value = "jti", required = false)
        private String id;

        @JsonProperty(value = "typ", required = false)
        private String type;

        @JsonProperty(value = "realm_access", required = false)
        private Access realmAccess;

        @JsonProperty(value = "resource_access", required = false)
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

        public Access getRealmAccess() {
            return realmAccess;
        }

        public Map<String, Access> getResourceAccess() {
            return resourceAccess;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Access {
        @JsonProperty("roles")
        protected java.util.Set<String> roles;

        public Set<String> getRoles() {
            return roles;
        }
    }

}
