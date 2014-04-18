package io.liveoak.common.util;

import io.liveoak.spi.ResourcePath;

import java.util.*;
import java.util.stream.Stream;

/**
 * Each tree stores some objects and also can have some child trees. Saving and retrieving objects is based on {@link ResourcePath}
 *
 * @author Bob McWhirter
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ObjectsTree<T> {

    public void addObject(T object, ResourcePath resourcePath) {
        ObjectsTree<T> leaf = findLeaf(resourcePath);
        leaf.objects.add(object);
    }

    public void removeObject(T object, ResourcePath resourcePath) {
        ObjectsTree<T> leaf = findLeaf(resourcePath);
        leaf.objects.remove(object);
    }

    public ObjectsTree<T> findLeaf(ResourcePath path) {
        if (path.isEmpty()) {
            return this;
        }
        String id = path.head().name();

        ObjectsTree<T> child = this.children.get(id);
        if (child == null) {
            child = new ObjectsTree<>();
            this.children.put(path.head().name(), child);
        }

        // For now, support * just at the end
        if (id.equals("*") && !path.subPath().isEmpty()) {
            throw new IllegalArgumentException("* supported just at the end. Remaining path is " + path.subPath());
        }
        return child.findLeaf(path.subPath());
    }

    public Stream<T> objects(ResourcePath path) {
        if (path.isEmpty()) {
            return this.objects.stream();
        }

        String name = path.head().name();

        ObjectsTree<T> wildcardChild = this.children.get("*");
        Stream<T> wildcardSubs = null;
        if ( wildcardChild != null ) {
            wildcardSubs = wildcardChild.objects.stream();
        }

        ObjectsTree<T> child = this.children.get(name);
        Stream<T> childSubs = null;

        if (child != null) {
            childSubs = child.objects(path.subPath());
        }

        if ( wildcardSubs != null && childSubs != null ) {
            return Stream.concat( wildcardSubs, childSubs );
        }

        if ( wildcardSubs != null ) {
            return wildcardSubs;
        }

        if ( childSubs != null ) {
            return childSubs;
        }

        List<T> empty = Collections.emptyList();
        return empty.stream();
    }

    public Stream<T> objects() {
        return Stream.concat(this.objects.stream(),
                this.children.values().stream().flatMap((e) -> {
                    return e.objects();
                }));
    }

    private Map<String, ObjectsTree<T>> children = new HashMap<>();
    private List<T> objects = new ArrayList<>();

}
