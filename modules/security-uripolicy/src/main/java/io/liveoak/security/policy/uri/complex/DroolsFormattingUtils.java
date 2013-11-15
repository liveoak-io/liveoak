/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.policy.uri.complex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility to format URI strings from "user-friendly" form to "drools" form, which will be used in drools templates.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DroolsFormattingUtils {

    private static Pattern droolsNormalizationPattern = Pattern.compile("\\{[^{]*\\}");
    private static Pattern wildcardNormalizationPattern = Pattern.compile("\\([^(]*\\)");
    private static Pattern wildcardReplacePattern = Pattern.compile("\\*");

    /**
     * Format pattern from "user-friendly" form to Drools-friendly form. See testsuite for examples
     * NOTE: Maybe it's not the best way to do it, but sufficient for now as this code is not critical for performance...
     * (Executed just when inserting new rule)
     *
     * @param input
     * @return
     */
    public static String formatStringToDrools(String input) {
        String result = formatForDrools(input);
        result = formatWildcards(result);
        return result;
    }

    // Replace string like 'foo/${bar}/baz' with something like '"foo" + $bar + "baz"'
    private static String formatForDrools(String input) {
        String[] innerAr = droolsNormalizationPattern.split(input);
        List<String> outer = new ArrayList<String>();

        Matcher m = droolsNormalizationPattern.matcher(input);
        boolean startWith = false;
        while (m.find()) {
            if (m.start() == 0) {
                startWith = true;
            }
            outer.add(m.group().replace("{", "").replace("}", ""));
        }

        // Move items from Array to List. Remove very first String if it is ""
        List<String> inner = new ArrayList<>();
        for (int i=0 ; i<innerAr.length ; i++) {
            if (i > 0 || !startWith) {
                inner.add(innerAr[i]);
            }
        }

        // Case when whole input starts with {foo-like} prefix
        StringBuilder result = new StringBuilder();
        if (startWith) {
            // Add "^(" to the beginning
            result.append("\"^(\" + ");

            result.append(outer.get(0));

            if (inner.size() > 0) {
                result.append(" + ");
            } else {
                // Case when whole input is just something like {foo-like}
                result.append(" + \")$\"");
            }

            outer.remove(0);
        }

        // Main algorithm
        for (int i=0 ; i<inner.size() ; i++) {
            String currentInner = inner.get(i);
            String currentOuter = null;
            if (outer.size() > i) {
                currentOuter = outer.get(i);
            }

            result.append('"');

            // Case when we had some {foo-like} before this currentInner.
            if (i > 0 || startWith) {
                result.append(')');
            }

            // Add ^ to the beginning of output
            if (i == 0 && !startWith) {
                result.append('^');
            }

            result.append(currentInner);

            // Add $ to the end of output
            if (i == (inner.size() - 1) && currentOuter == null) {
                result.append('$');
            }

            // Case when we will have some additional currentOuter (something like {foo-like} )
            if (currentOuter != null) {
                result.append('(');
            }

            result.append('"');

            if (currentOuter != null) {
                result.append(" + ").append(currentOuter);

                // Add ")$" to the end of output
                if (i == (inner.size() - 1)) {
                    result.append(" + \")$\"");
                }
            }

            if (i != inner.size()-1) {
                result.append(" + ");
            }
        }

        return result.toString();
    }

    // Replace string like "foo/*/bar/(.*)" with something like "foo/(.*)/bar/(.*)" (IE: * is replaced with (.*) but
    // only if it's not already part of regex.)
    private static String formatWildcards(String input) {

        String[] inner = wildcardNormalizationPattern.split(input);

        List<String> outer = new ArrayList<String>();
        Matcher m = wildcardNormalizationPattern.matcher(input);
        while (m.find()) {
            outer.add(m.group());
        }

        StringBuilder result = new StringBuilder();

        for (int i=0 ; i<inner.length ; i++) {
            String currentInner = inner[i];
            String currentOuter = null;
            if (outer.size() > i) {
                currentOuter = outer.get(i);
            }

            result.append(wildcardReplacePattern.matcher(currentInner).replaceAll("(.*)"));
            if (currentOuter != null) {
                result.append(currentOuter);
            }
        }

        return result.toString();
    }
}
