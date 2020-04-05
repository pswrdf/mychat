package ru.pswrdf.mychat;

import com.google.protobuf.Empty;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import ru.mychat.protostub.MychatGrpc;
import ru.mychat.protostub.ServiceDef;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MyChatServer {

    private static MyChatServer SELF;
    private Server server;

    private MyChatServer(int port) throws IOException, InterruptedException {
        server = ServerBuilder.forPort(port).addService(new MyChatService()).build();

        server.start();
        Runtime.getRuntime().addShutdownHook(
                new Thread() {
                    @Override
                    public void run() {
                        System.err.println("*** shutting down gRPC server since JVM is shutting down");
                        try {
                            MyChatServer.this.stop();
                        } catch (InterruptedException e) {
                            e.printStackTrace(System.err);
                        }
                        System.err.println("*** server shut down");
                    }
                });
    }

    public static MyChatServer start(int port) throws IOException, InterruptedException {
        if (SELF == null) {
            SELF = new MyChatServer(port);
        }
        return SELF;
    }

    public void stop() throws InterruptedException {
        server.shutdown().awaitTermination(10, TimeUnit.SECONDS);
    }

    private static class MyChatService extends MychatGrpc.MychatImplBase {
        private Map<ServiceDef.User, EventObserver> receivers = (Map<ServiceDef.User, EventObserver>) Collections.synchronizedMap(new HashMap<ServiceDef.User, EventObserver>());

        @Override
        public void send(ServiceDef.ChatMsg request, StreamObserver<Empty> responseObserver) {
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
            receivers.values().forEach(r -> {
                r.newMessage(request);
            });
        }

        @Override
        public void join(ServiceDef.User request, StreamObserver<Empty> responseObserver) {
            if (receivers.containsKey(request)) {
                responseObserver.onError(new IllegalArgumentException("username already in use"));
            } else {
                receivers.values().forEach(r -> {
                    r.userJoined(request);
                });
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
            }
        }

        @Override
        public void joinedUsers(ServiceDef.User request, StreamObserver<ServiceDef.User> responseObserver) {
            EventObserver eventObserver = receivers.get(request);
            if (eventObserver == null) {
                eventObserver = new EventObserver();
                eventObserver.setUserNotifier(responseObserver);
                receivers.put(request, eventObserver);
            } else {
                eventObserver.setUserNotifier(responseObserver);
            }
            receivers.keySet().forEach(u -> responseObserver.onNext(u));
        }

        @Override
        public void receiveMessages(ServiceDef.User request, StreamObserver<ServiceDef.ChatMsg> responseObserver) {
            EventObserver eventObserver = receivers.get(request);
            if (eventObserver == null) {
                eventObserver = new EventObserver();
                eventObserver.setMsgNotifier(responseObserver);
                receivers.put(request, eventObserver);
            } else {
                eventObserver.setMsgNotifier(responseObserver);
            }
        }
    }

    private static class EventObserver implements ChatEventListener {
        private StreamObserver<ServiceDef.User> userNotifier;
        private StreamObserver<ServiceDef.ChatMsg> msgNotifier;

        private EventObserver() {
        }

        public StreamObserver<ServiceDef.ChatMsg> getMsgNotifier() {
            return msgNotifier;
        }

        public void setMsgNotifier(StreamObserver<ServiceDef.ChatMsg> msgNotifier) {
            this.msgNotifier = msgNotifier;
        }

        public StreamObserver<ServiceDef.User> getUserNotifier() {
            return userNotifier;
        }

        public void setUserNotifier(StreamObserver<ServiceDef.User> userNotifier) {
            this.userNotifier = userNotifier;
        }

        @Override
        public void userJoined(ServiceDef.User user) {
            if (userNotifier != null)
                userNotifier.onNext(user);
        }

        @Override
        public void newMessage(ServiceDef.ChatMsg message) {
            if (msgNotifier != null)
                msgNotifier.onNext(message);
        }
    }
}
