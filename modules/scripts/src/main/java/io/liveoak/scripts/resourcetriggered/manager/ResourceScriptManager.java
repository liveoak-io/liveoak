package io.liveoak.scripts.resourcetriggered.manager;

import java.util.Set;

import io.liveoak.common.DefaultResourceResponse;
import io.liveoak.scripts.common.ScriptManager;
import io.liveoak.scripts.libraries.manager.LibraryManager;
import io.liveoak.scripts.objects.impl.LiveOakResourceRequest;
import io.liveoak.scripts.objects.impl.LiveOakResourceResponse;
import io.liveoak.scripts.objects.scripting.ScriptingResourceRequest;
import io.liveoak.scripts.resource.ScriptConfig;
import io.liveoak.scripts.resourcetriggered.resource.ResourceTriggeredScript;
import io.liveoak.scripts.resourcetriggered.resource.ScriptRegistry;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ResourceScriptManager extends ScriptManager {

    private ScriptRegistry scriptRegistry;

    public ResourceScriptManager(ScriptRegistry scriptRegistry, ScriptConfig scriptConfig, LibraryManager libraryManager) {
        super(scriptConfig, libraryManager);
        this.scriptRegistry = scriptRegistry;
    }

    public Object executeScripts(ResourceRequest request) throws Exception {

        ScriptingResourceRequest scriptingRequest = new ScriptingResourceRequest(request);
        String resourcePath = scriptingRequest.resourcePath().toString();
        RequestType type = scriptingRequest.requestType();

        ResourceTriggeredScript.FUNCTIONS resourceFunction = ResourceTriggeredScript.FUNCTIONS.PREREAD;
        switch (type) {
            case CREATE:
                resourceFunction = ResourceTriggeredScript.FUNCTIONS.PRECREATE;
                break;
            case UPDATE:
                resourceFunction = ResourceTriggeredScript.FUNCTIONS.PREUPDATE;
                break;
            case DELETE:
                resourceFunction = ResourceTriggeredScript.FUNCTIONS.PREDELETE;
                break;
        }

        Set<ResourceTriggeredScript> scripts = scriptRegistry.getByTarget(resourcePath, resourceFunction, true);

        // CREATE is special since we apply the create to /foo/bar to create /foo/bar/baz so we should also check /foo/bar/*
        if (resourceFunction == ResourceTriggeredScript.FUNCTIONS.PRECREATE) {
            scripts.addAll(scriptRegistry.getByPath(resourcePath + "/*", resourceFunction, true)) ;
        }

        for (ResourceTriggeredScript script : scripts) {
            Object reply = runScript(resourceFunction.getFunctionName(), script, scriptingRequest);
            if (reply != null) {
                return reply;
            }
        }

        return scriptingRequest;
    }

    public Object executeScripts(ResourceResponse response) throws Exception {

        String resourcePath = response.inReplyTo().resourcePath().toString();
        ResourceResponse.ResponseType type = response.responseType();

        ResourceTriggeredScript.FUNCTIONS resourceFunction = ResourceTriggeredScript.FUNCTIONS.POSTREAD;
        switch (type) {
            case CREATED:
                resourceFunction = ResourceTriggeredScript.FUNCTIONS.POSTCREATE;
                break;
            case READ:
                resourceFunction = ResourceTriggeredScript.FUNCTIONS.POSTREAD;
                break;
            case UPDATED:
                resourceFunction = ResourceTriggeredScript.FUNCTIONS.POSTUPDATE;
                break;
            case DELETED:
                resourceFunction = ResourceTriggeredScript.FUNCTIONS.POSTDELETE;
                break;
            case ERROR:
                resourceFunction = ResourceTriggeredScript.FUNCTIONS.ONERROR;
                break;
        }

        Set<ResourceTriggeredScript> scripts = scriptRegistry.getByTarget(resourcePath, resourceFunction, true);

        // CREATE is special since we apply the create to /foo/bar to create /foo/bar/baz so we should also check /foo/bar/*
        if (resourceFunction == ResourceTriggeredScript.FUNCTIONS.POSTCREATE) {
            scripts.addAll(scriptRegistry.getByPath(resourcePath + "/*", resourceFunction, true)) ;
        }

        for (ResourceTriggeredScript script : scripts) {
            Object reply = runScript(resourceFunction.getFunctionName(), script, response);
            if (reply != null) {
                return reply;
            }
        }

        if (resourceFunction == ResourceTriggeredScript.FUNCTIONS.POSTREAD) {
            //we are on a post read, which means expand could have been used to return expanded members

            //check if the members were set to be expanded or not
            Object handleMembers = handleMembers (response, response.state(), response.inReplyTo().requestContext().returnFields());
            if (handleMembers != null) {
                return handleMembers;
            }
        }

        return null;
    }

    protected Object handleMembers(ResourceResponse response, ResourceState state, ReturnFields returnFields) throws Exception{
        if (!returnFields.child(LiveOak.MEMBERS).isEmpty()) {
            for (ResourceState memberState: state.members()) {
                DefaultResourceResponse memberResponse = new DefaultResourceResponse(response.inReplyTo(),
                        ResourceResponse.ResponseType.READ, new Resource() {
                    @Override
                    public Resource parent() {
                        return response.resource();
                    }

                    @Override
                    public String id() {
                        return memberState.id();
                    }
                });
                memberResponse.setState(memberState);

                Set<ResourceTriggeredScript> memberScripts = scriptRegistry.getByTarget(memberState.uri().toString(), ResourceTriggeredScript.FUNCTIONS.POSTREAD, true);
                for (ResourceTriggeredScript memberScript: memberScripts) {
                    Object reply = runScript(ResourceTriggeredScript.FUNCTIONS.POSTREAD.getFunctionName(),memberScript , memberResponse);
                    if (reply != null) {
                        return reply;
                    }
                }

                handleMembers(response, memberState, returnFields.child(LiveOak.MEMBERS));
            }
        }
        return null;
    }

    protected Object runScript(String functionName, ResourceTriggeredScript script, ResourceResponse resourceResponse) throws Exception {

        Object response = runScript(functionName, script, new LiveOakResourceResponse(resourceResponse), getLibrary(script));

        ScriptingResourceRequest request = new ScriptingResourceRequest(resourceResponse.inReplyTo());
        return handleResponse(response, request);
    }

    protected Object runScript(String functionName, ResourceTriggeredScript script, ScriptingResourceRequest resourceRequest) throws Exception {

        Object response = runScript(functionName, script, new LiveOakResourceRequest(resourceRequest), getLibrary(script));

        return handleResponse(response, resourceRequest);
    }
}
