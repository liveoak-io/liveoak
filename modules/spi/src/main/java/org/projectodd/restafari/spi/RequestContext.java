package org.projectodd.restafari.spi;


public interface RequestContext {

    Application getApplication();
    SecurityContext getSecurityContext();
    Pagination getPagination();
    ResourcePath getResourcePath();
    ResourceParams getResourceParams();
    RequestAttributes getRequestAttributes();
    RequestType getRequestType();
    ReturnFields getReturnFields();

    public static class Builder implements RequestContext {

        public Builder() {

        }

        public Builder securityContext(SecurityContext securityContext) {
            this.securityContext = securityContext;
            return this;
        }

        public Builder pagination(Pagination pagination) {
            this.pagination = pagination;
            return this;
        }

        public RequestContext build() {
            return this;
        }

        @Override
        public Application getApplication() {
            return null;
        }

        @Override
        public SecurityContext getSecurityContext() {
            return this.securityContext;
        }

        @Override
        public Pagination getPagination() {
            return this.pagination;
        }

        @Override
        public ResourceParams getResourceParams() {
            return null;
        }

        @Override
        public ReturnFields getReturnFields() {
            return null;
        }

        @Override
        public ResourcePath getResourcePath() {
            return null;
        }

        @Override
        public RequestAttributes getRequestAttributes() {
            return null;
        }

        @Override
        public RequestType getRequestType() {
            return null;
        }

        private SecurityContext securityContext;
        private Pagination pagination = Pagination.NONE;
    }
}
