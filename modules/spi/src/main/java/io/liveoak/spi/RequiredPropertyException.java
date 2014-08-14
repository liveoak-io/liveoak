package io.liveoak.spi;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class RequiredPropertyException extends InvalidPropertyTypeException {

    public RequiredPropertyException(String name, Class<?> requestedType) {
        super(name, requestedType);
    }

    @Override
    public String getMessage() {
        return "Required property missing. The property named '" + name + "' is required and must be of type " + requestedType.getSimpleName();
    }
}