package common;
/**
 * RuntimeException which is raised whenever incorrect path was provided
 * @author Jakub Reszka
 */
public class InvalidPathException extends RuntimeException {

    /**
     * Constructs an instance of InvalidPathException
     * @param errorMessage the message to be shown
     * @param err the cause of raising this exception
     */
    public InvalidPathException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }

    /**
     * Constructs an instance of ClientSocketException
     * @param errorMessage the message to be shown
     */
    public InvalidPathException(String errorMessage) {
        super(errorMessage);
    }
}
