package server;


/**
 * RuntimeException which is raised whenever serverSocket problem occurs
 * @author Jakub Reszka
 */
public class ServerSocketException extends RuntimeException {

    /**
     * Constructs an instance of ServerSocketException
     * @param errorMessage the message to be shown
     * @param err the cause of raising this exception
     */
    public ServerSocketException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }

    /**
     * Constructs an instance of ServerSocketException
     * @param errorMessage the message to be shown
     */
    public ServerSocketException(String errorMessage) {
        super(errorMessage);
    }
}
