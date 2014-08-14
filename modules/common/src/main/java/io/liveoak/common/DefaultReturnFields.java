/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

import io.liveoak.spi.ReturnFields;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class DefaultReturnFields implements ReturnFields {

    private HashMap<String, DefaultReturnFields> fields = new LinkedHashMap<>();

    private static enum XpctState {
        xpctIdentCommaOpen,
        xpctIdent,
        xpctComma,
        xpctAnything
    }

    private static enum FieldState {
        start,
        name,
        end
    }

    private DefaultReturnFields() {
    }

    public DefaultReturnFields(String spec) {

        if (spec == null || spec.trim().length() == 0) {
            throw new IllegalArgumentException("Fields spec is null or empty!");
        }
        // parse the spec, building up the tree for nested children
        char[] buf = spec.toCharArray();
        StringBuilder token = new StringBuilder(buf.length);

        // stack for handling depth
        LinkedList<HashMap<String, DefaultReturnFields>> specs = new LinkedList<>();
        specs.add(fields);

        // parser state
        FieldState fldState = FieldState.start;
        XpctState state = XpctState.xpctIdent;

        int i;
        for (i = 0; i < buf.length; i++) {
            char c = buf[i];

            if (c == ',') {
                if (state == XpctState.xpctIdent) {
                    error(spec, i);
                }
                if (fldState == FieldState.name) {
                    specs.getLast().put(token.toString(), null);
                    token.setLength(0);
                }
                state = XpctState.xpctIdent;
                fldState = FieldState.start;
            } else if (c == '(') {
                if (state != XpctState.xpctIdentCommaOpen && state != XpctState.xpctAnything) {
                    error(spec, i);
                }
                DefaultReturnFields sub = new DefaultReturnFields();
                specs.getLast().put(token.toString(), sub);
                specs.add(sub.fields);
                token.setLength(0);

                state = XpctState.xpctIdent;
                fldState = FieldState.start;
            } else if (c == ')') {
                if (state != XpctState.xpctAnything) {
                    error(spec, i);
                }
                if (fldState == FieldState.name) {
                    specs.getLast().put(token.toString(), null);
                    token.setLength(0);

                }
                specs.removeLast();

                fldState = FieldState.end;
                state = specs.size() > 1 ? XpctState.xpctAnything : XpctState.xpctComma;
            } else {
                token.append(c);
                if (fldState == FieldState.start) {
                    fldState = FieldState.name;
                    state = specs.size() > 1 ? XpctState.xpctAnything : XpctState.xpctIdentCommaOpen;
                }
            }
        }

        if (specs.size() > 1) {
            error(spec, i);
        }

        if (token.length() > 0) {
            specs.getLast().put(token.toString(), null);
        } else if (!(state == XpctState.xpctAnything || state == XpctState.xpctComma)) {
            error(spec, i);
        }
    }

    private void error(String spec, int i) {
        throw new RuntimeException("Invalid fields specification at position " + i + ": " + spec);
    }

    @Override
    public ReturnFields child(String field) {
        ReturnFields returnFields = fields.get(field);
        if (returnFields == null) {
            returnFields = fields.get("*");
            if (returnFields == null) {
                returnFields = ReturnFields.NONE;
            }
        }
        return returnFields;
    }

    @Override
    public boolean included(String... pathSegments) {

        if (pathSegments == null || pathSegments.length == 0) {
            throw new IllegalArgumentException("No path specified!");
        }
        DefaultReturnFields current = this;

        for (String path : pathSegments) {
            if (current == null) {
                return false;
            }
            if (current.fields.containsKey("*")) {
                return true;
            }
            if (!current.fields.containsKey(path)) {
                return false;
            }
            current = (DefaultReturnFields) current.fields.get(path);
        }
        return true;
    }

    @Override
    public Iterator<String> iterator() {
        return fields.keySet().iterator();
    }

    @Override
    public boolean isEmpty() {
        return this.fields.isEmpty();
    }

    @Override
    public boolean isAll() {
        return this.fields.keySet().contains("*");
    }

    @Override
    public String toString() {
        return "[ReturnFieldsImpl: fields=" + this.fields + "]";
    }

    public DefaultReturnFields withExpand(String spec) {
        StringTokenizer expandFields = new StringTokenizer(spec);

        DefaultReturnFields merged = new DefaultReturnFields();
        merged.fields.putAll(this.fields);

        while (expandFields.hasMoreTokens()) {
            String expandField = expandFields.nextToken();
            merged.fields.put(expandField, new DefaultReturnFields("*"));
        }

        return merged;
    }
}
