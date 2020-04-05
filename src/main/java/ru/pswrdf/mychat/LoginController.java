package ru.pswrdf.mychat;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField remoteHost;
    @FXML
    private TextField remotePort;
    @FXML
    private TextField userName;

    public void join(ActionEvent actionEvent) throws IOException, InterruptedException {
        String host = remoteHost.getText();
        int port = Integer.parseInt(remotePort.getText());

        MyChatClient.start(host, port).join(userName.getText());
        userName.getScene().getWindow().hide();
    }
}
