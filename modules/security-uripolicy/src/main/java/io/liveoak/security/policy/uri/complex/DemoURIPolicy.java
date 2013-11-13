package io.liveoak.security.policy.uri.complex;

/**
 * Demo policy with pre-configured rules for testing purposes
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DemoURIPolicy extends URIPolicy {

    @Override
    protected void doInit() {
        super.doInit();

        // Rule specifies that READ requests to '/droolsTest/foo' are accepted for members of realm roles "role1" and "role2"
        URIPolicyEntry rule1 = URIPolicyEntry.createEntry(8, "/droolsTest/foo", null,
                "READ", "\"role1\", \"role2\"", null, null, null, null, null);

        // Rule specifies that requests to '/droolsTest/*' are accepted if params condition matched as well
        URIPolicyEntry rule2 = URIPolicyEntry.createEntry(9, "/droolsTest/*", "resourceParams.value(\"param1\") == \"foo\" && resourceParams.intValue(\"param2\") >= 10",
                "*" ,null , null, null, null, "\"*\"", null);

        // Rule specifies that requests, which permits everything if URI matches regex and all params condition matched as well
        URIPolicyEntry rule3 = URIPolicyEntry.createEntry(9, "/droolsTest/*/bar/([abc].*)", "resourceParams.value(\"param1\") == $uriMatcher.group(1) && resourceParams.value(\"param2\") == $uriMatcher.group(2) && resourceParams.value(\"param3\") == $token.username",
                "*" ,null , null, null, null, "\"*\"", null);


        // Rule specifies that URI like '/droolsTest/foo' is available for user with username 'foo' (Last part must be username of current user)
        URIPolicyEntry rule4 = URIPolicyEntry.createEntry(10, "/droolsTest/{ $token.username }", null,
                "*", null, null, null, null, "$token.username", null);

        // Rule specifies that URI like '/droolsTest/foo' is available for user, which has realmRole 'foo' (Last part must be some realmRole of current user)
        URIPolicyEntry rule5 = URIPolicyEntry.createEntry(10, "/droolsTest/{ any($token.realmRoles) }", null,
                "*", null, null, null, null, "\"*\"", null);

        // Rule specifies that all requests to '/droolsTest/foo' are accepted for members of realm roles "role3" (similar to rule1, but this is for all requests)
        // NOTE: Read requests to /droolsTest/foo will be preferably processed by rule1 because it has bigger priority
        URIPolicyEntry rule6 = URIPolicyEntry.createEntry(5, "/droolsTest/foo", null,
                "*", null, "\"role1\"", null, null, "\"*\"", null);


        // Killer rule with big priority. Automatically denies all requests if user is member of realm role "evilRole"
        URIPolicyEntry rule7 = URIPolicyEntry.createEntry(20, "/droolsTest/*", null,
                "*", null, "\"evilRole\"", null, null, null, null);

        this.addURIPolicyEntry(rule1);
        this.addURIPolicyEntry(rule2);
        this.addURIPolicyEntry(rule3);
        this.addURIPolicyEntry(rule4);
        this.addURIPolicyEntry(rule5);
        this.addURIPolicyEntry(rule6);
        this.addURIPolicyEntry(rule7);
    }
}
