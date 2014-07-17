package io.liveoak.ups;

import java.net.URI;

import io.liveoak.ups.resource.config.UPSRootConfigResource;
import org.jboss.aerogear.unifiedpush.JavaSender;
import org.jboss.aerogear.unifiedpush.SenderClient;
import org.jboss.aerogear.unifiedpush.message.MessageResponseCallback;
import org.jboss.aerogear.unifiedpush.message.UnifiedMessage;
import org.jboss.logging.Logger;

/**
 * Handles the communication between the LiveOak instances and a UPS instance.
 *
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class UPS {

    private static final Logger log = Logger.getLogger("io.liveoak.ups");

    //LiveOak specifics to be added as attribute to the message
    //Using io.liveoak.push as prefix to not conflict with an application's specified values in the message
    public static final String LIVEOAK_RESOURCE_URL = "io.liveoak.push.url";
    public static final String LIVEOAK_RESOURCE_EVENT = "io.liveoak.push.event";

    /**
     * The type of resource event.
     */
    public static enum EventType {
        CREATED("created"),
        UPDATED("updated"),
        DELETED("deleted");

        private final String name;

        private EventType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    UPSRootConfigResource upsRootConfigResource;

    public UPS(UPSRootConfigResource upsRootConfigResource) {
        this.upsRootConfigResource = upsRootConfigResource;
    }


    /**
     * Sends a message to the UPS instance to send out push notifications to registered applications
     *
     * @param URI          The URI of the updated resource
     * @param eventType    The type of event which occurred
     * @param subscription The object containing the message and specified recipients
     */
    public void send(URI URI, EventType eventType, UPSSubscription subscription) {
        JavaSender sender = new SenderClient(upsRootConfigResource.getUPSServerURL());

        // setup the application specifics
        UnifiedMessage.Builder builder = new UnifiedMessage.Builder()
                .pushApplicationId(upsRootConfigResource.getApplicationId())
                .masterSecret(upsRootConfigResource.getMasterSecret());

        // setup who is to receive the message
        builder.variants(subscription.variants());
        builder.aliases(subscription.aliases());
        builder.categories(subscription.categories());
        builder.deviceType(subscription.deviceTypes());

        if (subscription.simplePush() != null) {
            builder.simplePush(subscription.simplePush().toString());

            //increment the simplePush value, otherwise next time the simple-push server will ignore the notification
            subscription.simplePush(subscription.simplePush() + 1);
        }

        //setup the message itself

        builder.attributes(subscription.message());
        // specify the liveoak specifics of the message, overwrite if needed.
        builder.attribute(LIVEOAK_RESOURCE_URL, URI.toString());
        builder.attribute(LIVEOAK_RESOURCE_EVENT, eventType.toString());


        sender.send(builder.build(), new MessageResponseCallback() {
            @Override
            public void onComplete(int i) {
                //do nothing for now
            }

            @Override
            public void onError(Throwable throwable) {
                //TODO: how to handle when there is an error between LiveOak and UPS?
                // should we just log the error, try again after x many seconds and y many retries?
                log.error("Error trying to send notification to UPS server", throwable);
            }
        });
    }

}
