package socialnetwork.socialnetwork.repository;

import socialnetwork.socialnetwork.domain.ChatRoom;
import socialnetwork.socialnetwork.domain.Message;
import socialnetwork.socialnetwork.domain.User;
import socialnetwork.socialnetwork.domain.MessageType;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChatRoomRepoDB {
    private final Connection connection;

    public ChatRoomRepoDB() throws SQLException {
        try {
            this.connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/SocialNetworkDB", "postgres", "ericdas777");
            try (Statement statement = connection.createStatement()) {
                // Ensure the chatrooms table exists
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS chatrooms (" +
                                "id SERIAL PRIMARY KEY," +
                                "name VARCHAR(100) NOT NULL)"
                );
                // Ensure the chatroom_users table exists
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS chatroom_users (" +
                                "chatroom_id INTEGER NOT NULL," +
                                "user_id INTEGER NOT NULL," +
                                "FOREIGN KEY (chatroom_id) REFERENCES chatrooms(id)," +
                                "FOREIGN KEY (user_id) REFERENCES users(id))"
                );
                // Ensure the messages table exists
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

    public Optional<ChatRoom> save(ChatRoom chatRoom) throws IOException {
        String sql = "INSERT INTO chatrooms (name) VALUES (?) RETURNING id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, chatRoom.getName());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int chatRoomId = resultSet.getInt("id");
                chatRoom.setId(chatRoomId);
                saveChatRoomUsers(chatRoom);
                saveMessages(chatRoom);
                return Optional.of(chatRoom);
            }
        } catch (SQLException e) {
            throw new IOException("Error saving chatroom to database " + e.getMessage());
        }
        return Optional.empty();
    }

    private void saveChatRoomUsers(ChatRoom chatRoom) throws SQLException {
        String sql = "INSERT INTO chatroom_users (chatroom_id, user_id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (User user : chatRoom.getParticipants()) {
                statement.setInt(1, chatRoom.getId());
                statement.setInt(2, user.getId());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void saveMessages(ChatRoom chatRoom) throws SQLException {
        String sql = "INSERT INTO messages (chatroom_id, user_id, message, date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Message message : chatRoom.getMessages()) {
                statement.setInt(1, chatRoom.getId());
                statement.setInt(2, message.getFrom().getId());
                statement.setString(3, message.getMessage());
                statement.setTimestamp(4, Timestamp.valueOf(message.getDate()));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    public List<ChatRoom> findAll() throws IOException {
        List<ChatRoom> chatRooms = new ArrayList<>();
        String sql = "SELECT * FROM chatrooms";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                List<User> participants = findChatRoomUsers(id);
                List<Message> messages = findMessages(id);
                chatRooms.add(new ChatRoom(id, name, participants, messages));
            }
        } catch (SQLException e) {
            throw new IOException("Error retrieving chatrooms from database", e);
        }
        return chatRooms;
    }

    private List<User> findChatRoomUsers(int chatRoomId) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.password FROM users u " +
                "JOIN chatroom_users cu ON u.id = cu.user_id WHERE cu.chatroom_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, chatRoomId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                users.add(new User(id, username, password));
            }
        }
        return users;
    }

    private List<Message> findMessages(int chatRoomId) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.id, m.user_id, m.message, m.date, u.username, u.password FROM messages m " +
                "JOIN users u ON m.user_id = u.id WHERE m.chatroom_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, chatRoomId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int userId = resultSet.getInt("user_id");
                String messageText = resultSet.getString("message");
                Timestamp date = resultSet.getTimestamp("date");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                User user = new User(userId, username, password);
                // Use the correct constructor for the Message class
                messages.add(new Message(user, List.of(user), messageText, date.toLocalDateTime(), MessageType.Message));
            }
        }
        return messages;
    }
}