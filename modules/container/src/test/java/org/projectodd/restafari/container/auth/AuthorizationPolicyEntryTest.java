package org.projectodd.restafari.container.auth;

import org.junit.Assert;
import org.junit.Test;
import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.container.auth.spi.AuthorizationPolicyEntry;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthorizationPolicyEntryTest {

    @Test
    public void testPath1() {
        AuthorizationPolicyEntry entry1 = new AuthorizationPolicyEntry(null, null);
        entry1.addIncludedResourcePrefix("/").addIncludedResourcePrefix("/foo").addExcludedResourcePrefix("/foo/bar");

        AuthorizationPolicyEntry entry2 = new AuthorizationPolicyEntry(null, null);
        entry2.addIncludedResourcePrefix("/foo");

        AuthorizationPolicyEntry entry3 = new AuthorizationPolicyEntry(null, null);
        entry3.addIncludedResourcePrefix("/foo/bar").addIncludedResourcePrefix("/foo/baz");

        AuthorizationPolicyEntry entry4 = new AuthorizationPolicyEntry(null, null);
        entry4.addIncludedResourcePrefix("/").addExcludedResourcePrefix("/foo");

        AuthorizationPolicyEntry entry5 = new AuthorizationPolicyEntry(null, null);
        entry5.addIncludedResourcePrefix("/").addExcludedResourcePrefix("/foo/bar").addExcludedResourcePrefix("/foo/baz");

        AuthorizationPolicyEntry entry6 = new AuthorizationPolicyEntry(null, null);

        ResourcePath path1 = new ResourcePath("/");
        Assert.assertTrue(entry1.isResourceMapped(path1));
        Assert.assertFalse(entry2.isResourceMapped(path1));
        Assert.assertFalse(entry3.isResourceMapped(path1));
        Assert.assertTrue(entry4.isResourceMapped(path1));
        Assert.assertTrue(entry5.isResourceMapped(path1));
        Assert.assertFalse(entry6.isResourceMapped(path1));

        ResourcePath path2 = new ResourcePath("/foo");
        Assert.assertTrue(entry1.isResourceMapped(path2));
        Assert.assertTrue(entry2.isResourceMapped(path2));
        Assert.assertFalse(entry3.isResourceMapped(path2));
        Assert.assertFalse(entry4.isResourceMapped(path2));
        Assert.assertTrue(entry5.isResourceMapped(path2));
        Assert.assertFalse(entry6.isResourceMapped(path2));

        ResourcePath path3 = new ResourcePath("/foo/bar");
        Assert.assertFalse(entry1.isResourceMapped(path3));
        Assert.assertTrue(entry2.isResourceMapped(path3));
        Assert.assertTrue(entry3.isResourceMapped(path3));
        Assert.assertFalse(entry4.isResourceMapped(path3));
        Assert.assertFalse(entry5.isResourceMapped(path3));
        Assert.assertFalse(entry6.isResourceMapped(path3));

        ResourcePath path4 = new ResourcePath("/foo/bar/baz");
        Assert.assertFalse(entry1.isResourceMapped(path4));
        Assert.assertTrue(entry2.isResourceMapped(path4));
        Assert.assertTrue(entry3.isResourceMapped(path4));
        Assert.assertFalse(entry4.isResourceMapped(path4));
        Assert.assertFalse(entry5.isResourceMapped(path4));
        Assert.assertFalse(entry6.isResourceMapped(path4));

    }
}
