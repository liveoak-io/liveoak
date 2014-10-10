/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi;

import java.util.LinkedList;
import java.util.List;

import io.liveoak.spi.security.SecurityContext;

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

    void dispose();

    void onDispose(Runnable runnable);

    public static class Builder implements RequestContext {

        private List<Runnable> disposeTasks;

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

        public Builder application(Application application) {
            this.application = application;
            return this;
        }

        public Builder resourcePath(ResourcePath resourcePath) {
            this.resourcePath = resourcePath;
            return this;
        }

        public Builder requestAttributes(RequestAttributes requestAttributes) {
            this.requestAttributes = requestAttributes;
            return this;
        }

        public Builder requestType(RequestType requestType) {
            this.requestType = requestType;
            return this;
        }

        public RequestContext build() {
            return this;
        }

        @Override
        public Application application() {
            return application;
        }

        @Override
        public SecurityContext securityContext() {
            return securityContext;
        }

        @Override
        public Pagination pagination() {
            return pagination;
        }

        @Override
        public ResourceParams resourceParams() {
            return resourceParams;
        }

        @Override
        public ReturnFields returnFields() {
            return this.returnFields;
        }

        @Override
        public ResourcePath resourcePath() {
            return this.resourcePath;
        }

        @Override
        public RequestAttributes requestAttributes() {
            return this.requestAttributes;
        }

        @Override
        public RequestType requestType() {
            return this.requestType;
        }

        @Override
        public Sorting sorting() {
            return sorting;
        }

        @Override
        public void dispose() {
            if (disposeTasks != null) {
                for (Runnable r : disposeTasks) {
                    r.run();
                }
            }
        }

        @Override
        public void onDispose(Runnable runnable) {
            if (disposeTasks == null) {
                disposeTasks = new LinkedList<>();
            }
            disposeTasks.add(runnable);
        }

        private Sorting sorting;
        private SecurityContext securityContext;
        private Pagination pagination = Pagination.NONE;
        private ResourceParams resourceParams = ResourceParams.NONE;
        private ReturnFields returnFields = ReturnFields.ALL;
        private Application application;
        private ResourcePath resourcePath;
        private RequestAttributes requestAttributes;
        private RequestType requestType;
    }
}
