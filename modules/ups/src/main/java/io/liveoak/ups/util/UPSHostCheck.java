package io.liveoak.ups.util;

import java.io.IOException;

import io.liveoak.spi.ResourceErrorResponse;
import io.liveoak.spi.exceptions.ResourceProcessingException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * @author Ken Finnigan
 */
public final class UPSHostCheck {
    public static boolean isValid(String upsUrl) throws Exception {
        boolean valid = false;

        if (upsUrl != null && upsUrl.length() > 1) {
            HttpGet get = new HttpGet(upsUrl + "/rest/ping/");

            try (CloseableHttpClient client = HttpClients.custom()
                    .disableContentCompression()
                    .build()) {
                try (CloseableHttpResponse response = client.execute(get)) {
                    if (response.getStatusLine().getStatusCode() == 401) {
                        // We assume that a 401 being returned signifies that the UPS instance is valid because it's
                        // got a secured endpoint that we failed authorization on
                        valid = true;
                    }
                } catch (IOException e) {
                    throw new ResourceProcessingException(ResourceErrorResponse.ErrorType.INTERNAL_ERROR, "Error trying to access UPS endpoint: " + upsUrl, e);
                }
            }
        }
        return valid;
    }
}
