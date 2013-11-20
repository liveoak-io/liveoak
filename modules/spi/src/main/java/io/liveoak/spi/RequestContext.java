/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi;


public interface RequestContext {

    Application getApplication();

    SecurityContext getSecurityContext();

    Pagination getPagination();

    ResourcePath getResourcePath();

    ResourceParams getResourceParams();

    RequestAttributes getRequestAttributes();

    RequestType getRequestType();

    ReturnFields getReturnFields();

    Sorting getSorting();

    public static class Builder implements RequestContext {

        public Builder() {

        }

        public Builder securityContext( SecurityContext securityContext ) {
            this.securityContext = securityContext;
            return this;
        }

        public Builder pagination( Pagination pagination ) {
            this.pagination = pagination;
            return this;
        }

        public Builder resourceParams( ResourceParams resourceParams ) {
            this.resourceParams = resourceParams;
            return this;
        }

        public Builder returnFields( ReturnFields returnFields ) {
            this.returnFields = returnFields;
            return this;
        }

        public Builder sorting( Sorting sorting ) {
            this.sorting = sorting;
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
            return this.resourceParams;
        }

        @Override
        public ReturnFields getReturnFields() {
            return returnFields;
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

        @Override
        public Sorting getSorting() {
            return sorting;
        }

        private Sorting sorting;
        private SecurityContext securityContext;
        private Pagination pagination = Pagination.NONE;
        private ResourceParams resourceParams = ResourceParams.NONE;
        private ReturnFields returnFields = ReturnFields.ALL;
    }
}
