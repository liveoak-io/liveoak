package io.liveoak.mongo.service;

import com.mongodb.DB;
import io.liveoak.spi.extension.Task;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class DropDBTask extends Task {

    @Override
    protected void perform() throws Exception {
        this.dbInjector.getValue().dropDatabase();
    }

    public Injector<DB> dbInjector() {
        return this.dbInjector;
    }

    private InjectedValue<DB> dbInjector = new InjectedValue<>();
}
