package server;

import server.gui.Controller;
import server.models.User;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 *  The class Server represents a server used to manage users
 * @author Jakub Reszka
 */
public class Server {

    private final int port; // Connection port
    private final Controller controller;
    private final String path;


    private ServerSocket connection = null;
    private Thread clientAcceptor = null;

    private List<User> users = new ArrayList<>();
    private ReentrantLock lock = new ReentrantLock();


    /**
     * Constructs server instance with given params
     * @param controller the controller responsible for GUI
     * @param port the port number
     * @param path the path of server's main directory
     */
    public Server(Controller controller, int port, String path) {
        this.port = port;
        this.controller = controller;
        this.path = path;
        this.openSocket();
        this.runClientAcceptor();
    }

    /**
     *  Initializes server socket
     */
    private void openSocket() {
        try {
            connection = new ServerSocket(port);
        } catch (IOException e) {
            throw new ServerSocketException("Server socket binding failed");
        }
    }

    /**
     * Runs client acceptor thread responsible for accepting clients to server
     */
    private void runClientAcceptor() {
        clientAcceptor = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                Socket socket = null;
                try {
                    socket = connection.accept();
                } catch (IOException e) {
                    throw new ServerSocketException("Client accepting failed");
                }
                User user = new User(controller, path, socket, this);
                lock.lock();
                users.add(user);
                lock.unlock();
            }
        });
        clientAcceptor.setDaemon(true);
        clientAcceptor.start();
    }

    /**
     * Sends broadcast message to all currently logged users containing list of available users
     */

    public void sendBroadcastUsersList() {

        List<String> userNamesList = new ArrayList<>();
        for(User user: users) {
            userNamesList.add(user.getUsername());
        }

        for(User user: users) {
            List<String> temp = new ArrayList<>(userNamesList);
            temp.remove(user.getUsername());
            try {
                user.sendUsersList(temp);
            } catch (IOException ignored) { }
        }
    }


    /**
     * Removes user given with username from list of users
     * @param username the name of user
     */
    public void removeFromUsersList(String username) {
        users.removeIf(user -> user.getUsername() == username);
    }

    /**
     * Gets reference to user with specific username
     * @param username the name of user
     * @return the reference to user
     */
    public User getUserByName(String username) {
        for(User user: users) {
            if(user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }
}
