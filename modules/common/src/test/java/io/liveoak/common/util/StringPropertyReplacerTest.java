package io.liveoak.common.util;

import java.util.Properties;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class StringPropertyReplacerTest {

    @Test
    public void testReplaceProperties() throws Exception {

        Properties properties = new Properties();

        String value = StringPropertyReplacer.replaceProperties("${foo:bar}", properties);
        assertThat(value).isEqualTo("bar");

        properties.put("foo", "baz");
        value = StringPropertyReplacer.replaceProperties("${foo:bar}", properties);
        assertThat(value).isEqualTo("baz");

        properties.put("foo:bar", "ABC");
        value = StringPropertyReplacer.replaceProperties("${foo:bar}", properties);
        assertThat(value).isEqualTo("ABC");
    }

    @Test
    public void testPortOffset() throws Exception {
        Properties properties = new Properties();

        String value = StringPropertyReplacer.replaceProperties("${jboss.http.port:8080}", properties);
        assertThat(value).isEqualTo("8080");

        properties.put("jboss.http.port", "9090");
        value = StringPropertyReplacer.replaceProperties("${jboss.http.port:8080}", properties);
        assertThat(value).isEqualTo("9090");

        //check that the port offset get applied
        properties.put("jboss.socket.binding.port-offset", "10");
        value = StringPropertyReplacer.replaceProperties("${jboss.http.port:8080}", properties);
        assertThat(value).isEqualTo("9100");

        //remove the jboss.http.port from the properties. Now the offset should be applied to the default
        properties.remove("jboss.http.port");
        value = StringPropertyReplacer.replaceProperties("${jboss.http.port:8080}", properties);
        assertThat(value).isEqualTo("8090");

        value = StringPropertyReplacer.replaceProperties("${jboss.http.port,foo:8080}", properties);
        assertThat(value).isEqualTo("8090");

        value = StringPropertyReplacer.replaceProperties("${bar,jboss.http.port,foo:8080}", properties);
        assertThat(value).isEqualTo("8090");

        // put in a non-number
        properties.put("jboss.socket.binding.port-offset", "hello world");
        value = StringPropertyReplacer.replaceProperties("${bar,jboss.http.port,foo:8080}", properties);
        assertThat(value).isEqualTo("8080");

        // put in a non-number for the value for the default port
        properties.put("jboss.socket.binding.port-offset", "10");
        value = StringPropertyReplacer.replaceProperties("${jboss.http.port:hello}", properties);
        assertThat(value).isEqualTo("hello");

        // put in a non-number for the value for the default port
        properties.put("jboss.http.port", "world");
        value = StringPropertyReplacer.replaceProperties("${jboss.http.port:8080}", properties);
        assertThat(value).isEqualTo("world");
    }

}
