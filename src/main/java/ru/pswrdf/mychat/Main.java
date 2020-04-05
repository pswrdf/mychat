package ru.pswrdf.mychat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private MyChatServer server;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(ClassLoader.getSystemClassLoader().getResource("mainWindow.fxml"));
        primaryStage.setTitle("MyChat");

        FXMLLoader loader = new FXMLLoader(ClassLoader.getSystemClassLoader().getResource("login.fxml"));
        final Popup popup = new Popup();
        popup.getContent().add(loader.load());
        popup.centerOnScreen();

        Scene scene = new Scene(root, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.toFront();
        popup.show(primaryStage);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length > 0) {
            int serverport = Integer.parseInt(args[0]);
            MyChatServer.start(serverport);
        }
        launch(args);
    }
}
