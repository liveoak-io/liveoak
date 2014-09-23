package io.liveoak.scripts.resourcetriggered.resource;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import io.netty.buffer.ByteBuf;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class Script{

    private String id;              //required
    private String name;            //optional
    private String description;     //optional
    private boolean enabled = true; //enabled by default
    private List<String> libraries; //optional
    private String target;          //required
    private int priority = 1;       //'1' by default

    private ByteBuf scriptBuffer;   // the buffer which contains the script itself

    private List<FUNCTIONS> provides; //List of functions the script provides.

    public static enum FUNCTIONS {
        PRECREATE("preCreate"),
        POSTCREATE("postCreate"),
        PREREAD("preRead"),
        POSTREAD("postRead"),
        PREUPDATE("preUpdate"),
        POSTUPDATE("postUpdate"),
        PREDELETE("preDelete"),
        POSTDELETE("postDelete"),
        ONERROR("onError");

        String name;

        private FUNCTIONS(String name) {
            this.name = name;
        }

        public String getFunctionName() {
            return this.name;
        }
    }

    protected Script(String id, String name, String description, boolean enabled, List<String> libraries, String target, int priority, ByteBuf scriptBuffer) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabled;

        if (libraries == null) {
            this.libraries = new ArrayList<>();
        } else {
            this.libraries = libraries;
        }

        this.target = target;
        this.priority = priority;
        this.scriptBuffer = scriptBuffer;
        this.provides = new ArrayList<>();
    }

    public static class Builder {
        private String id, name, description, target;
        private boolean enabled = true;
        private List<String> libraries;
        private int priority = 5;
        private ByteBuf scriptBuffer;

        public Builder(String id, String target) {
            this.id = id;
            this.target = target;
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

        public Builder setScriptBuffer(ByteBuf scriptBuffer) {
            this.scriptBuffer = scriptBuffer;
            return this;
        }

        public Builder setPriority(int priority) {
            this.priority = priority;
            return this;
        }

        public Script build() {
            return new Script(id, name, description, enabled, libraries, target, priority, scriptBuffer);
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

    public List<String> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<String> libraries) {
        this.libraries = libraries;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public ByteBuf getScriptBuffer() {
        if (scriptBuffer != null) {
            return scriptBuffer.copy();
        } else {
            return null;
        }
    }

    //TODO: remove ?
    public String getScriptBufferAsString() {
        if (getScriptBuffer() != null) {
            return getScriptBuffer().toString(Charset.forName("UTF-8"));
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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public List<FUNCTIONS> getProvides() {
        return this.provides;
    }

    private void analyseProvides() {
        ScriptEngineFactory nsef = new NashornScriptEngineFactory();
        ScriptEngine engine = nsef.getScriptEngine();

        try {
            //Load the script
            String script = getScriptBufferAsString();
            engine.eval(script);

            //Check if the functions we are monitoring exist or not
            for (FUNCTIONS resourceFunction : FUNCTIONS.values()) {

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

    public static class PriorityComparator implements Comparator<Script> {
        @Override
        public int compare(Script script1, Script script2) {
            if (script1.getPriority() > script2.getPriority()) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
