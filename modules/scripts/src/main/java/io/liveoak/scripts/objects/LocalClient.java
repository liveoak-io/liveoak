package io.liveoak.scripts.objects;

import java.util.Map;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public interface LocalClient {

    Resource create(String path, Resource resource) throws Exception;

    Resource read(String path) throws Exception;

    Resource update(String path, Resource resource) throws Exception;

    Resource remove(String path) throws Exception;

    Resource create(String path, Resource resource, Map<String, Object> parameters) throws Exception;

    Resource read(String path, Map<String, Object> parameters) throws Exception;

    Resource update(String path, Resource resource, Map<String, Object> parameters) throws Exception;

    Resource remove(String path, Map<String, Object> parameters) throws Exception;

}
