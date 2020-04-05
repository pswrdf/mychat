package ru.pswrdf.mychat;

import ru.mychat.protostub.ServiceDef;

public interface ChatEventListener {
    void userJoined(ServiceDef.User username);
    void newMessage(ServiceDef.ChatMsg message);
}
