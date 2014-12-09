package io.liveoak.common.codec.driver;

import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import io.liveoak.common.codec.NonEncodableValueException;
import io.liveoak.common.codec.StateEncoder;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class StateEncodingDriver extends AbstractEncodingDriver {

    protected StateEncoder stateEncoder;

    public StateEncodingDriver(RequestContext ctx, StateEncoder stateEncoder, ResourceState resourceState) {
        super(resourceState, ctx.returnFields());
        this.stateEncoder = stateEncoder;
    }

    public ResourceState state() {
        return (ResourceState) object();
    }

    @Override
    public StateEncoder encoder() {
        return stateEncoder;
    }

    @Override
    public void encode() throws Exception {
        encodeState(state(), returnFields());
    }

    protected void encodeMembers(ResourceState resourceState) throws Exception {
        if (resourceState.members() != null && !resourceState.members().isEmpty()) {
            encoder().startMembers();
            for (ResourceState memberState : resourceState.members()) {
                encodeValue(memberState, returnFields().child(LiveOak.MEMBERS));
            }
            encoder().endMembers();
        }
    }

    protected void encodeProperties(ResourceState resourceState, ReturnFields returnFields) throws Exception {
        if (!resourceState.getPropertyNames().isEmpty()) {
            encoder().startProperties();
            for (String propertyName : resourceState.getPropertyNames()) {
                if (returnFields.included(propertyName)) {
                    encodeProperty(propertyName, resourceState.getProperty(propertyName), returnFields);
                }
            }
            encoder().endProperties();
        }
    }

    protected void encodeProperty(String propertyName, Object property, ReturnFields returnFields) throws Exception {
        encoder().startProperty(propertyName);
        if (property instanceof ResourceState) {
            ResourceState resourceState = (ResourceState) property;
            // If the resource id is null, then its an 'embedded resource' and should be expanded
            if (resourceState.id() != null) {
                encodeValue(resourceState, returnFields.child(resourceState.id()));
            } else {
                encodeValue(resourceState, returnFields.ALL);
            }
        } else {
            encodeValue(property, returnFields);
        }
        encoder().endProperty(propertyName);
    }

    protected void encodeValue(Object value, ReturnFields returnFields) throws Exception {
        if (value instanceof ResourceState) {
            encodeState((ResourceState) value, returnFields);
        } else if (value instanceof String) {
            encoder().writeValue((String) value);
        } else if (value instanceof Integer) {
            encoder().writeValue((Integer) value);
        } else if (value instanceof Double) {
            encoder().writeValue((Double) value);
        } else if (value instanceof Long) {
            encoder().writeValue((Long) value);
        } else if (value instanceof Date) {
            encoder().writeValue((Date) value);
        } else if (value instanceof URI) {
            encoder().writeValue(((URI) value).getPath());
        } else if (value instanceof Boolean) {
            encoder().writeValue((Boolean) value);
        } else if (value instanceof Map) {
            encoder().writeValue((Map) value);
        } else if (value instanceof Collection) {
            encodeList((Collection) value, returnFields);
        } else if (value == null) {
            encoder().writeNullValue();
        } else {
            throw new NonEncodableValueException(value);
        }
    }

    protected void encodeState(ResourceState resourceState, ReturnFields returnFields) throws Exception {

        if (returnFields.excluded("id")) {
            resourceState.id(null);
        }

        if (returnFields.excluded("self")) {
            resourceState.uri(null);
        }

        encoder().startResource(resourceState);
        encodeProperties(resourceState, returnFields);
        encodeMembers(resourceState);
        encoder().endResource(resourceState);

    }

    protected void encodeList(Collection list, ReturnFields returnFields) throws Exception {
        encoder().startList();
        for (Object element : list) {
            if (element instanceof ResourceState) {

            }
            encodeValue(element, returnFields);
        }
        encoder().endList();
    }

    @Override
    public void close() throws Exception {
        encoder().close();
    }
}
