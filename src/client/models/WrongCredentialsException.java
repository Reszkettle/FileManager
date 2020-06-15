package client.models;

/**
 * RuntimeException which is raised whenever client's credentials are wrong
 * @author Jakub Reszka
 */
public class WrongCredentialsException extends RuntimeException {

    /**
     * Constructs an instance of WrongCredentialsException
     * @param errorMessage the message to be shown
     * @param err the cause of raising this exception
     */
    public WrongCredentialsException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }

    /**
     * Constructs an instance of WrongCredentialsException
     * @param errorMessage the message to be shown
     */
    public WrongCredentialsException(String errorMessage) {
        super(errorMessage);
    }
}
