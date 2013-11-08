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

        public Builder principal(Principal principal) {
            this.principal = principal;
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
        public Principal getPrinicpal() {
            return this.principal;
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

        private Principal principal;
        private Pagination pagination = Pagination.NONE;
    }
}
