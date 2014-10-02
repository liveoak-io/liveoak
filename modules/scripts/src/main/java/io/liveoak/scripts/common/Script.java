package io.liveoak.scripts.common;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import io.liveoak.spi.exceptions.PropertyException;
import io.netty.buffer.ByteBuf;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public abstract class Script {

    protected String id, name, description;
    boolean enabled;
    List<String> libraries = new ArrayList<String>();
    ByteBuf scriptBuffer;
    private List<Function> provides = new ArrayList<>();
    Integer timeout;

    protected abstract Function[] getFunctions();

    protected Script(String id, String name, String description, boolean enabled, Integer timeout, List<String> libraries, ByteBuf scriptBuffer) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabled;

        if (libraries != null) {
            this.libraries = libraries;
        }

        this.scriptBuffer = scriptBuffer;
        this.timeout = timeout;

        if (scriptBuffer != null) {
            analyseProvides();
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public List<String> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<String> libraries) {
        this.libraries = libraries;
    }

    public ByteBuf getScriptBuffer() {
        if (scriptBuffer != null) {
            return scriptBuffer.copy();
        } else {
            return null;
        }
    }

    public void setScriptBuffer(ByteBuf scriptBuffer) {
        this.scriptBuffer = scriptBuffer;
        if (scriptBuffer != null) {
            analyseProvides();
        }
    }

    public List<Function> getProvides() {
        return provides;
    }

    public static class Builder {
        protected String id, name, description;
        protected boolean enabled = true;
        protected List<String> libraries;
        protected ByteBuf scriptBuffer;
        protected Integer timeout;

        public Builder(String id) {
            this.id = id;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder setLibraries(List<String> libraries) {
            this.libraries = libraries;
            return this;
        }

        public Builder setScriptBuffer(ByteBuf buffer) {
            this.scriptBuffer = buffer;
            return this;
        }

        public Builder setTimeout(Integer timeout) throws PropertyException {
            if (timeout < 0) {
                throw new PropertyException("'timeout' must be a positive number.");
            } else {
                this.timeout = timeout;
                return this;
            }
        }
    }

    private void analyseProvides() {
        ScriptEngineFactory nsef = new NashornScriptEngineFactory();
        ScriptEngine engine = nsef.getScriptEngine();

        try {
            //Load the script
            String script = getScriptBuffer().toString(Charset.forName("UTF-8"));
            engine.eval(script);

            //clear out the current provides list
            provides.clear();
            //Check if the functions we are monitoring exist or not
            for (Function resourceFunction : getFunctions()) {

                Boolean exists = (Boolean) engine.eval(generateCheck(resourceFunction.getFunctionName()));
                if (exists) {
                    provides.add(resourceFunction);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateCheck(String functionName) {
        String check = "typeof " + functionName + " === 'function' ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE";
        return check;
    }
}
