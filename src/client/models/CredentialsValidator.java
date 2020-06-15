package client.models;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class CredentialsValidator {

    public static void validate(String username, String path) {

        if(!Files.isDirectory(Paths.get(path)))
            throw new WrongCredentialsException("Invalid folder path");
        if(path.isEmpty())
            throw new WrongCredentialsException("Path is empty");
        if(username.isEmpty())
            throw new WrongCredentialsException("Username is empty");
    }
}
