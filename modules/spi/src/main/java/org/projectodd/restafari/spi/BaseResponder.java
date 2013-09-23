package org.projectodd.restafari.spi;

/** Base asynchronous responder.
 * 
 * @author Bob McWhirter
 */
public interface BaseResponder {

    /** Respond with an error, ending the interaction.
     * 
     * @param message The error message.
     */
    void respondWithError(String message);
    
    /** Responde with an exception, ending the interaction.
     * 
     * @param t The exception.
     */
    void respondWithError(Throwable t);
}
