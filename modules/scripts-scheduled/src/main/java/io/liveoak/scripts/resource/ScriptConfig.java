package io.liveoak.scripts.resource;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScriptConfig {

    // The default timeout in milliseconds.
    public static final Integer DEFAULT_TIMEOUT = 5000;

    private String scriptDirectory;
    private Integer timeout;

    private ScriptConfig(String scriptDirectory, Integer timeout) {
        this.scriptDirectory = scriptDirectory;

        if (timeout == null) {
            this.timeout = DEFAULT_TIMEOUT;
        } else {
            this.timeout = timeout;
        }
    }

    public String getScriptDirectory() {
        return this.scriptDirectory;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public static class Builder {

        private String scriptDirectory;
        private Integer timeout = null;

        public Builder(String scriptDirectory) {
            this.scriptDirectory = scriptDirectory;
        }

        public Builder setTimeout(Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        public ScriptConfig build() {
            return new ScriptConfig(scriptDirectory, timeout);
        }
    }
}
