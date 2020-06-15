package client;
/**
 * RuntimeException which is raised whenever client's socket problem occurs
 * @author Jakub Reszka
 */
public class ClientSocketException extends RuntimeException {

    /**
     * Constructs an instance of ClientSocketException
     * @param errorMessage the message to be shown
     * @param err the cause of raising this exception
     */
    public ClientSocketException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }


    /**
     * Constructs an instance of ClientSocketException
     * @param errorMessage the message to be shown
     */
    public ClientSocketException(String errorMessage) {
        super(errorMessage);
    }

}

