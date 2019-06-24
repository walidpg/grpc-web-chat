package com.angelomelonas.grpcwebchat.common;

import com.angelomelonas.grpcwebchat.Chat.Message;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

public class ChatSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatSession.class);

    private UUID sessionId;
    private String username;
    private StreamObserver<Message> responseObserver;

    private boolean isSubscribed;

    public ChatSession() {
        this.isSubscribed = false;
    }

    public synchronized void subscribe(UUID sessionId, String username, StreamObserver responseObserver) {
        this.sessionId = sessionId;
        this.username = username;
        this.responseObserver = responseObserver;
        this.isSubscribed = true;

        LOGGER.info("Client with username {} subscribed.", this.username);
    }

    public synchronized void unsubscribe() {
        if (!this.isSubscribed) {
            throw new IllegalArgumentException("Cannot unsubscribe. Session not subscribed.");
        }

        try {

            // Try to close the stream.
            this.responseObserver.onCompleted();
        } catch (IllegalStateException exception) {
            LOGGER.error("An error was thrown while trying to unsubscribe user with username {}", this.username, exception);
            this.isSubscribed = false;
            throw exception;
        }
        this.isSubscribed = false;

        LOGGER.info("Client with username {} unsubscribed.", this.username);
    }

    public synchronized void sendMessage(String message) {
        if (!this.isSubscribed) {
//            LOGGER.warn("Client not subscribed. Message was not sent.", this.username);
            return;
        }

        Message newMessage = Message.newBuilder()
                .setUuid(String.valueOf(this.sessionId))
                .setUsername(this.username)
                .setMessage(message)
                .setTimestamp(Instant.now().toEpochMilli())
                .build();
        try {
            this.responseObserver.onNext(newMessage);
            LOGGER.info("Message sent successfully from user with username {}.", this.username);
            return;
        } catch (IllegalStateException exception) {
            LOGGER.error("Send Message Failed", exception);
            throw exception;
        }
    }

    public String getUsername() {
        return this.username;
    }

    public synchronized boolean isSubscribed() {
        return this.isSubscribed;
    }
}
