package io.liveoak.scripts.resourcetriggered.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.liveoak.common.DefaultResourceErrorResponse;
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
import io.liveoak.scripts.resourcetriggered.resource.ResourceScript;
import io.liveoak.scripts.resourcetriggered.resource.ScriptFileResource;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourceErrorResponse;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.ResourceResponse;
import org.dynjs.Config;
import org.dynjs.runtime.DynJS;
import org.dynjs.runtime.GlobalObject;
import org.dynjs.runtime.Runner;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ResourceScriptManager {

    //TODO: see if there is an easier way of handling this....
    Map<String, ResourceScript> scriptIDMap = new HashMap<>();
    Map<ScriptKey, List<String>> scriptPathMap = new HashMap<>();

    private class ScriptKey {
        private String path;
        private String function;

        public ScriptKey(String path, String function) {
            this.path = path;
            this.function = function;
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof ScriptKey) {
                ScriptKey scriptKey = (ScriptKey) object;
                if (this.path.equals(scriptKey.path) && this.function.equals(scriptKey.function)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            return path.hashCode() + function.hashCode();
        }
    }

    LibraryManager libraryManager;


    public ResourceScriptManager(LibraryManager libraryManager) {
        this.libraryManager = libraryManager;
    }

    public void addResource(ResourceScript resource) {
        String id = resource.id();
        if (resource.enabled()) {
            String path = resource.target();

            ScriptFileResource scriptFileResource = resource.getScriptFile();
            if (scriptFileResource != null && !scriptFileResource.provides().isEmpty()) {
                scriptIDMap.put(id, resource);

                List<ScriptFileResource.RESOURCE_FUNCTION> provides = resource.getScriptFile().provides();
                for (ScriptFileResource.RESOURCE_FUNCTION function : provides) {
                    ScriptKey key = new ScriptKey(path, function.getFunctionName());

                    if (scriptPathMap.get(key) == null) {
                        scriptPathMap.put(key, new ArrayList<>());
                    }

                    List<String> list = scriptPathMap.get(key);
                    if (!list.contains(id)) {
                        list.add(id);
                    }
                }
            }
        } else if (scriptIDMap.containsKey(id)) { //if the updated resource is disabled but it was in the list, remove it now
            removeResource(id);
        }
    }

    public void removeResource(String id) {
        ResourceScript resource = scriptIDMap.get(id);
        if (resource != null) {
            String path = resource.target();
            List<ScriptFileResource.RESOURCE_FUNCTION> provides = resource.getScriptFile().provides();
            for (ScriptFileResource.RESOURCE_FUNCTION function : provides) {
                ScriptKey key = new ScriptKey(path, function.getFunctionName());
                List<String> ids = scriptPathMap.get(key);
                if (ids != null) {
                    ids.remove(id);
                }
            }
            scriptIDMap.remove(id);
        }
    }

    public Object executeScripts(ResourceRequest request) {

        ScriptingResourceRequest scriptingRequest = new ScriptingResourceRequest(request);
        String resourcePath = scriptingRequest.resourcePath().toString();
        RequestType type = scriptingRequest.requestType();

        ScriptFileResource.RESOURCE_FUNCTION resourceFunction = ScriptFileResource.RESOURCE_FUNCTION.PREREAD;
        switch (type) {
            case CREATE:
                resourceFunction = ScriptFileResource.RESOURCE_FUNCTION.PRECREATE;
                break;
            case UPDATE:
                resourceFunction = ScriptFileResource.RESOURCE_FUNCTION.PREUPDATE;
                break;
            case DELETE:
                resourceFunction = ScriptFileResource.RESOURCE_FUNCTION.PREDELETE;
                break;
        }

        List<String> paths = generatePaths(resourcePath);

        //special case on CREATE, since its a POST to the parent!
        if (resourceFunction == ScriptFileResource.RESOURCE_FUNCTION.PRECREATE) {
            paths.add(resourcePath + "/*");
        }

        for (String path : paths) {
            ScriptKey key = new ScriptKey(path, resourceFunction.getFunctionName());
            List<String> ids = scriptPathMap.get(key);
            if (ids != null && !ids.isEmpty()) {
                List<ResourceScript> resourceScripts = new ArrayList<>();
                for (String id : ids) {
                    resourceScripts.add(scriptIDMap.get(id));
                }
                // sort the collection in order of priority
                // Note: since we want to run scripts with a higher priority first, we also need to reverse it
                Collections.sort(resourceScripts, new ResourceScript.ReverseComparator());
                for (ResourceScript resourceScript : resourceScripts) {
                    Object reply = runScript(resourceFunction.getFunctionName(), resourceScript, scriptingRequest);
                    if (reply != null) {
                        return reply;
                    }
                }
            }
        }

        return scriptingRequest;
    }

    public Object executeScripts(ResourceResponse response) throws Exception {

        String resourcePath = response.inReplyTo().resourcePath().toString();
        ResourceResponse.ResponseType type = response.responseType();

        ScriptFileResource.RESOURCE_FUNCTION resourceFunction = ScriptFileResource.RESOURCE_FUNCTION.POSTREAD;
        switch (type) {
            case CREATED:
                resourceFunction = ScriptFileResource.RESOURCE_FUNCTION.POSTCREATE;
                break;
            case READ:
                resourceFunction = ScriptFileResource.RESOURCE_FUNCTION.POSTREAD;
                break;
            case UPDATED:
                resourceFunction = ScriptFileResource.RESOURCE_FUNCTION.POSTUPDATE;
                break;
            case DELETED:
                resourceFunction = ScriptFileResource.RESOURCE_FUNCTION.POSTDELETE;
                break;
            case ERROR:
                resourceFunction = ScriptFileResource.RESOURCE_FUNCTION.ONERROR;
                break;
        }

        List<String> paths = generatePaths(resourcePath);

        //special case on CREATE, since its a POST to the parent!
        if (resourceFunction == ScriptFileResource.RESOURCE_FUNCTION.POSTCREATE) {
            paths.add(resourcePath + "/*");
        }

        for (String path : paths) {
            ScriptKey key = new ScriptKey(path, resourceFunction.getFunctionName());
            List<String> ids = scriptPathMap.get(key);
            if (ids != null && !ids.isEmpty()) {
                List<ResourceScript> resourceScripts = new ArrayList<>();
                for (String id : ids) {
                    resourceScripts.add(scriptIDMap.get(id));
                }
                // sort the collection in order of priority
                Collections.sort(resourceScripts);
                for (ResourceScript resourceScript : resourceScripts) {
                    Object reply = runScript(resourceFunction.getFunctionName(), resourceScript, response);
                    if (reply != null) {
                        return reply;
                    }
                }
            }
        }

        return null;
    }

    protected List<String> generatePaths(String uri) {
        List<String> paths = new ArrayList<>();

        ResourcePath resourcePath = new ResourcePath(uri);
        paths.add(resourcePath.toString());
        paths.add(resourcePath.toString() + "*");
        while (!resourcePath.segments().isEmpty()) {
            resourcePath = resourcePath.parent();
            paths.add(resourcePath.toString() + "/*");
            paths.add(resourcePath.toString() + "*");
        }
        return paths;
    }

    protected Object runScript(String functionName, ResourceScript resourceScript, ResourceResponse resourceResponse) {

        Map<String, Object> library = new HashMap<>();

        for (String libraryName : resourceScript.libraries()) {
            library.put(libraryName, libraryManager.getLibrary(libraryName).object());
        }

        Config config = new Config();
        config.setCompileMode(Config.CompileMode.OFF); //TODO: probably shouldn't be needed, check with a newer version of DynJS

        DynJS dynJS = new DynJS(config);

        GlobalObject globalObject = dynJS.getExecutionContext().getGlobalObject();
        configureGlobalObject(globalObject);

        dynJS.evaluate(resourceScript.getScriptFile().getScriptAsString());

        String source = configureFunction(globalObject, functionName, new LiveOakResourceResponse(resourceResponse), library);

        Runner runner = dynJS.newRunner();
        runner.withSource(source);

        Object response = runner.evaluate();
        ScriptingResourceRequest request = new ScriptingResourceRequest(resourceResponse.inReplyTo());
        return handleResponse(response, request);
    }

    protected Object runScript(String functionName, ResourceScript resourceScript, ScriptingResourceRequest resourceRequest) {
        Map<String, Object> library = new HashMap<>();

        for (String libraryName : resourceScript.libraries()) {
            library.put(libraryName, libraryManager.getLibrary(libraryName).object());
        }

        Config config = new Config();
        config.setCompileMode(Config.CompileMode.OFF); //TODO: probably shouldn't be needed, check with a newer version of DynJS

        DynJS dynJS = new DynJS(config);

        GlobalObject globalObject = dynJS.getExecutionContext().getGlobalObject();
        configureGlobalObject(globalObject);

        dynJS.evaluate(resourceScript.getScriptFile().getScriptAsString());

        String source = configureFunction(globalObject, functionName, new LiveOakResourceRequest(resourceRequest), library);

        Runner runner = dynJS.newRunner();
        runner.withSource(source);

        Object response = runner.evaluate();
        return handleResponse(response, resourceRequest);
    }

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
