package io.liveoak.scripts.common;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import io.liveoak.common.DefaultResourceErrorResponse;
import io.liveoak.scripts.libraries.manager.LibraryManager;
import io.liveoak.scripts.objects.impl.LiveOakResource;
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
import io.liveoak.spi.ResourceErrorResponse;
import org.dynjs.Config;
import org.dynjs.runtime.DynJS;
import org.dynjs.runtime.GlobalObject;
import org.dynjs.runtime.Runner;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScriptManager {

    LibraryManager libraryManager;

    public ScriptManager(LibraryManager libraryManager) {
        this.libraryManager = libraryManager;
    }

    protected Object getLibrary(Script script) {

        Map<String, Object> library = new HashMap<>();

        for (String libraryName : script.getLibraries()) {
            library.put(libraryName, libraryManager.getLibrary(libraryName).object());
        }
        return library;
    }

    protected Object runScript(String functionName, Script script, Object... functionArguments) {
        Config config = new Config();
        config.setCompileMode(Config.CompileMode.OFF); //TODO: probably shouldn't be needed, check with a newer version of DynJS

        DynJS dynJS = new DynJS(config);

        GlobalObject globalObject = dynJS.getDefaultExecutionContext().getGlobalObject();
        configureGlobalObject(globalObject);

        dynJS.evaluate(script.getScriptBuffer().toString(Charset.forName("UTF-8")));

        String source = configureFunction(globalObject, functionName, functionArguments);
        Runner runner = dynJS.newRunner();
        runner.withSource(source);

        return runner.evaluate();
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
