package io.liveoak.keycloak;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.TypeReference;
import org.keycloak.util.PemUtils;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class KeycloakConfig {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String baseUrl = "http://localhost:8383/auth";

    private boolean loadKeys = true;

    private Map<String, String> publicKeyPems = new HashMap<>();
    private Map<String, PublicKey> publicKeys = new HashMap<>();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public boolean isLoadKeys() {
        return loadKeys;
    }

    public void setLoadKeys(boolean loadKeys) {
        this.loadKeys = loadKeys;
    }

    public Map<String, String> getPublicKeyPems() {
        return publicKeyPems;
    }

    public void setPublicKeyPems(Map<String, String> publicKeyPems) {
        this.publicKeyPems = publicKeyPems;
    }

    public PublicKey getPublicKey(String realm) throws Exception {
        PublicKey publicKey = publicKeys.get(realm);
        if (publicKey == null) {
            String pem = getPublicKeyPem(realm);
            if (pem != null) {
                publicKey = PemUtils.decodePublicKey(pem);
                publicKeys.put(realm, publicKey);
            } else {
                throw new Exception("Public key not found for realm " + realm);
            }
        }
        return publicKey;
    }

    private String getPublicKeyPem(String realm) throws Exception {
        String pem = publicKeyPems.get(realm);
        if (pem == null) {
            if (loadKeys) {
                pem = loadPublicKey(realm);
                publicKeyPems.put(realm, pem);
            }
        }
        return pem;
    }

    private String loadPublicKey(String realm) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        Map<String, String> realmDetails;

        String realmUrl = baseUrl + "/rest/realms/" + realm;

        HttpGet get = new HttpGet(realmUrl);
        get.setHeader(HttpHeaders.ACCEPT, "application/json");

        CloseableHttpResponse response;
        try {
            response = client.execute(get);
        } catch (Exception e) {
            throw new Exception("Failed to load public key for realm " + realm, e);
        }

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new Exception("Failed to load public key for realm " + realm + ": " + response.getStatusLine());
        }

        JsonNode node = objectMapper.readTree(response.getEntity().getContent());
        if (!node.get("realm").getTextValue().equals(realm)) {
            throw new Exception("Invalid response, realm doesn't match");
        }
        return node.get("public_key").getTextValue();
    }

}


