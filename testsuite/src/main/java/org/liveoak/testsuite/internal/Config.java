package org.liveoak.testsuite.internal;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class Config {

    public static String baseUrl() {
        String url = System.getProperty("liveoak.url", "http://localhost:8080");
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        } else {
            return url;
        }
    }

    public static boolean remote() {
        return Boolean.parseBoolean(System.getProperty("liveoak.remote", "false"));
    }

    public static boolean showOutput() {
        return Boolean.parseBoolean(System.getProperty("liveoak.showOutput", "false"));
    }

    public static long startTimeout() {
        return Long.parseLong(System.getProperty("liveoak.startTimeout", "30000"));
    }

    public static long stopTimeout() {
        return Long.parseLong(System.getProperty("liveoak.stopTimeout", "10000"));
    }

}
