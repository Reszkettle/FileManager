package client;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import client.gui.Controller;
import javafx.stage.WindowEvent;

/**
 * Class used to start client's application and JavaFX GUI
 * @author Jakub Reszka
 */
public class ClientMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui/layout.fxml"));
        Parent root  = loader.load();
        Controller controller = loader.getController();
        primaryStage.setTitle("@CLIENT - File Manager v1.0");
        primaryStage.setScene(new Scene(root, 482, 540));
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                controller.exitApplication();
            }
        });
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
