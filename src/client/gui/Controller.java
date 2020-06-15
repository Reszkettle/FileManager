package client.gui;

import client.Client;
import client.models.CredentialsValidator;
import client.models.WrongCredentialsException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;

/**
 * Class represents main JavaFX controller for client
 * @author Jakub Reszka
 */
public class Controller {

    @FXML
    public Button loginButton;
    @FXML
    public Button sendButton;
    @FXML
    public Label bottomLabel;
    @FXML
    public ListView<String> usersListView;
    @FXML
    public ListView<String> filesListView;
    @FXML
    public TextField pathTextField;
    @FXML
    public TextField usernameTextField;


    public ObservableList<String> usersList = FXCollections.observableArrayList();
    public ObservableList<String> filesList = FXCollections.observableArrayList();

    private Client client = null;

    /**
     * Describes tasks to be done whenever the login button is pressed
     * @param actionEvent not used parameter
     */
    public void loginButtonPressed(ActionEvent actionEvent) {
        try {
            createClient();
            loginButton.setDisable(true);
            bottomLabel.setText("SUCCESS: You have logged in");
        } catch (WrongCredentialsException e) {
            bottomLabel.setText(e.getMessage());
        }
    }

    /**
     * Describes tasks to be done whenever the send button is pressed
     * @param actionEvent not used parameter
     */
    public void sendButtonPressed(ActionEvent actionEvent) {
        if(client == null) {
            bottomLabel.setText("ERROR: You have to log in first");
            return;
        }
        String receiver = usersListView.getSelectionModel().getSelectedItem();
        String filename = filesListView.getSelectionModel().getSelectedItem();
        if(receiver == null || filename == null) {
            bottomLabel.setText("ERROR: You have to choose receiver and filename");
            return;
        }
        try {
            client.sendFile(filename, receiver);
        } catch (IOException ignored) { }
    }

    /**
     * Runs when the client finishes his work
     */
    public void exitApplication() {
        try {
            client.sendGoodbye();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates client object with given username and path in TextFields
     */
    public void createClient() {
        String username = usernameTextField.getText();
        String path = pathTextField.getText();
        CredentialsValidator.validate(username, path);
        this.client = new Client(this, username, path, "localhost", 2115);
    }

    /**
     * Changes text of the bottom label
     * @param text the text to be set
     */
    public void changeLabel(String text) {
        bottomLabel.setText(text);
    }


    /**
     * Runs after controller's constructor, on default sets ListView items as ObservableLists
     */
    @FXML
    public void initialize() {
        usersListView.setItems(usersList);
        filesListView.setItems(filesList);
    }

}
