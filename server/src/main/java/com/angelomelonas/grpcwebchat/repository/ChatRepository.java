package com.angelomelonas.grpcwebchat.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Repository
public class ChatRepository {

    @Autowired
    JdbcTemplate template;

    public void addUser(UUID sessionId, String username) {
        if (doesSessionExist(sessionId)) {
            // The session already exists.
            return;
        }

        String query = "INSERT INTO User(sessionId, username) VALUES(?,?)";

        template.update(query, preparedStatement -> {
            preparedStatement.setObject(1, sessionId);
            preparedStatement.setString(2, username);
        });
    }

    public void addMessage(UUID sessionId, String message, Instant timestamp) {
        String query = "INSERT INTO Message(sessionId, message, timestamp) VALUES(?,?,?)";

        template.update(query, preparedStatement -> {
            preparedStatement.setObject(1, sessionId);
            preparedStatement.setString(2, message);
            preparedStatement.setTimestamp(3, Timestamp.from(timestamp));
        });
    }

    public boolean doesSessionExist(UUID sessionId) {
        String query = "SELECT count(*) FROM User WHERE sessionId = ?";

        return template.queryForObject(query, new Object[]{sessionId}, Integer.class) > 0;
    }

    public boolean doesUserExist(String username) {
        String query = "SELECT count(*) FROM User WHERE username = ?";

        return template.queryForObject(query, new Object[]{username}, Integer.class) > 0;
    }

    public int messagesBySessionId(UUID sessionId) {
        String query = "SELECT count(*) FROM Message WHERE sessionId = ?";

        return template.queryForObject(query, new Object[]{sessionId}, Integer.class);
    }
}