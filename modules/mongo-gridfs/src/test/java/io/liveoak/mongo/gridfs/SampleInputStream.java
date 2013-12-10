/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import java.io.IOException;
import java.io.InputStream;

/**
* @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
*/
class SampleInputStream extends InputStream {

    private int size;
    private int count;

    public SampleInputStream(int size) {
        this.size = size;
    }

    @Override
    public int read() throws IOException {
        if (count == size)
            return -1;

        return '0' + count++ % 10;
    }
}
