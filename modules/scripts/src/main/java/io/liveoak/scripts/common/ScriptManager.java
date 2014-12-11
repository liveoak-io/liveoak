package io.liveoak.scripts.common;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import io.liveoak.scripts.resource.ScriptConfig;
import io.liveoak.spi.ResourceErrorResponse;
import org.dynjs.Config;
import org.dynjs.exception.ThrowException;
import org.dynjs.runtime.DynJS;
import org.dynjs.runtime.GlobalObject;
import org.dynjs.runtime.Runner;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScriptManager {

    LibraryManager libraryManager;
    ScriptConfig scriptConfig;

    protected static final Logger log = Logger.getLogger("io.liveoak.scripts");

    public ScriptManager(ScriptConfig scriptConfig, LibraryManager libraryManager) {
        this.libraryManager = libraryManager;
        this.scriptConfig = scriptConfig;
    }

    protected Object getLibrary(Script script) {

        Map<String, Object> library = new HashMap<>();

        for (String libraryName : script.getLibraries()) {
            library.put(libraryName, libraryManager.getLibrary(libraryName).object());
        }
        return library;
    }

    protected Object runScript(String functionName, Script script, Object... functionArguments) throws Exception {
        CompletableFuture<Object> future = new CompletableFuture<>();

        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
               try {
                   future.complete(executeScript(functionName, script, functionArguments));
                } catch (Exception e) {
                   e.printStackTrace();
                   future.complete(e);
                }
            }
        });

        myThread.start();

        try {
            Integer timeout = script.timeout;
            if (timeout == null) {
                //TODO: remove this once the script config is a separate service and not part of the main root resource
                if (scriptConfig != null) {
                    timeout = scriptConfig.getTimeout();
                } else {
                    timeout = ScriptConfig.DEFAULT_TIMEOUT;
                }
            }
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return e;
        } catch (ExecutionException e) {
            if (e.getCause() != null && e.getCause() instanceof LiveOakException) {
                return e.getCause();
            } else {
                if (e.getCause() != null && e.getCause() instanceof Exception) {
                    throw (Exception) e.getCause();
                }
                else {
                    throw e;
                }
            }
        } catch (TimeoutException e) {
            // The script ran too long and we need to stop it.
            // First try to interrupt the thread, then, if that doesn't
            // work after another timeout, go nuclear and stop the thread itself.
            future.cancel(true);
            myThread.interrupt();
            log.error("A script went over the timeout. Interrupting the thread.");
            Long startTime = System.currentTimeMillis();
            while (myThread.isAlive()) {
                if (System.currentTimeMillis() - startTime >= 5000 ) {
                    log.error("A resource based script did not terminate after the timeout. Killing the thread.");
                    myThread.stop();
                    break;
                } else {
                    Thread.sleep(10);
                }
            }

            return e;
        }
    }

    protected Object executeScript(String functionName, Script script, Object... functionArguments) {
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
        Map<String, Object> liveoakMap = new HashMap<String, Object>();
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

        liveoakMap.put("Client", libraryManager.getLibrary("client").object());

        globalObject.put("liveoak", liveoakMap);
    }

    protected Object handleResponse(Object response, ScriptingResourceRequest request) {

        if (response instanceof ThrowException) {
            Object value = ((ThrowException)response).getValue();
            if (value instanceof LiveOakException) {
                response = value;
            }
        }

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
        } else if (response instanceof TimeoutException) {
            ResourceErrorResponse resourceErrorResponse = new DefaultResourceErrorResponse(request, ResourceErrorResponse.ErrorType.INTERNAL_ERROR , "A timeout occurred when running the script.");
            return resourceErrorResponse;
        } else if (response instanceof Exception) {
            ResourceErrorResponse resourceErrorResponse = new DefaultResourceErrorResponse(request, ResourceErrorResponse.ErrorType.INTERNAL_ERROR , "An error occurred while running the script.");
            return resourceErrorResponse;
        }
        return null;
    }
}
