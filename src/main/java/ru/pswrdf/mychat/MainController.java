package ru.pswrdf.mychat;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import ru.mychat.protostub.ServiceDef;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable, ChatEventListener {
    @FXML
    private TextArea chatArea;
    @FXML
    private TextArea messageField;
    @FXML
    private ListView userList;
    @FXML
    private Label usernameLabel;

    public void send(ActionEvent actionEvent) {
        MyChatClient.instance().send(messageField.getText());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        MyChatClient.setChatEventListener(this);
    }

    @Override
    public void userJoined(ServiceDef.User username) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                userList.getItems().add(username.getUsername());
                if (MyChatClient.instance().getClientUsername().equals(username)) {
                    usernameLabel.setText(username.getUsername() + ": ");
                }
            }
        });
    }

    @Override
    public void newMessage(ServiceDef.ChatMsg message) {
        String msg = new StringBuilder(chatArea.getText()).append(message.getSender()).append(":").append(message.getMsg()).append("\n").toString();
        chatArea.setText(msg);
    }
}
