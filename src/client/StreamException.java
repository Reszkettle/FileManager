package client;
/**
 * RuntimeException which is raised whenever problem on input or data stream occurs
 * @author Jakub Reszka
 */
public class StreamException extends RuntimeException {

    /**
     * Constructs an instance of StreamException
     * @param errorMessage the message to be shown
     * @param err the cause of raising this exception
     */
    public StreamException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }


    /**
     * Constructs an instance of StreamException
     * @param errorMessage the message to be shown
     */
    public StreamException(String errorMessage) {
        super(errorMessage);
    }
}
