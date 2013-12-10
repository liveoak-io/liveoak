/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import java.io.IOException;
import java.io.OutputStream;

/**
* @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
*/
class CountOutputStream extends OutputStream {

    private int count;

    @Override
    public void write(int b) throws IOException {
        count++;
    }

    public int getCount() {
        return count;
    }
}
