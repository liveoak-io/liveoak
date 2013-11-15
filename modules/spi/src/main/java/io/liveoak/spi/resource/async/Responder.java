/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi.resource.async;

/** Object to report on results of actions performed by Resources.
 *
 * @author Bob McWhirter
 */
public interface Responder {

    /** Report a resource that has been readMember in response to a readMember request.
     *
     * @param resource The resource that has been fetched for reading.
     */
    void resourceRead(Resource resource);

    /** Report a resource that has been created in response to a create (or update) request.
     *
     * @param resource The resource that has been created
     */
    void resourceCreated(Resource resource);

    /** Report a resource that has been deleted in response to a delete request.
     *
     * <p>When reporting a deleted resource, the expectation is that the deleted
     * resource will continue to have state available until fully notifying
     * all clients and interested parties.</p>
     *
     * @param resource The resource that has been deleted.
     */
    void resourceDeleted(Resource resource);

    /** Report a resource that has been updated in response to an update request.
     *
     * @param resource The resource that has been updated.
     */
    void resourceUpdated(Resource resource);

    /** Indicate that a resource does not support create requests.
     *
     * @param resource The resource that does not support creating requests.
     */
    void createNotSupported(Resource resource);

    /** Indicate that a resource does not support readMember requests.
     *
     * @param resource The resource that does not support reading requests.
     */
    void readNotSupported(Resource resource);

    /** Indicate that a resource does not support update requests.
     *
     * @param resource The resource that does not support update requests.
     */
    void updateNotSupported(Resource resource);

    /** Indicate that a resource does not support delete requests.
     *
     * @param resource The resource that does not support delete requests.
     */
    void deleteNotSupported(Resource resource);

    /** Indicate an attempt to manipulate a resource that cannot be found.
     *
     * @param id The identifier that cannot be found.
     */
    void noSuchResource(String id);

    /** Indicate an attempt to create a new resource with an id that already exists.
     *
     * @param id The identifier used in the create.
     */
    void resourceAlreadyExists(String id);

    /** Indicate a temporary error condition that normally should not occur during
     * the processing of this request. Examples include DB unreachable, DB error ...
     *
     * @param message
     */
    void internalError(String message);
}
