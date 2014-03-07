
package io.liveoak.common.codec.driver;

import io.liveoak.common.codec.NonEncodableValueException;
import io.liveoak.common.codec.StateEncoder;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;

import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class StateEncodingDriver extends AbstractEncodingDriver {

    protected StateEncoder stateEncoder;

    public StateEncodingDriver(RequestContext ctx, StateEncoder stateEncoder, ResourceState resourceState) {
        super (resourceState, ctx.returnFields());
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
        encoder().startResource(state());
        encodeProperties(state());
        encodeMembers(state());
        encoder().endResource( state() );
    }

    protected void encodeMembers(ResourceState resourceState) throws Exception {
        if ( resourceState.members() != null && !resourceState.members().isEmpty() ) {
            encoder().startMembers();
            for (ResourceState memberState :resourceState.members()) {
                encodeValue( memberState );
            }
            encoder().endMembers();
        }
    }

    protected void encodeProperties( ResourceState resourceState ) throws Exception {
        if ( !resourceState.getPropertyNames().isEmpty() ) {
            encoder().startProperties();
            for ( String propertyName : resourceState.getPropertyNames() ) {
                encodeProperty( propertyName, resourceState.getProperty( propertyName ) );
            }
            encoder().endProperties();
        }
    }

    protected void encodeProperty(String propertyName, Object property) throws Exception{
        encoder().startProperty( propertyName ) ;
        encodeValue( property );
        encoder().endProperty( propertyName );
    }

    protected void encodeValue(Object value) throws Exception {
        if (value instanceof ResourceState) {
            encodeState( ( ResourceState ) value );
        }
        else if (value instanceof String) {
            encoder().writeValue((String) value);
        } else if (value instanceof Integer) {
            encoder().writeValue((Integer) value);
        } else if (value instanceof Double) {
            encoder().writeValue((Double) value);
        } else if (value instanceof Long) {
            encoder().writeValue( ( Long ) value );
        } else if (value instanceof Date ) {
            encoder().writeValue( ( Date ) value );
       //TODO: figure out when writing a link should be used....
//        } else if (value instanceof URI ) {
////        } else if (property instanceof ResourceState) {
//            encoder().writeLink((URI) property);
//        }
        } else if (value instanceof URI) {
            encoder().writeValue(((URI)value).getPath());
        } else if (value instanceof Boolean) {
            encoder().writeValue( ( Boolean ) value );
        } else if (value instanceof Map ) {
            encoder().writeValue( ( Map ) value );
        } else if (value instanceof Collection ) {
            encodeList((Collection)value);
        } else if (value == null) {
            encoder().writeNullValue();
        } else {
            throw new NonEncodableValueException(value);
        }
    }

    protected void encodeState(ResourceState resourceState) throws Exception {
        encoder().startResource( resourceState );
        encodeProperties( resourceState );
        encodeMembers( resourceState );
        encoder().endResource( resourceState );

    }

    protected void encodeList(Collection list) throws Exception {
        encoder().startList();
        for (Object element : list) {
            if (element instanceof ResourceState) {

            }
            encodeValue( element );
        }
        encoder().endList();
    }

    @Override
    public void close() throws Exception {
        encoder().close();
    }
}
