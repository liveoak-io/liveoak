package org.liveoak.testsuite.internal;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface LazyResource<T> extends Closeable {

    public T get();

    public void close() throws IOException;

}
