package io.liveoak.scripts.libraries.manager;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class Library {

    String name;
    String description;
    Object object;

    public Library(String name, String description, Object library) {
        this.name = name;
        this.description = description;
        this.object = library;
    }

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }

    public String description() {
        return description;
    }

    public void description(String description) {
        this.description = description;
    }

    public Object object() {
        return object;
    }

    public void object(Object library) {
        this.object = library;
    }
}
