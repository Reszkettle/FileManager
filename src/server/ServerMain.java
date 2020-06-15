package server;

import server.gui.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Paths;

/**
 * Class used to start server's application and JavaFX GUI
 * @author Jakub Reszka
 */
public class ServerMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui/layout.fxml"));
        Parent root  = loader.load();
        Controller controller = loader.getController();
        primaryStage.setTitle("@SERVER - File Manager v1.0");
        primaryStage.setScene(new Scene(root, 482, 540));
        primaryStage.setResizable(false);
        primaryStage.show();
        String path = Paths.get(System.getProperty("user.dir"), "Cloud").toString();
        Server server = new Server(controller, 2115, path);
        //Server server = new Server(controller, 2115, "E:\\FileServer\\Cloud");
}


    public static void main(String[] args) {
        launch(args);
    }
}
