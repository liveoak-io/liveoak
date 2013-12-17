/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.impl;

import java.util.Arrays;

import io.liveoak.security.spi.AuthzPolicyEntry;
import io.liveoak.spi.ResourcePath;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthzPolicyEntryTest {

    @Test
    public void testPath1() {
        AuthzPolicyEntry entry1 = new AuthzPolicyEntry();
        entry1.setIncludedResourcePrefixes(Arrays.asList(new String[] { "/", "/foo" }));
        entry1.setExcludedResourcePrefixes(Arrays.asList(new String[] { "/foo/bar" }));

        AuthzPolicyEntry entry2 = new AuthzPolicyEntry();
        entry2.setIncludedResourcePrefixes(Arrays.asList(new String[] { "/foo" }));

        AuthzPolicyEntry entry3 = new AuthzPolicyEntry();
        entry3.setIncludedResourcePrefixes(Arrays.asList(new String[] { "/foo/bar", "/foo/baz" }));

        AuthzPolicyEntry entry4 = new AuthzPolicyEntry();
        entry4.setIncludedResourcePrefixes(Arrays.asList(new String[] { "/" }));
        entry4.setExcludedResourcePrefixes(Arrays.asList(new String[] { "/foo" }));

        AuthzPolicyEntry entry5 = new AuthzPolicyEntry();
        entry5.setIncludedResourcePrefixes(Arrays.asList(new String[] { "/" }));
        entry5.setExcludedResourcePrefixes(Arrays.asList(new String[] { "/foo/bar", "/foo/baz" }));

        AuthzPolicyEntry entry6 = new AuthzPolicyEntry();

        ResourcePath path1 = new ResourcePath("/");
        Assert.assertTrue(entry1.isResourceMapped(path1));
        Assert.assertFalse(entry2.isResourceMapped(path1));
        Assert.assertFalse(entry3.isResourceMapped(path1));
        Assert.assertTrue(entry4.isResourceMapped(path1));
        Assert.assertTrue(entry5.isResourceMapped(path1));
        Assert.assertTrue(entry6.isResourceMapped(path1));

        ResourcePath path2 = new ResourcePath("/foo");
        Assert.assertTrue(entry1.isResourceMapped(path2));
        Assert.assertTrue(entry2.isResourceMapped(path2));
        Assert.assertFalse(entry3.isResourceMapped(path2));
        Assert.assertFalse(entry4.isResourceMapped(path2));
        Assert.assertTrue(entry5.isResourceMapped(path2));
        Assert.assertTrue(entry6.isResourceMapped(path2));

        ResourcePath path3 = new ResourcePath("/foo/bar");
        Assert.assertFalse(entry1.isResourceMapped(path3));
        Assert.assertTrue(entry2.isResourceMapped(path3));
        Assert.assertTrue(entry3.isResourceMapped(path3));
        Assert.assertFalse(entry4.isResourceMapped(path3));
        Assert.assertFalse(entry5.isResourceMapped(path3));
        Assert.assertTrue(entry6.isResourceMapped(path3));

        ResourcePath path4 = new ResourcePath("/foo/bar/baz");
        Assert.assertFalse(entry1.isResourceMapped(path4));
        Assert.assertTrue(entry2.isResourceMapped(path4));
        Assert.assertTrue(entry3.isResourceMapped(path4));
        Assert.assertFalse(entry4.isResourceMapped(path4));
        Assert.assertFalse(entry5.isResourceMapped(path4));
        Assert.assertTrue(entry6.isResourceMapped(path4));

    }
}
