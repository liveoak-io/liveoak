/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.git;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.git.extension.GitExtension;
import io.liveoak.testtools.AbstractHTTPResourceTestCase;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class HTTPGitResourceTest extends AbstractHTTPResourceTestCase {

    @Override
    protected File applicationDirectory() {
        return new File( this.projectRoot, "target/test-app" );
    }

    @Override
    public void loadExtensions() throws Exception {
        new File( applicationDirectory(), "git" ).mkdirs();
        loadExtension( "git", new GitExtension() );
        installResource( "git", "git", JsonNodeFactory.instance.objectNode() );
    }

    @Test
    public void enumerateRoot() throws Exception {
        HttpGet get = new HttpGet("http://localhost:8080/testApp/git");
        get.addHeader("Accept", "application/json");

        try {
            System.out.println("DO GET");
            CloseableHttpResponse result = httpClient.execute(get);
            System.out.println("=============>>>");
            System.out.println(result);

            HttpEntity entity = result.getEntity();
            if (entity.getContentLength() > 0) {
                entity.writeTo(System.out);
            }
            System.out.println("\n<<<=============");
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);
        } finally {
            httpClient.close();
        }
    }

}
