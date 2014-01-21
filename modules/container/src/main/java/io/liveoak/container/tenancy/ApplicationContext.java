package io.liveoak.container.tenancy;

/**
 * @author Bob McWhirter
 */
public class ApplicationContext extends SimpleResourceRegistry {

    public ApplicationContext(String id) {
        super(id);
    }

    public String toString() {
        return "[ApplicationContext: " + id() + "]";
    }

}
