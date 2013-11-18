/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class AssertionHelper {

    public static interface AssertionBlock {
        public void doAssertions() throws Throwable;
    }

    public void assertThat( AssertionBlock b ) {
        try {
            b.doAssertions();
        } catch ( Throwable e ) {
            System.err.println( "caught: " + e );
            errors.add( e );
        }
    }

    public void complete() throws Throwable {
        if ( errors.isEmpty() ) {
            return;
        }

        if ( errors.get( 0 ) instanceof AssertionError ) {
            throw errors.get( 0 );
        }

        throw new AssertionError( errors.get( 0 ) );

    }

    private List<Throwable> errors = new ArrayList<>();
}
