package org.projectodd.restafari.container;

import io.netty.channel.nio.NioEventLoopGroup;

import java.net.InetAddress;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;

public class BasicServerTest {

    @Test
    public void testServer() throws Exception {

        InMemoryObjectResourceController controller = new InMemoryObjectResourceController();
        controller.addCollection( "people" );
        controller.addCollection( "dogs" );
        
        Container container = new Container();
        container.registerResourceController( "/people", controller );
        container.registerResourceController( "/dogs", controller );
        
        UnsecureServer server = new UnsecureServer(container, InetAddress.getByName("localhost"), 8080, new NioEventLoopGroup());

        server.start();

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        //HttpGet get = new HttpGet( "http://localhost:8080/tacos" );
        //HttpGet get = new HttpGet( "http://localhost:8080/people" );
        HttpGet get = new HttpGet( "http://localhost:8080/people/bob" );
        get.addHeader( "Accept", "text/plain" );
        CloseableHttpResponse result = httpClient.execute( get );

        System.err.println("=============>>>");
        System.err.println(result);
        result.getEntity().writeTo( System.err );
        System.err.println("<<<=============");

        System.err.println("closing");
        httpClient.close();
        System.err.println("closed");

        server.stop();

    }

}
