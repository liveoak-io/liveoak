package org.liveoak.testsuite.internal;

import org.liveoak.testsuite.annotations.Page;
import org.liveoak.testsuite.annotations.Resource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class BasicInjector {

    private Map<Class<?>, Object> resources = new HashMap<>();

    public void addResource(Class<?> clazz, Object o) {
        resources.put(clazz, o);
    }

    public void init(Object o) {
        Class<?> c = o.getClass();
        while (c != null) {
            for (Field f : c.getDeclaredFields()) {
                if (f.getAnnotation(Resource.class) != null) {
                    Class<?> type = f.getType();
                    Object resource = resources.get(type);

                    if (resource == null) {
                        throw new RuntimeException("Unsupported type " + f.getType());
                    }

                    if (resource instanceof LazyResource) {
                        resource = ((LazyResource) resource).get();
                    }

                    set(f, o, resource);
                } else if (f.getAnnotation(Page.class) != null) {
                    set(f, o, getPage(f.getType()));
                }
            }

            c = c.getSuperclass();
        }
    }

    protected void set(Field f, Object o, Object v) {
        f.setAccessible(true);
        try {
            f.set(o, v);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T getPage(Class<T> pageClass) {
        try {
            T instance = pageClass.newInstance();
            init(instance);

            WebDriver driver = (WebDriver) resources.get(WebDriver.class);
            PageFactory.initElements(driver, instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<Class<?>, Object> getResources() {
        return resources;
    }

}
