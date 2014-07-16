package io.liveoak.spi.container;

/**
 * @author Bob McWhirter
 */
public interface Server {
    void start() throws Exception;
    void stop() throws Exception;
}
