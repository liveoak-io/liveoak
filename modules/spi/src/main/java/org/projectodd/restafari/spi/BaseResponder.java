package org.projectodd.restafari.spi;

public interface BaseResponder {

    void respondWithError(String message);
    void respondWithError(Throwable t);
}
