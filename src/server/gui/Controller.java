package server.gui;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class represents main JavaFX controller for server
 * @author Jakub Reszka
 */
public class Controller {

    public ObservableList<String> usersList = FXCollections.observableArrayList();
    HashMap<String, ObservableList<String>> filesList = new HashMap<>();


    @FXML
    public ListView<String> usersListView;
    @FXML
    public ListView<String> filesListView;
    @FXML
    public Label bottomLabel;

    private Thread filesListRefresher = null;

    /**
     * Changes text of the bottom label
     * @param text the text to be set
     */
    public void changeLabel(String text) {
        bottomLabel.setText(text);
    }

    /**
     * Runs after controller's constructor, on default sets ListView items as ObservableLists and starts refresher thread
     */
    @FXML
    public void initialize() {
        usersListView.setItems(usersList);
        runRefresher();
    }

    /**
     * Adds user to users list, and updates GUI
     * @param username the name of user to be added
     */
    public void addUserToUsersList(String username) {
        usersList.add(username);
        filesList.put(username, FXCollections.observableArrayList());
    }

    /**
     * Updates list of files for certain user with new list given by parameter
     * @param username the name of user whose list is updated
     * @param newList the new list of files
     */
    public void updateFilesList(String username, List<String> newList) {
        filesList.get(username).setAll(newList);
    }

    /**
     * Removes user to users list
     * @param username the name of user to be removed
     */
    public void removeUserFromUsersList(String username) {
        usersList.remove(username);
    }

    /**
     * Runs refresher thread responsible for refreshing list of files in GUI every 100ms
     * This thread is daemonic
     */
    private void runRefresher() {
        filesListRefresher = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                Platform.runLater(() ->

                        usersListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                            @Override
                            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                                if(t1 != null)
                                    filesListView.setItems(filesList.get(t1));
                            }
                        })
                    );
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) { }
                }
            }
        });
        filesListRefresher.setDaemon(true);;
        filesListRefresher.start();
    }

}

