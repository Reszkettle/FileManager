package client.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Interface represents client's sender methods
 * @author Jakub Reszka
 */
public interface Sender {

    void sendFile(File file) throws IOException;
    void sendDeleted(List<String> deletedFiles) throws IOException;
    void sendAdded(List<String> addedFiles) throws IOException;
    void sendGreeting() throws IOException;
    void sendGoodbye() throws IOException;
}
