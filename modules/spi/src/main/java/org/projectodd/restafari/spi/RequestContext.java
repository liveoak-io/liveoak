package org.projectodd.restafari.spi;

import java.security.Principal;

public interface RequestContext {

    Application getApplication();
    Principal getPrinicpal();
    Pagination getPagination();
    ResourceParams getResourceParams();
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
