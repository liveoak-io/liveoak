package io.liveoak.scripts.objects;

import java.util.Iterator;

import javax.naming.OperationNotSupportedException;

import io.liveoak.common.DefaultResourceErrorResponse;
import io.liveoak.scripts.objects.impl.exception.LiveOakCreateNotSupportedException;
import io.liveoak.scripts.objects.impl.exception.LiveOakDeleteNotSupportedException;
import io.liveoak.scripts.objects.impl.exception.LiveOakException;
import io.liveoak.scripts.objects.impl.exception.LiveOakNotAcceptableException;
import io.liveoak.scripts.objects.impl.exception.LiveOakNotAuthorizedException;
import io.liveoak.scripts.objects.impl.exception.LiveOakReadNotSupportedException;
import io.liveoak.scripts.objects.impl.exception.LiveOakResourceAlreadyExistsException;
import io.liveoak.scripts.objects.impl.exception.LiveOakResourceNotFoundException;
import io.liveoak.scripts.objects.impl.exception.LiveOakUpdateNotSupportedException;
import io.liveoak.spi.CreateNotSupportedException;
import io.liveoak.spi.DeleteNotSupportedException;
import io.liveoak.spi.NotAcceptableException;
import io.liveoak.spi.NotAuthorizedException;
import io.liveoak.spi.ReadNotSupportedException;
import io.liveoak.spi.ResourceAlreadyExistsException;
import io.liveoak.spi.ResourceErrorResponse;
import io.liveoak.spi.ResourceNotFoundException;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.UpdateNotSupportedException;
import io.liveoak.spi.ResourceException;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class Util {

    public static Integer getIntValue(Object value, Integer defaultValue) {
        try {
            if (value instanceof Integer) {
                return (Integer)value;
            } else if (value instanceof Long) {
                return ((Long)value).intValue();
            } else if (value instanceof String) {
                return Integer.parseInt((String)value);
            } else {
                return defaultValue;
            }
        } catch (Exception e) {
            // if an exception occurs, use the default value instead
            // TODO: log something here
            return defaultValue;
        }
    }

    public static String generateReturnFieldsString(ReturnFields returnFields) {
        StringBuilder builder = new StringBuilder();
        Iterator<String> fieldIterator = returnFields.iterator();
        while (fieldIterator.hasNext()) {
            String field = fieldIterator.next();
            builder.append(field);
            ReturnFields child = returnFields.child(field);
            if (child.isAll()) {
                builder.append("(*)");
            } else if (!child.isEmpty()) {
                builder.append("(");
                builder.append(generateReturnFieldsString(child));
                builder.append(")");
            }

            if (fieldIterator.hasNext()) {
                builder.append(",");
            }
        }

        return builder.toString();
    }

    public static String generateSortString(io.liveoak.spi.Sorting sorting) {
        StringBuilder builder = new StringBuilder();
        Iterator<io.liveoak.spi.Sorting.Spec> sortIterator = sorting.iterator();
        while (sortIterator.hasNext()) {
            io.liveoak.spi.Sorting.Spec spec = sortIterator.next();
            if (spec.ascending()) {
                builder.append(spec.name());
            } else {
                builder.append("-");
                builder.append(spec.name());
            }

            if (sortIterator.hasNext()) {
                builder.append(",");
            }
        }

        return builder.toString();
    }

    public static ResourceResponse getErrorResponse(ResourceRequest request, LiveOakException exception) {
        String message = exception.getMessage();
        ResourceErrorResponse.ErrorType errorType =  ResourceErrorResponse.ErrorType.INTERNAL_ERROR;
        if (exception instanceof LiveOakResourceAlreadyExistsException) {
            errorType = ResourceErrorResponse.ErrorType.RESOURCE_ALREADY_EXISTS;
        } else if (exception instanceof LiveOakNotAcceptableException) {
            errorType =  ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE;
        } else if (exception instanceof LiveOakUpdateNotSupportedException) {
            errorType =  ResourceErrorResponse.ErrorType.UPDATE_NOT_SUPPORTED;
        }else if (exception instanceof LiveOakReadNotSupportedException) {
            errorType =  ResourceErrorResponse.ErrorType.READ_NOT_SUPPORTED;
        }else if (exception instanceof LiveOakResourceNotFoundException) {
            errorType =  ResourceErrorResponse.ErrorType.NO_SUCH_RESOURCE;
        }else if (exception instanceof LiveOakNotAuthorizedException) {
            errorType =  ResourceErrorResponse.ErrorType.NOT_AUTHORIZED;
        }else if (exception instanceof LiveOakDeleteNotSupportedException) {
            errorType =  ResourceErrorResponse.ErrorType.DELETE_NOT_SUPPORTED;
        }else if (exception instanceof LiveOakCreateNotSupportedException) {
            errorType =  ResourceErrorResponse.ErrorType.CREATE_NOT_SUPPORTED;
        }

        return new DefaultResourceErrorResponse(request, errorType, message);
    }

    public static Exception convertException(ResourceException e) {
        if (e instanceof ResourceAlreadyExistsException) {
            return new LiveOakResourceAlreadyExistsException(e.getMessage());
        } else if (e instanceof ResourceNotFoundException) {
            return new LiveOakResourceNotFoundException(e.getMessage());
        } else if (e instanceof UpdateNotSupportedException) {
            return new LiveOakUpdateNotSupportedException(e.getMessage());
        } else if (e instanceof ReadNotSupportedException) {
            return new LiveOakReadNotSupportedException(e.getMessage());
        } else if (e instanceof NotAcceptableException) {
            return new LiveOakNotAcceptableException(e.getMessage());
        } else if (e instanceof NotAuthorizedException) {
            return new LiveOakNotAuthorizedException(e.getMessage());
        } else if (e instanceof DeleteNotSupportedException) {
            return new LiveOakDeleteNotSupportedException(e.getMessage());
        } else if (e instanceof CreateNotSupportedException) {
            return new LiveOakCreateNotSupportedException(e.getMessage());
        } else {
            return e;
        }
    }

    public static Exception notEditable(String name) {
        return new OperationNotSupportedException(name + " cannot be modified");
    }
}
