package client;

import client.gui.Controller;
import client.models.Sender;
import common.FileManager;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 *  The class Client represents a client model used to communicate with server
 * @author Jakub Reszka
 */

public class Client implements Sender {

    private final Controller controller;
    private final String username;
    private final String host;
    private final String path;
    private final int port;

    private Thread folderObserver = null;
    private List<String> filesList = new ArrayList<>();
    private Socket socket = null;
    private DataOutputStream output = null;
    private DataInputStream input = null;
    private ReentrantLock lock = new ReentrantLock();
    private Thread reader = null;

    /**
     * Constructs client instance with given params
     * @param controller the controller responsible for GUI
     * @param username the client name
     * @param path the path to local folder
     * @param host the host name
     * @param port the port number
     */

    public Client(Controller controller, String username, String path, String host, int port) {

        this.controller = controller;
        this.username = username;
        this.host = host;
        this.port = port;
        this.path = path;
        this.openSocket();
        this.createStreams();
        this.sendGreeting();
        this.observeFolder();
        this.runReader();
    }

    /**
     * Starts folder observer thread responsible for observing changes in local folder
     * The folder observer thread is a daemonic thread
     */
    private void observeFolder() {
        folderObserver = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    List<String> newFileList = FileManager.listDir(path);
                    Hashtable<String, List<String>> diff = FileManager.compareLists(filesList, newFileList);
                    try {
                        sendAdded(diff.get("Added"));
                        sendDeleted(diff.get("Deleted"));
                    } catch (IOException e) {
                        throw new StreamException("Error with sending files to server");
                    }
                    filesList.clear();
                    filesList.addAll(newFileList);
                }
            }
        });
        folderObserver.setDaemon(true);
        folderObserver.start();
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

    /***
     *  Creates a connection with server by initializing socket
     */

    private void openSocket() {
        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            throw new ClientSocketException("Client socket binding failed");
        }
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
          // ZAKOMENTOWANE  filesList.add(fileName);
        }
        else Platform.runLater(() -> controller.changeLabel("ERROR WHEN SAVING: " + fileName));
    }

    /**
     * Sends file to DataOutputStream in the form of header=3, file's length, file's name and file's bytes
     * This method is threadsafe thanks to ReentrantLock set on output stream
     * @param file the file handler
     * @throws IOException if an error occurs when writing
     */
    @Override
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
        Platform.runLater(() -> controller.changeLabel("SENT: " + file.getName()));
    }

    /**
     * Sends greeting to server in the form of header=3 and client's username
     */
    @Override
    public void sendGreeting() {
        lock.lock();
        try {
            output.writeByte(1);
            output.writeUTF(this.username);
        } catch (IOException e) {
            throw new StreamException("ERROR: Sending greeting to server");
        }
        lock.unlock();
        Platform.runLater(() -> controller.changeLabel("Sent greeting to server"));
    }

    /**
     * Sends goodbye to server in the form of header=2
     */
    @Override
    public void sendGoodbye() throws IOException {
        lock.lock();
        output.writeByte(2);
        lock.unlock();
        Platform.runLater(() -> controller.changeLabel("SENT: Goodbye to server"));
    }

    /**
     * Sends names of files to be deleted to server in the form of header=5, count of files, files' names
     */
    @Override
    public void sendDeleted(List<String> deletedFiles) throws IOException {

        if(deletedFiles == null) return;

        lock.lock();
        output.writeByte(5);
        output.writeInt(deletedFiles.size());
        for (String file: deletedFiles) {
            output.writeUTF(file);
        }
        lock.unlock();
        Platform.runLater(() -> controller.changeLabel("SENT: Files to delete to server"));
    }

    /**
     * Sends new files which appeared in the local folder to server by multiple execution of sendFile
     * @param addedFiles the list of files' names to be added on server
     */
    @Override
    public void sendAdded(List<String> addedFiles) {

        if(addedFiles == null) return;

        for (String fileName : addedFiles) {
            String filePath = Paths.get(path, fileName).toString();
            File file = new File(filePath);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sendFile(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
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

        if (header == 4) {
            readFilesList();
        } else if (header == 6) {
            readUsersList();
        } else if (header == 3) {
            readFile();
        }
    }

    /**
     * Reads list of users from DataOutputStream and sets it in the GUI
     * @throws IOException
     */
    private void readUsersList() throws IOException {
        int usersCount = input.readInt();

        List<String> usersList = new ArrayList<>();
        for(int i = 0; i < usersCount; ++i) {
            usersList.add(input.readUTF());
        }

        Platform.runLater(() -> controller.usersList.setAll(usersList));
        Platform.runLater(() -> controller.changeLabel("RECEIVED: List of users"));

    }

    /**
     * Reads list of files from DataOutputStream and sets it in the GUI
     * @throws IOException
     */
    private void readFilesList() throws IOException {

        int filesCount = input.readInt();

        List<String> filesList = new ArrayList<>();
        for(int i = 0; i < filesCount; ++i) {
            filesList.add(input.readUTF());
        }

        Platform.runLater(() -> controller.filesList.setAll(filesList));
        Platform.runLater(() -> controller.changeLabel("RECEIVED: List of files"));
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
                } catch (IOException e) {
                    throw new StreamException("Reading stream error");
                }
            }
        });
        reader.setDaemon(true);
        reader.start();
    }

    /**
     * Reads DataInputStream
     * @throws IOException
     */
    private void readStream() throws IOException {
        readMessage(readHeader());
    }

    /**
     * Sends file's name to be sent to another user in the form of header=7, file's name, receiver's name
     * @param filename the name of file to be sent
     * @param receiver the name of destination client
     * @throws IOException if an error occurs when writing
     */
    public void sendFile(String filename, String receiver) throws IOException {
        lock.lock();
        output.writeByte(7);
        output.writeUTF(filename);
        output.writeUTF(receiver);
        lock.unlock();
        Platform.runLater(() -> controller.changeLabel("SENT: " + filename + " to " + receiver));
    }
}
