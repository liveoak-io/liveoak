package io.liveoak.scripts.resourcetriggered.resource;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import io.liveoak.spi.MediaType;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.BinaryContentSink;
import io.liveoak.spi.resource.async.BinaryResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScriptFileResource implements BinaryResource {

    static final String ID = "script";

    protected ResourceScript parent;
    protected ByteBuf buffer;

    public static enum RESOURCE_FUNCTION {
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

        private RESOURCE_FUNCTION(String name) {
            this.name = name;
        }

        public String getFunctionName() {
            return this.name;
        }
    }

    protected List<RESOURCE_FUNCTION> provides = new ArrayList<>();

    public ScriptFileResource(ResourceScript parent, ByteBuf buffer) {
        this.parent = parent;
        this.buffer = buffer;

        analyseProvides(getScriptAsString());
    }

    @Override
    public MediaType mediaType() {
        return MediaType.JAVASCRIPT;
    }

    @Override
    public long contentLength() {
        return buffer.readableBytes();
    }

    @Override
    public void readContent(RequestContext ctx, BinaryContentSink sink) throws Exception {
        sink.accept(buffer.copy());
        sink.close();
    }

    public String getScriptAsString() {
        return buffer.copy().toString(Charset.forName("UTF-8"));
    }

    @Override
    public void updateContent(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        responder.updateNotSupported(this);
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        parent.deleteMember(ctx, this.id(), responder);
    }

    protected void analyseProvides(String script) {
        ScriptEngineFactory nsef = new NashornScriptEngineFactory();
        ScriptEngine engine = nsef.getScriptEngine();

        try {
            //Load the script
            engine.eval(script);

            //Check if the functions we are monitoring exist or not
            for (RESOURCE_FUNCTION resourceFunction : RESOURCE_FUNCTION.values()) {

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

    public List<RESOURCE_FUNCTION> provides() {
        return this.provides;
    }
}
