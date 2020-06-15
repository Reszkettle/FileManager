package server.models;

import client.StreamException;
import common.FileManager;
import javafx.application.Platform;
import server.Server;
import server.gui.Controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 *  The class User represents a user model used to handle communication with certain user
 * @author Jakub Reszka
 */
public class User {

    private final Controller controller;
    private final Socket socket;
    private final String cloudPath;
    private final Server server;

    private int id = 0;

    private String username = null;
    private DataInputStream input = null;
    private DataOutputStream output = null;

    private Thread reader = null;
    private List<String> filesList = new ArrayList<>();
    private String path = null;

    private ReentrantLock lock = new ReentrantLock();

    /**
     * Constructs user instance with given params
     * @param controller the controller responsible for server GUI
     * @param cloudPath the path to server's directory
     * @param socket the socket connecting server with client
     * @param server the server instance
     */
    public User(Controller controller, String cloudPath, Socket socket, Server server) {
        this.controller = controller;
        this.cloudPath = cloudPath;
        this.socket = socket;
        this.server = server;
        this.createDirectory();
        this.createStreams();
        this.runReader();
    }

    /**
     * Starts reader thread responsible for reading DataInputStream
     * The reader thread is daemonic thread
     */
    private void runReader() {
        reader = new Thread(() -> {
            while(true) {
                try {
                    readStream();
                } catch (IOException ignored) {
                }
            }
        });
        reader.setDaemon(true);
        reader.start();
    }

    /**
     * Reads byte which describes message header from DataInputStream
     * @return message header
     * @throws IOException
     */
    private byte readHeader() throws IOException {
        return input.readByte();
    }

    /**
     * Reads whole message from DataInputStream by executing read* methods for certain header
     * @param header the type of message to be read
     * @throws IOException
     */
    private void readMessage(byte header) throws IOException {
        if (header == 1) {
            readGreeting();
        } else if (header == 2) {
            readGoodbye();
        } else if (header == 3) {
            readFile();
            sendFilesList();
            Platform.runLater(() -> controller.updateFilesList(username, filesList));
        } else if (header == 5) {
            readDeleted();
            sendFilesList();
            Platform.runLater(() -> controller.updateFilesList(username, filesList));
        } else if (header == 7) {
            readFileToClient();
        }
    }

    /**
     * Reads the name of the file and the name of the client to which the file should be sent
     * @throws IOException if an error occurs when reading
     */
    public void readFileToClient() throws IOException {
        String filename = input.readUTF();
        String receiver = input.readUTF();
        server.getUserByName(receiver).sendFile(Paths.get(path, filename).toFile());
    }

    /**
     * Sends file to DataOutputStream in the form of header=3, file's length, file's name and file's bytes
     * This method is threadsafe thanks to ReentrantLock set on output stream
     * @param file the file handler
     * @throws IOException if an error occurs when writing
     */
    public void sendFile(File file) throws IOException {
        try {
            FileManager.waitTillFileIsReady(file);
        } catch (InterruptedException ignored) {
        }
        byte[] fileBytes = FileManager.readFileToByteArray(file);
        lock.lock();
        output.writeByte(3);
        output.writeInt(fileBytes.length);
        output.writeUTF(file.getName());
        output.write(fileBytes, 0, fileBytes.length);
        lock.unlock();
        Platform.runLater(() -> controller.changeLabel("SENT: " + file.getName() + " to " + username));
    }

    /**
     * Reads file from DataInputStream as a sequence of bytes and saves it in local folder given by path
     * @throws IOException
     */
    private void readFile() throws IOException {
        int fileLength = input.readInt();
        String fileName = input.readUTF();
        byte[] fileBytes = new byte[fileLength];
        input.readNBytes(fileBytes, 0, fileLength);
        boolean success = FileManager.saveFileFromByteArray(fileBytes, path, fileName);
        if(success) {
            Platform.runLater(() -> controller.changeLabel("SAVED: " + fileName));
            filesList.add(fileName);
        }
        else Platform.runLater(() -> controller.changeLabel("ERROR WHEN SAVING: " + fileName));
    }

    /**
     * Reads greeting from client containing clients username
     */
    private void readGreeting() throws IOException {
        this.username = input.readUTF();
        Platform.runLater(() -> controller.addUserToUsersList(this.username));
        Platform.runLater(() -> controller.changeLabel("JOINED: " + this.username.toUpperCase()));
        server.sendBroadcastUsersList();
    }

    /**
     * Reads goodbye from client which stands for "client is stopping his work"
     */
    private void readGoodbye() {
        Platform.runLater(() -> controller.removeUserFromUsersList(this.username));
        Platform.runLater(() -> controller.changeLabel("LEFT: " + this.username.toUpperCase()));
        server.removeFromUsersList(this.username);
        reader.interrupt();
        server.sendBroadcastUsersList();
    }

    /**
     * Reads files to be deleted from server
     * @throws IOException
     */
    private void readDeleted() throws IOException {
        int deletedCount = input.readInt();

        for(int i=0; i<deletedCount; ++i) {
            String fileName = input.readUTF();
            removeFileFromServer(fileName);
        }
    }

    /**
     * Reads DataInputStream
     * @throws IOException
     */
    private void readStream() throws IOException {
        readMessage(readHeader());
    }

    /**
     * Creates directory for user in the form of user_%POSSIBLE_ID%
     * Sets user's path
     */
    private void createDirectory() {
        generateID();
        String postfix = "client_" + Integer.toString(id);
        this.path = Paths.get(cloudPath, postfix).toString();
        Path pathHandle = Paths.get(this.path);
        try {
            Files.createDirectory(pathHandle);
            Platform.runLater(() -> controller.changeLabel("Created directory for client " + Integer.toString(id)));
        } catch (IOException e) {
            throw new FolderExistsException("Folder of given path already exists");
        }
    }

    /**
     * Generates unique ID for client
     */
    private void generateID() {
        while(true) {
            if (Paths.get(cloudPath, "client_"+id).toFile().exists() == true) {
                id++;
            } else {
                break;
            }
        }
    }

    /**
     * Removes file given with fileName from server
     * @param fileName
     */
    private void removeFileFromServer(String fileName) {
        String fullPath = Paths.get(path, fileName).toString();
        File file = new File(fullPath);
        boolean success = file.delete();
        if(success) Platform.runLater(() ->controller.changeLabel("DELETED: " + fileName));
        filesList.remove(fileName);
    }


    /**
     *  Creates input and output DataStream between client and server
     */
    private void createStreams() {
        createInputStream();
        createOutputStream();
    }


    /**
     *  Creates data input stream instance
     */
    private void createInputStream() {
        try {
            input = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new StreamException("Couldn't create input stream");
        }
    }

    /**
     *  Creates data output stream instance
     */
    private void createOutputStream() {
        try {
            output = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new StreamException("Couldn't create output stream");
        }
    }


    /**
     * Sends list of files in server's directory to user
     * @throws IOException if an error occurs when writing
     */
    public void sendFilesList() throws IOException {
        lock.lock();
        output.writeByte(4);
        output.writeInt(filesList.size());
        for(String file: filesList) {
            output.writeUTF(file);
        }
        lock.unlock();
        Platform.runLater(()->controller.changeLabel("SENT: " + "list of files to " + username));
    }


    /**
     * Sends list of currently logged users to user
     * @param usersList the list of users
     * @throws IOException if an error occurs when writing
     */
    public void sendUsersList(List<String> usersList) throws IOException {
        lock.lock();
        output.writeByte(6);
        output.writeInt(usersList.size());
        for(String user: usersList) {
            output.writeUTF(user);
        }
        lock.unlock();
        Platform.runLater(()->controller.changeLabel("SENT: " + "list of users to " + username));
    }

    /**
     * @return username
     */
    public String getUsername() {
        return username;
    }
}
