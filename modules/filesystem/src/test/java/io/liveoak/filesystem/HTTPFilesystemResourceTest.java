/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.filesystem;

import io.liveoak.container.SimpleConfig;
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
 * @author Bob McWhirter
 */
public class HTTPFilesystemResourceTest extends AbstractHTTPResourceTestCase {

    @Override
    public RootResource createRootResource() {
        return new FilesystemResource( "files" );
    }

    @Override
    public Config createConfig() {
        File dataDir = new File(this.projectRoot, "/target/test-data/one");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        SimpleConfig config = new SimpleConfig();
        config.put( "root", dataDir.getAbsolutePath() );
        return config;
    }


    @Test
    public void testEnumerateRoot() throws Exception {

        HttpGet get = new HttpGet( "http://localhost:8080/files" );
        get.addHeader( "Accept", "application/json" );

        try {
            System.err.println( "DO GET" );
            CloseableHttpResponse result = httpClient.execute( get );
            System.err.println( "=============>>>" );
            System.err.println( result );

            HttpEntity entity = result.getEntity();
            if ( entity.getContentLength() > 0 ) {
                entity.writeTo( System.err );
            }
            System.err.println( "\n<<<=============" );
            assertThat( result.getStatusLine().getStatusCode() ).isEqualTo( 200 );

        } finally {
            httpClient.close();
        }

    }
}
