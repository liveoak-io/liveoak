/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi;


public interface RequestContext {

    Application application();

    SecurityContext securityContext();

    Pagination pagination();

    ResourcePath resourcePath();

    ResourceParams resourceParams();

    RequestAttributes requestAttributes();

    RequestType requestType();

    ReturnFields returnFields();

    Sorting sorting();

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

        public Builder resourceParams(ResourceParams resourceParams) {
            this.resourceParams = resourceParams;
            return this;
        }

        public Builder returnFields(ReturnFields returnFields) {
            this.returnFields = returnFields;
            return this;
        }

        public Builder sorting(Sorting sorting) {
            this.sorting = sorting;
            return this;
        }

        public RequestContext build() {
            return this;
        }

        @Override
        public Application application() {
            return null;
        }

        @Override
        public SecurityContext securityContext() {
            return this.securityContext;
        }

        @Override
        public Pagination pagination() {
            return this.pagination;
        }

        @Override
        public ResourceParams resourceParams() {
            return this.resourceParams;
        }

        @Override
        public ReturnFields returnFields() {
            return returnFields;
        }

        @Override
        public ResourcePath resourcePath() {
            return null;
        }

        @Override
        public RequestAttributes requestAttributes() {
            return null;
        }

        @Override
        public RequestType requestType() {
            return null;
        }

        @Override
        public Sorting sorting() {
            return sorting;
        }

        private Sorting sorting;
        private SecurityContext securityContext;
        private Pagination pagination = Pagination.NONE;
        private ResourceParams resourceParams = ResourceParams.NONE;
        private ReturnFields returnFields = ReturnFields.ALL;
    }
}
