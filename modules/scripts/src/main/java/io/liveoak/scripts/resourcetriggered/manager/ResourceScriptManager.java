package io.liveoak.scripts.resourcetriggered.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.liveoak.common.DefaultResourceErrorResponse;
import io.liveoak.common.DefaultResourceResponse;
import io.liveoak.scripts.libraries.manager.LibraryManager;
import io.liveoak.scripts.objects.impl.LiveOakResource;
import io.liveoak.scripts.objects.impl.LiveOakResourceRequest;
import io.liveoak.scripts.objects.impl.LiveOakResourceResponse;
import io.liveoak.scripts.objects.impl.exception.LiveOakCreateNotSupportedException;
import io.liveoak.scripts.objects.impl.exception.LiveOakDeleteNotSupportedException;
import io.liveoak.scripts.objects.impl.exception.LiveOakException;
import io.liveoak.scripts.objects.impl.exception.LiveOakNotAcceptableException;
import io.liveoak.scripts.objects.impl.exception.LiveOakNotAuthorizedException;
import io.liveoak.scripts.objects.impl.exception.LiveOakReadNotSupportedException;
import io.liveoak.scripts.objects.impl.exception.LiveOakResourceAlreadyExistsException;
import io.liveoak.scripts.objects.impl.exception.LiveOakResourceNotFoundException;
import io.liveoak.scripts.objects.impl.exception.LiveOakUpdateNotSupportedException;
import io.liveoak.scripts.objects.scripting.ScriptingResourceRequest;
import io.liveoak.scripts.resourcetriggered.resource.Script;
import io.liveoak.scripts.resourcetriggered.resource.ScriptMap;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourceErrorResponse;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import org.dynjs.Config;
import org.dynjs.runtime.DynJS;
import org.dynjs.runtime.GlobalObject;
import org.dynjs.runtime.Runner;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ResourceScriptManager {

    private ScriptMap scriptMap;

    LibraryManager libraryManager;


    public ResourceScriptManager(ScriptMap scriptMap,LibraryManager libraryManager) {
        this.libraryManager = libraryManager;
        this.scriptMap = scriptMap;
    }

    public Object executeScripts(ResourceRequest request) {

        ScriptingResourceRequest scriptingRequest = new ScriptingResourceRequest(request);
        String resourcePath = scriptingRequest.resourcePath().toString();
        RequestType type = scriptingRequest.requestType();

        Script.FUNCTIONS resourceFunction = Script.FUNCTIONS.PREREAD;
        switch (type) {
            case CREATE:
                resourceFunction = Script.FUNCTIONS.PRECREATE;
                break;
            case UPDATE:
                resourceFunction = Script.FUNCTIONS.PREUPDATE;
                break;
            case DELETE:
                resourceFunction = Script.FUNCTIONS.PREDELETE;
                break;
        }

        Set<Script> scripts = scriptMap.getByTarget(resourcePath, resourceFunction, true);

        // CREATE is special since we apply the create to /foo/bar to create /foo/bar/baz so we should also check /foo/bar/*
        if (resourceFunction == Script.FUNCTIONS.PRECREATE) {
            scripts.addAll(scriptMap.getByPath(resourcePath + "/*", resourceFunction, true)) ;
        }

        for (Script script : scripts) {
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

        Script.FUNCTIONS resourceFunction = Script.FUNCTIONS.POSTREAD;
        switch (type) {
            case CREATED:
                resourceFunction = Script.FUNCTIONS.POSTCREATE;
                break;
            case READ:
                resourceFunction = Script.FUNCTIONS.POSTREAD;
                break;
            case UPDATED:
                resourceFunction = Script.FUNCTIONS.POSTUPDATE;
                break;
            case DELETED:
                resourceFunction = Script.FUNCTIONS.POSTDELETE;
                break;
            case ERROR:
                resourceFunction = Script.FUNCTIONS.ONERROR;
                break;
        }

        Set<Script> scripts = scriptMap.getByTarget(resourcePath, resourceFunction, true);

        // CREATE is special since we apply the create to /foo/bar to create /foo/bar/baz so we should also check /foo/bar/*
        if (resourceFunction == Script.FUNCTIONS.POSTCREATE) {
            scripts.addAll(scriptMap.getByPath(resourcePath + "/*", resourceFunction, true)) ;
        }

        for (Script script : scripts) {
            Object reply = runScript(resourceFunction.getFunctionName(), script, response);
            if (reply != null) {
                return reply;
            }
        }

        if (resourceFunction == Script.FUNCTIONS.POSTREAD) {
            //we are on a post read, which means expand could have been used to return expanded members

            //check if the members were set to be expanded or not
            Object handleMembers = handleMembers (response, response.state(), response.inReplyTo().requestContext().returnFields());
            if (handleMembers != null) {
                return handleMembers;
            }
        }

        return null;
    }

    protected Object handleMembers(ResourceResponse response, ResourceState state, ReturnFields returnFields) {
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

                Set<Script> memberScripts = scriptMap.getByTarget(memberState.uri().toString(), Script.FUNCTIONS.POSTREAD, true);
                for (Script memberScript: memberScripts) {
                    Object reply = runScript(Script.FUNCTIONS.POSTREAD.getFunctionName(),memberScript , memberResponse);
                    if (reply != null) {
                        return reply;
                    }
                }

                handleMembers(response, memberState, returnFields.child(LiveOak.MEMBERS));
            }
        }
        return null;
    }

    protected Object runScript(String functionName, Script script, ResourceResponse resourceResponse) {

        Map<String, Object> library = new HashMap<>();

        for (String libraryName : script.getLibraries()) {
            library.put(libraryName, libraryManager.getLibrary(libraryName).object());
        }

        Config config = new Config();
        config.setCompileMode(Config.CompileMode.OFF); //TODO: probably shouldn't be needed, check with a newer version of DynJS

        DynJS dynJS = new DynJS(config);

        GlobalObject globalObject = dynJS.getDefaultExecutionContext().getGlobalObject();
        configureGlobalObject(globalObject);

        dynJS.evaluate(script.getScriptBufferAsString());

        String source = configureFunction(globalObject, functionName, new LiveOakResourceResponse(resourceResponse), library);

        Runner runner = dynJS.newRunner();
        runner.withSource(source);

        Object response = runner.evaluate();
        ScriptingResourceRequest request = new ScriptingResourceRequest(resourceResponse.inReplyTo());
        return handleResponse(response, request);
    }

    protected Object runScript(String functionName, Script script, ScriptingResourceRequest resourceRequest) {
        Map<String, Object> library = new HashMap<>();

        if (script.getLibraries() != null) {
            for (String libraryName : script.getLibraries()) {
                library.put(libraryName, libraryManager.getLibrary(libraryName).object());
            }
        }

        Config config = new Config();
        config.setCompileMode(Config.CompileMode.OFF); //TODO: probably shouldn't be needed, check with a newer version of DynJS

        DynJS dynJS = new DynJS(config);

        GlobalObject globalObject = dynJS.getDefaultExecutionContext().getGlobalObject();
        configureGlobalObject(globalObject);

        dynJS.evaluate(script.getScriptBufferAsString());

        String source = configureFunction(globalObject, functionName, new LiveOakResourceRequest(resourceRequest), library);

        Runner runner = dynJS.newRunner();
        runner.withSource(source);

        Object response = runner.evaluate();
        return handleResponse(response, resourceRequest);
    }

    //TODO: REMOVE AND EXTEND SCRIPTMANAGER
    protected String configureFunction(GlobalObject globalObject, String functionName, Object... args) {
        //TODO: figure out the right approach to adding parameters to functions
        String prefix = "__liveoak_" + functionName;

        String source = functionName + "(" ;
        for (int i = 0; i< args.length; i++) {
            String argString = prefix + "_" + i;
            globalObject.put(argString, args[i]);

            source += argString;
            if (i + 1 < args.length) {
                source += ", ";
            }
        }

        return source + ")";
    }

    //TODO: REMOVE AND EXTEND SCRIPTMANAGER
    //TODO: move this to a more common ScriptManager to be used by the Endpoint and Scheduled Scripts
    protected void configureGlobalObject(GlobalObject globalObject) {
        Map<String, Class> liveoakMap = new HashMap<String, Class>();
        liveoakMap.put("Resource", LiveOakResource.class);
        liveoakMap.put("Error", LiveOakException.class);
        liveoakMap.put("NotAcceptableError", LiveOakNotAcceptableException.class);
        liveoakMap.put("ResourceAlreadyExistsError", LiveOakResourceAlreadyExistsException.class);
        liveoakMap.put("UpdateNotSupportedError", LiveOakUpdateNotSupportedException.class);
        liveoakMap.put("ReadNotSupportedError", LiveOakReadNotSupportedException.class);
        liveoakMap.put("ResourceNotFoundError", LiveOakResourceNotFoundException.class);
        liveoakMap.put("NotAuthorizedError", LiveOakNotAuthorizedException.class);
        liveoakMap.put("DeleteNotSupportedError", LiveOakDeleteNotSupportedException.class);
        liveoakMap.put("CreateNotSupportedError", LiveOakCreateNotSupportedException.class);

        globalObject.put("liveoak", liveoakMap);
    }

    //TODO: REMOVE AND EXTEND SCRIPTMANAGER
    //TODO: move this to a util class or a more common ScriptManager class
    protected Object handleResponse(Object response, ScriptingResourceRequest request) {
        if (response instanceof LiveOakException) {
            LiveOakException liveOakException = (LiveOakException)response;
            ResourceErrorResponse.ErrorType errorType = ResourceErrorResponse.ErrorType.INTERNAL_ERROR;
            if (response instanceof LiveOakResourceAlreadyExistsException) {
                errorType = ResourceErrorResponse.ErrorType.RESOURCE_ALREADY_EXISTS;
            } else if (response instanceof LiveOakNotAcceptableException) {
                errorType =  ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE;
            } else if (response instanceof LiveOakUpdateNotSupportedException) {
                errorType =  ResourceErrorResponse.ErrorType.UPDATE_NOT_SUPPORTED;
            }else if (response instanceof LiveOakReadNotSupportedException) {
                errorType =  ResourceErrorResponse.ErrorType.READ_NOT_SUPPORTED;
            }else if (response instanceof LiveOakResourceNotFoundException) {
                errorType =  ResourceErrorResponse.ErrorType.NO_SUCH_RESOURCE;
            }else if (response instanceof LiveOakNotAuthorizedException) {
                errorType =  ResourceErrorResponse.ErrorType.NOT_AUTHORIZED;
            }else if (response instanceof LiveOakDeleteNotSupportedException) {
                errorType =  ResourceErrorResponse.ErrorType.DELETE_NOT_SUPPORTED;
            }else if (response instanceof LiveOakCreateNotSupportedException) {
                errorType =  ResourceErrorResponse.ErrorType.CREATE_NOT_SUPPORTED;
            }

            ResourceErrorResponse resourceErrorResponse = new DefaultResourceErrorResponse(request, errorType, liveOakException.getMessage());
            return resourceErrorResponse;
        }
        return null;
    }
}
