package org.projectodd.restafari.container.auth;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class JsonWebTokenTest {

    public static String SAMPLE_TOKEN = "eyJhbGciOiJSUzI1NiJ9.eyJpc3N1ZWRGb3IiOiJ0ZXN0LWFwcCIsImp0aSI6IjMtMTM4MTU5MTE3NTIyNiIsImV4cCI6MTM4MTU5MTQ3NSwiaWF0IjoxMzgxNTkxMTc1LCJhdWQiOiJ0ZXN0IiwicHJuIjoiYSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJ1c2VyIl19fQ.c1fuWB7yz0aOqAEe5dVFEt99DXfzt7LzL1plmwC5NmNcMNoyfLWUT3HATPH1Ee-3vO05bLXoBHcIhegKNpthO6qC2az-xNoKK2rUauJVa69Xiy0dsnVqWMxAwgQMUwuES5FeH7F5ht74ndqMpTimxaXCNKiX--srpyQM1xYx91k";

    @Test
    public void testToken() throws IOException {
        JsonWebToken token = new JsonWebToken(SAMPLE_TOKEN);

        assertEquals("RS256", token.getHeader().getAlgorithm());
        assertEquals("test", token.getClaims().getAudience());
        assertEquals("a", token.getClaims().getSubject());
        assertEquals(1381591175, token.getClaims().getIssuedAt());
        assertEquals(1381591475, token.getClaims().getExpiration());

        assertEquals(1, token.getClaims().getRealmAccess().getRoles().size());
        assertTrue(token.getClaims().getRealmAccess().getRoles().contains("user"));

        System.out.println(new String(token.getClaimsBytes()));
    }

}
