package org.projectodd.restafari.spi;

import java.util.Collection;

/**
 * Contains info about various request-scoped attributes.
 * For example: In HTTP requests, the attributes could be HTTP headers or other request-scoped informations
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface RequestAttributes {

    Collection<String> getAttributeNames();

    Object getAttribute(String attributeName);

    default <T> T getAttribute(String attributeName, Class<T> expectedClass) {
        Object o = getAttribute(attributeName);
        if (o == null) {
            return null;
        } else if (expectedClass.isAssignableFrom(o.getClass())) {
            return expectedClass.cast(o);
        } else {
            throw new IllegalArgumentException("Object " + o + " is not instance of expected class " + expectedClass);
        }
    }

    void setAttribute(String attributeName, Object attributeValue);

    /**
     * Removes attribute and return it's value if it was present or null if it wasn't
     * @param attributeName
     * @return value of removed attribute or null if attribute wasn't present
     */
    Object removeAttribute(String attributeName);
}
