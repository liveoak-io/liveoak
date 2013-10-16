package org.projectodd.restafari.container.subscriptions;

import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.container.codec.ResourceCodec;
import org.projectodd.restafari.container.codec.ResourceEncoder;
import org.projectodd.restafari.spi.Resource;
import org.projectodd.restafari.stomp.server.StompConnection;

/**
 * @author Bob McWhirter
 */
public class StompSubscription implements Subscription {

    public StompSubscription(StompConnection connection, String destination, String subscriptionId, String contentType, ResourceCodec codec) {
        this.connection = connection;
        this.destination = destination;
        this.subscriptionId = subscriptionId;
        this.contentType = contentType;
        this.codec = codec;
        this.resourcePath = new ResourcePath(destination);
    }

    public ResourcePath resourcePath() {
        return this.resourcePath;
    }

    @Override
    public void resourceCreated(Resource resource) {
        /*
        try {
            this.connection.send(createMessage("create", 200, resourceState));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        */
    }

    @Override
    public void resourceUpdated(Resource resource) {
        /*
        try {
            this.connection.send(createMessage("update", 200, resourceState));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        */
    }

    @Override
    public void resourceDeleted(Resource resource) {
        /*
        try {
            this.connection.send(createMessage("delete", 200, resourceState));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        */
    }

    /*
    protected StompMessage createMessage(String action, int status, ResourceState resourceState) throws IOException {
        StompMessage message = new DefaultStompMessage();
        message.headers().put(Headers.SUBSCRIPTION, this.subscriptionId);
        message.headers().put(Headers.CONTENT_TYPE, this.contentType);
        message.headers().put("action", action);
        message.headers().put("status", "" + status);
        message.content(this.codec.encode(resourceState));
        return message;
    }
    */

    private StompConnection connection;
    private String destination;
    private String subscriptionId;
    private String contentType;
    private ResourceCodec codec;

    private final ResourcePath resourcePath;

}
