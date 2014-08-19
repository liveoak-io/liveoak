/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common.codec.form;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.codec.ResourceDecoder;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Aslak Knutsen
 */
public class FormURLDecoder implements ResourceDecoder {

    private static final Charset ENCODING = StandardCharsets.UTF_8;

    @Override
    public ResourceState decode(ByteBuf resource) throws IOException {
        return parse(resource);
    }

    private ResourceState parse(ByteBuf resource) throws IOException {
        DefaultResourceState state = new DefaultResourceState();

        String content = resource.toString(ENCODING);
        String[] pairs = content.split("&");
        if(pairs.length > 0) {
            for(String pair : pairs) {
                String[] values = pair.split("=");
                if(values.length >= 1) {
                    String name = URLDecoder.decode(values[0], ENCODING.name());
                    if(values.length == 1) {
                        if( name.isEmpty() ) {
                            continue;
                        }
                        state.putProperty(name, null);
                    }
                    else {
                        String value = URLDecoder.decode(values[1], ENCODING.name());
                        Object typed = value;
                        if("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                            typed = Boolean.parseBoolean(value);
                        } else if("null".equalsIgnoreCase(value)) {
                            typed = null;
                        } else {
                            try {
                                typed = Integer.parseInt(value);
                            } catch(NumberFormatException e) {
                                try {
                                    typed = Double.parseDouble(value);
                                } catch(NumberFormatException e2) {
                                }
                            }
                        }
                        state.putProperty(name, typed);
                    }
                }
            }
        }
        return state;
    }
}
