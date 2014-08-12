package io.liveoak.scripts.resourcetriggered.interceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.liveoak.common.DefaultResourceErrorResponse;
import io.liveoak.scripts.objects.Util;
import io.liveoak.scripts.objects.impl.exception.LiveOakException;
import io.liveoak.scripts.resourcetriggered.manager.ResourceScriptManager;
import io.liveoak.spi.Application;
import io.liveoak.spi.ResourceErrorResponse;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.container.interceptor.DefaultInterceptor;
import io.liveoak.spi.container.interceptor.InboundInterceptorContext;
import io.liveoak.spi.container.interceptor.OutboundInterceptorContext;
import org.dynjs.exception.ThrowException;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScriptInterceptor extends DefaultInterceptor {

    Map<String, ResourceScriptManager> managers;

    private static final String DYNJS_ERROR_PREFIX = "Error: ";

    public ScriptInterceptor() {
        managers = new HashMap<>();
    }

    @Override
    public void onInbound(InboundInterceptorContext context) throws Exception {

        try {
            String applicationName = getApplicationName(context.request());

            ResourceScriptManager manager = managers.get(applicationName);

            if (manager != null) {
                Object reply = manager.executeScripts(context.request());
                if (reply instanceof ResourceRequest) {
                    context.forward((ResourceRequest) reply);
                } else if (reply instanceof ResourceResponse) {
                    context.replyWith((ResourceResponse) reply);
                } else {
                    context.forward();
                }
            }  else {
                context.forward();
            }

        } catch (Exception e) {
            e.printStackTrace();
            String message = "Error processing request";
            if (e.getMessage() != null && !e.getMessage().equals(DYNJS_ERROR_PREFIX)) {
                message = e.getMessage();
                if (message.startsWith(DYNJS_ERROR_PREFIX)) {
                    message = message.substring(DYNJS_ERROR_PREFIX.length());
                    context.replyWith(new DefaultResourceErrorResponse(context.request(), ResourceErrorResponse.ErrorType.INTERNAL_ERROR, message));
                    return;
                }
            } else if (e instanceof ThrowException) {
                Object value = ((ThrowException)e).getValue();
                if (value instanceof LiveOakException) {
                    context.replyWith(Util.getErrorResponse(context.request(), (LiveOakException)value));
                    return;
                }
            }
                context.replyWith(new DefaultResourceErrorResponse(context.request(), ResourceErrorResponse.ErrorType.INTERNAL_ERROR, message));

        }
    }

    @Override
    public void onOutbound(OutboundInterceptorContext context) throws Exception {

        try {
            String applicationName = getApplicationName(context.response().inReplyTo());
            ResourceScriptManager manager = managers.get(applicationName);
            if (manager != null) {
                Object reply = manager.executeScripts(context.response());
                if (reply instanceof ResourceResponse) {
                    context.forward((ResourceResponse) reply);
                } else {
                    context.forward();
                }
            } else {
                context.forward();
            }
        } catch (Exception e) {
            e.printStackTrace();
            String message = "Error processing response";
            //TODO: remove the "Error: " check here, its because DynJS for some reason uses a crappy empty error message.
            if (e.getMessage() != null && !e.getMessage().equals(DYNJS_ERROR_PREFIX)) {
                message = e.getMessage();
                if (message.startsWith(DYNJS_ERROR_PREFIX)) {
                    message = message.substring(DYNJS_ERROR_PREFIX.length());
                }
            } else if (e instanceof ThrowException) {
                Object value = ((ThrowException)e).getValue();
                if (value instanceof LiveOakException) {
                    context.forward(Util.getErrorResponse(context.response().inReplyTo(), (LiveOakException)value));
                    return;
                }
            }
            context.forward(new DefaultResourceErrorResponse(context.response().inReplyTo(), ResourceErrorResponse.ErrorType.INTERNAL_ERROR, message));
        }
    }

    @Override
    public void onComplete(UUID requestId) {
        //currently do nothing.
    }

    private String getApplicationName(ResourceRequest request) {
        //TODO: once we have the application actually being added to the requestContext remove getting the name from the ResourcePath
        List<ResourcePath.Segment> resourcePaths = request.resourcePath().segments();
        String applicationName = "/";
        if (resourcePaths.size() > 0) {
            applicationName = resourcePaths.get(0).name();
        }

        // TODO: This is proper way to check, but application is currently not set
        Application application = request.requestContext().application();
        if (application != null) {
            applicationName = application.name();
        }

        return applicationName;
    }

    public void addManager(String applicationName, ResourceScriptManager manager) {
        managers.put(applicationName, manager);
    }

    public void removeManager(String applicationName) {
        managers.remove(applicationName);
    }
}
