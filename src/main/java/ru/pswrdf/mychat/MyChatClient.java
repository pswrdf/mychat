package ru.pswrdf.mychat;

import com.google.protobuf.Empty;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import ru.mychat.protostub.MychatGrpc;
import ru.mychat.protostub.ServiceDef;

import java.io.IOException;

public class MyChatClient {
    public static MyChatClient SELF;
    private final MychatGrpc.MychatStub stub;
    private final MychatGrpc.MychatBlockingStub stubBlk;
    private ServiceDef.User clientUsername;
    private static ChatEventListener chatEventListener;

    private MyChatClient(String host, int port) {
        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forAddress(host, port).usePlaintext();
        stub = MychatGrpc.newStub(channelBuilder.build());
        stubBlk = MychatGrpc.newBlockingStub(channelBuilder.build());
    }

    public static MyChatClient start(String host, int port) throws IOException, InterruptedException {
        if (SELF == null) {
            SELF = new MyChatClient(host, port);
        }
        return SELF;
    }

    public static MyChatClient instance() {
        return SELF;
    }

    public void send(String message) {
        ServiceDef.ChatMsg msg = ServiceDef.ChatMsg.newBuilder().setMsg(message).setSender(clientUsername.getUsername()).build();
        stubBlk.send(msg);
    }

    public void join(String username) {
        clientUsername = ServiceDef.User.newBuilder().setUsername(username).build();
        stubBlk.join(clientUsername);
        stub.joinedUsers(clientUsername, new StreamObserver<ServiceDef.User>() {
            @Override
            public void onNext(ServiceDef.User value) {
                chatEventListener.userJoined(value);
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
            }
        });
        stub.receiveMessages(clientUsername, new StreamObserver<ServiceDef.ChatMsg>() {
            @Override
            public void onNext(ServiceDef.ChatMsg value) {
                chatEventListener.newMessage(value);
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
            }
        });
    }

    public static void setChatEventListener(ChatEventListener jul) {
        chatEventListener = jul;
    }

    public ServiceDef.User getClientUsername() {
        return clientUsername;
    }
}
