package org.projectodd.restafari.spi;

import java.security.Principal;

public interface RequestContext {
    
    Application getApplication();
    Principal getPrinicpal();

}
