package server.models;

/**
 * RuntimeException which is raised whenever there is a problem with creating directory
 * @author Jakub Reszka
 */
public class FolderExistsException extends RuntimeException {

    /**
     * Constructs an instance of FolderExistsException
     * @param errorMessage the message to be shown
     * @param err the cause of raising this exception
     */
    public FolderExistsException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }


    /**
     * Constructs an instance of FolderExistsException
     * @param errorMessage the message to be shown
     */
    public FolderExistsException(String errorMessage) {
        super(errorMessage);
    }
}
