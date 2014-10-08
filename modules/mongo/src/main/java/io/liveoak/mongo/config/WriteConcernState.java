package io.liveoak.mongo.config;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.WriteConcern;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.InvalidPropertyTypeException;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class WriteConcernState extends EmbeddedConfigResource{

    private WriteConcern writeConcern;

    public static final String ID = "WriteConcern";

    public static final String W = "w";
    public static final String WTIMEOUT = "wTimeout";
    public static final String J = "j";
    public static final String FSYNC = "fsync";
    public static final String CONTINUEONERRORFORINSERT = "continueOnErrorForInsert";

    public WriteConcernState(Resource parent, ResourceState state) throws Exception {
        super(parent);
        Object w = state.getProperty(W);

        Integer wTimeout = state.getProperty(WTIMEOUT, false, Integer.class);
        wTimeout = (wTimeout == null) ? 0 : wTimeout;

        Boolean j = state.getProperty(J, false, Boolean.class);
        j = (j == null) ? false: j;

        Boolean fsync = state.getProperty(FSYNC, false, Boolean.class);
        fsync = (fsync == null) ? false: fsync;

        Boolean continueOnError = state.getProperty(CONTINUEONERRORFORINSERT, false, Boolean.class);
        continueOnError = (continueOnError == null) ? false: continueOnError;

        if (w instanceof String) {
            this.writeConcern = new WriteConcern((String)w, wTimeout, fsync, j, continueOnError);
        } else if (w instanceof Integer) {
            this.writeConcern = new WriteConcern((Integer)w, wTimeout, fsync, j, continueOnError);
        } else {
            throw new InvalidPropertyTypeException("Invalid property type. The property named 'w' must be either a String or Integer");
        }
    }

    public WriteConcernState(Resource parent, WriteConcern writeConcern) {
        super(parent);
        this.writeConcern = writeConcern;
    }

    public WriteConcern getWriteConcern() {
        return writeConcern;
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(W, writeConcern.getWObject());
        map.put(WTIMEOUT, writeConcern.getWtimeout());
        map.put(J, writeConcern.getJ());
        map.put(FSYNC, writeConcern.getFsync());
        map.put(CONTINUEONERRORFORINSERT, writeConcern.getContinueOnErrorForInsert());

        return map;
    }
}
