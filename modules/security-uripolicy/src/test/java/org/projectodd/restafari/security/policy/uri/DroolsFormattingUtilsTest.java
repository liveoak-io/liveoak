package org.projectodd.restafari.security.policy.uri;

import org.junit.Assert;
import org.junit.Test;
import org.projectodd.restafari.security.policy.uri.complex.DroolsFormattingUtils;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DroolsFormattingUtilsTest {

    @Test
    public void testDroolsFormattingUtils() {
        Assert.assertEquals(DroolsFormattingUtils.formatStringToDrools("/something/foo"), "\"^/something/foo$\"");
        Assert.assertEquals(DroolsFormattingUtils.formatStringToDrools("/something/*"), "\"^/something/(.*)$\"");
        Assert.assertEquals(DroolsFormattingUtils.formatStringToDrools("/something1/*/kokos/*"), "\"^/something1/(.*)/kokos/(.*)$\"");
        Assert.assertEquals(DroolsFormattingUtils.formatStringToDrools("/something1/([abc].*)/part2/*"), "\"^/something1/([abc].*)/part2/(.*)$\"");
        Assert.assertEquals(DroolsFormattingUtils.formatStringToDrools("*/something1/([abc].*)/part2/(.*)"), "\"^(.*)/something1/([abc].*)/part2/(.*)$\"");
        Assert.assertEquals(DroolsFormattingUtils.formatStringToDrools("(.*)/something1/([abc].*)/part2/(.*)"), "\"^(.*)/something1/([abc].*)/part2/(.*)$\"");
        Assert.assertEquals(DroolsFormattingUtils.formatStringToDrools("/something1/{$token.username}/foo"), "\"^/something1/(\" + $token.username + \")/foo$\"");
        Assert.assertEquals(DroolsFormattingUtils.formatStringToDrools("/something1/{$token.username}"), "\"^/something1/(\" + $token.username + \")$\"");
        Assert.assertEquals(DroolsFormattingUtils.formatStringToDrools("{$token.app}/something1/{$token.username}"), "\"^(\" + $token.app + \")/something1/(\" + $token.username + \")$\"");
        Assert.assertEquals(DroolsFormattingUtils.formatStringToDrools("{$token.app}/something1"), "\"^(\" + $token.app + \")/something1$\"");
        Assert.assertEquals(DroolsFormattingUtils.formatStringToDrools("{$token.app}"), "\"^(\" + $token.app + \")$\"");
        Assert.assertEquals(DroolsFormattingUtils.formatStringToDrools("/something1/{ any($token.realmRoles)}/foo"), "\"^/something1/(\" +  any($token.realmRoles) + \")/foo$\"");
        Assert.assertEquals(DroolsFormattingUtils.formatStringToDrools("/something1/{ any($token.applicationRoles)}"), "\"^/something1/(\" +  any($token.applicationRoles) + \")$\"");
        Assert.assertEquals(DroolsFormattingUtils.formatStringToDrools("/something1/{$token.username}/foo/*/bar/(.*)"), "\"^/something1/(\" + $token.username + \")/foo/(.*)/bar/(.*)$\"");
    }
}
