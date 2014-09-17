package io.liveoak.scripts.scheduled;

import java.util.Date;
import java.util.List;

import io.liveoak.scripts.common.Function;
import io.liveoak.scripts.common.Script;
import io.netty.buffer.ByteBuf;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScheduledScript extends Script {

    protected String cron;

    protected Date at, until;

    public Date getAt() {
        return at;
    }

    public void setAt(Date at) {
        this.at = at;
    }

    public Date getUntil() {
        return until;
    }

    public void setUntil(Date until) {
        this.until = until;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    protected ScheduledScript(String id, String name, String description, boolean enabled, List<String> libraries, ByteBuf scriptBuffer, Date at, Date until, String cron) {
        super(id, name, description, enabled, libraries, scriptBuffer);
        this.at = at;
        this.until = until;
        this.cron = cron;
    }

    public static enum FUNCTIONS implements Function {
        ONSTART("onStart"),
        EXECUTE("execute"),
        ONEND("onEnd");

        String name;

        private FUNCTIONS(String name) {
            this.name = name;
        };

        @Override
        public String getFunctionName() {
            return name;
        }
    }

    @Override
    protected Function[] getFunctions() {
        return FUNCTIONS.values();
    }

    public static class Builder extends Script.Builder {
        private String cron;
        private Date at, until;

        public Builder(String id) {
            super(id);
        }

        public Builder setAt(Date at) {
            this.at = at;
            return this;
        }

        public Builder setUntil(Date until) {
            this.until = until;
            return this;
        }

        public Builder setCron(String cron) {
            this.cron = cron;
            return this;
        }

        public ScheduledScript build() {
            return new ScheduledScript(id, name, description, enabled, libraries, scriptBuffer, at, until, cron);
        }
    }
}
