/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.git;

import io.liveoak.spi.Config;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.testtools.AbstractHTTPResourceTestCase;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class HTTPGitResourceTest extends AbstractHTTPResourceTestCase {
    @Override
    public RootResource createRootResource() {
        return new GitRepoResource( "git" );
    }

    @Override
    public Config createConfig() {
        File repoDir = new File(this.projectRoot, "/target/repo_http");
        if (!repoDir.exists()) {
            repoDir.mkdirs();
        }

        Config config = super.createConfig();
        config.put("repoPath", repoDir.getAbsolutePath());
        return config;
    }

    @Test
    public void enumerateRoot() throws Exception {
        HttpGet get = new HttpGet( "http://localhost:8080/git" );
        get.addHeader( "Accept", "application/json" );

        try {
            System.out.println( "DO GET" );
            CloseableHttpResponse result = httpClient.execute( get );
            System.out.println( "=============>>>" );
            System.out.println( result );

            HttpEntity entity = result.getEntity();
            if ( entity.getContentLength() > 0 ) {
                entity.writeTo( System.out );
            }
            System.out.println( "\n<<<=============" );
            assertThat( result.getStatusLine().getStatusCode() ).isEqualTo( 200 );
        } finally {
            httpClient.close();
        }
    }
}
