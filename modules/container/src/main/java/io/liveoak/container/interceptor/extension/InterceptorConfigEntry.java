package io.liveoak.container.interceptor.extension;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InterceptorConfigEntry {

    @JsonProperty(value = "interceptor-name", required=true)
    private String interceptorName;

    @JsonProperty("resource-path-prefix")
    private String resourcePathMapping;

    @JsonProperty("request-type-mapping")
    private String requestTypeMapping;

    public String getInterceptorName() {
        return interceptorName;
    }

    public void setInterceptorName(String interceptorName) {
        this.interceptorName = interceptorName;
    }

    public String getResourcePathMapping() {
        return resourcePathMapping;
    }

    public void setResourcePathMapping(String resourcePathMapping) {
        this.resourcePathMapping = resourcePathMapping;
    }

    public String getRequestTypeMapping() {
        return requestTypeMapping;
    }

    public void setRequestType(String requestTypeMapping) {
        this.requestTypeMapping = requestTypeMapping;
    }

    @Override
    public String toString() {
        return new StringBuilder("InterceptorConfigEntry [")
                .append("interceptorName=" + interceptorName)
                .append(", resourcePathPrefix=" + resourcePathMapping)
                .append(", requestTypeMapping=" + requestTypeMapping)
                .toString();
    }
}
