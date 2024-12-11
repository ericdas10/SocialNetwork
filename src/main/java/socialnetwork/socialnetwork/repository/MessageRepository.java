package socialnetwork.socialnetwork.repository;

import socialnetwork.socialnetwork.domain.Message;
import socialnetwork.socialnetwork.domain.MessageType;
import socialnetwork.socialnetwork.domain.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository {
    private final Connection connection;

    public MessageRepository()throws SQLException {
        try {
            this.connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/SocialNetworkDB", "postgres", "ericdas777");
            try (Statement statement = connection.createStatement()) {
                // Ensure the users table exists
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS messages (" +
                                "id SERIAL PRIMARY KEY," +
                                "chatroom_id INTEGER NOT NULL," +
                                "user_id INTEGER NOT NULL," +
                                "message TEXT NOT NULL," +
                                "date TIMESTAMP NOT NULL," +
                                "FOREIGN KEY (chatroom_id) REFERENCES chatrooms(id)," +
                                "FOREIGN KEY (user_id) REFERENCES users(id))"
                );
            }
        } catch (SQLException e) {
            throw new SQLException("Error initializing the database", e);
        }
    }

    public void save(Message message) throws SQLException {
        String sql = "INSERT INTO messages (chatroom_id, user_id, message, date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Assuming the message contains the chatroom information
            stmt.setInt(1, message.getChatRoomId()); // You'll need to add a getChatRoomId() method to Message class
            stmt.setInt(2, message.getFrom().getId());
            stmt.setString(3, message.getMessage());
            stmt.setTimestamp(4, Timestamp.valueOf(message.getDate()));

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    message.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<Message> findAll() throws SQLException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                int fromUserId = rs.getInt("from_user_id");
                String toUserIds = rs.getString("to_user_ids");
                String messageText = rs.getString("message");
                Timestamp date = rs.getTimestamp("date");
                String type = rs.getString("type");
                int replyId = rs.getInt("reply_id");

                User fromUser = findUserById(fromUserId);
                List<User> toUsers = findUsersByIds(toUserIds);
                MessageType messageType = MessageType.valueOf(type);
                Message replyMessage = replyId != 0 ? findMessageById(replyId) : null;

                Message message = new Message(fromUser, toUsers, messageText, date.toLocalDateTime(), messageType);
                message.setId(id);
                message.setReply(replyMessage);
                messages.add(message);
            }
        }
        return messages;
    }

    private User findUserById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"));
                }
            }
        }
        return null;
    }

    private List<User> findUsersByIds(String userIds) throws SQLException {
        List<User> users = new ArrayList<>();
        String[] ids = userIds.split(",");
        for (String id : ids) {
            User user = findUserById(Integer.parseInt(id));
            if (user != null) {
                users.add(user);
            }
        }
        return users;
    }

    private Message findMessageById(int messageId) throws SQLException {
        String sql = "SELECT * FROM messages WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, messageId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int fromUserId = rs.getInt("from_user_id");
                    String toUserIds = rs.getString("to_user_ids");
                    String messageText = rs.getString("message");
                    Timestamp date = rs.getTimestamp("date");
                    String type = rs.getString("type");
                    int replyId = rs.getInt("reply_id");

                    User fromUser = findUserById(fromUserId);
                    List<User> toUsers = findUsersByIds(toUserIds);
                    MessageType messageType = MessageType.valueOf(type);
                    Message replyMessage = replyId != 0 ? findMessageById(replyId) : null;

                    Message message = new Message(fromUser, toUsers, messageText, date.toLocalDateTime(), messageType);
                    message.setId(messageId);
                    message.setReply(replyMessage);
                    return message;
                }
            }
        }
        return null;
    }

    public List<Message> findMessages(int chatRoomId) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.id, m.user_id, m.message, m.date, u.username, u.password " +
                "FROM messages m " +
                "JOIN users u ON m.user_id = u.id " +
                "WHERE m.chatroom_id = ? " +
                "ORDER BY m.date ASC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, chatRoomId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String messageText = rs.getString("message");
                Timestamp date = rs.getTimestamp("date");
                String username = rs.getString("username");
                String password = rs.getString("password");

                User user = new User(userId, username, password);
                Message message = new Message(user, null, messageText, date.toLocalDateTime(), MessageType.Message);
                message.setChatRoomId(chatRoomId);
                messages.add(message);
            }
        }
        return messages;
    }


}