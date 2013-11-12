package org.projectodd.restafari.spi;

/**
 * @author Bob McWhirter
 */
public class ResourceException extends Exception {

    protected ResourceException(String path) {
        this.path = path;
    }

    public ResourceException(String path, String message) {
        super( message );
        this.path = path;
    }

    public ResourceException(String path, Throwable cause) {
        super( cause );
        this.path = path;
    }

    public String path() {
        return this.path;
    }

    private String path;
}
