package io.liveoak.scripts.resourcetriggered.resource;

import java.util.Comparator;
import java.util.List;

import io.liveoak.scripts.common.Function;
import io.liveoak.scripts.common.Script;
import io.liveoak.spi.PropertyException;
import io.netty.buffer.ByteBuf;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ResourceTriggeredScript extends Script {

    public static final int MAX_PRIORITY = 10;
    public static final int MIN_PRIORITY = 1;

    private String target;
    private int priority;

    public static enum FUNCTIONS implements Function {
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
        };

        @Override
        public String getFunctionName() {
            return name;
        }
    }

    protected ResourceTriggeredScript(String id, String name, String description, boolean enabled, Integer timeout, List<String> libraries, String target, int priority, ByteBuf scriptBuffer) {
        super(id, name, description, enabled, timeout, libraries, scriptBuffer);

        this.target = target;
        this.priority = priority;
    }

    public static class Builder extends Script.Builder {
        private String target;
        private int priority = 5;

        public Builder(String id, String target) {
            super(id);
            this.id = id;
            this.target = target;
        }

        public Builder setPriority(int priority) throws PropertyException {
            if (priority < ResourceTriggeredScript.MIN_PRIORITY || priority > ResourceTriggeredScript.MAX_PRIORITY) {
                throw new PropertyException("'priority' must be between 1 and 10.");
            } else {
                this.priority = priority;
                return this;
            }
        }

        public ResourceTriggeredScript build() {
            return new ResourceTriggeredScript(id, name, description, enabled, timeout, libraries, target, priority, scriptBuffer);
        }
    }

    @Override
    protected Function[] getFunctions() {
        return FUNCTIONS.values();
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public static class PriorityComparator implements Comparator<ResourceTriggeredScript> {
        @Override
        public int compare(ResourceTriggeredScript script1, ResourceTriggeredScript script2) {
            if (script1.getPriority() > script2.getPriority()) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
