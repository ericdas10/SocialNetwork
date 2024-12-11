package socialnetwork.socialnetwork.repository;

import socialnetwork.socialnetwork.domain.Notification;
import socialnetwork.socialnetwork.domain.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepoDB {
    private final Connection connection;

    public NotificationRepoDB() throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/SocialNetworkDB", "postgres", "ericdas777");
        initTable();
    }

    private void initTable() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // Drop existing table if needed
            //statement.executeUpdate("DROP TABLE IF EXISTS notifications");

            // Create table with unique constraint
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS notifications (" +
                            "id SERIAL PRIMARY KEY, " +
                            "to_user_id INTEGER NOT NULL REFERENCES users(id), " +
                            "from_user_id INTEGER NOT NULL REFERENCES users(id), " +
                            "timestamp TIMESTAMP NOT NULL, " +
                            "is_read BOOLEAN DEFAULT FALSE, " +
                            "CONSTRAINT unique_notification UNIQUE (to_user_id, from_user_id, timestamp)" +
                            ")"
            );
        }
    }

    public void save(Notification notification) throws SQLException {
        // Modifică query-ul pentru a nu mai folosi ON CONFLICT
        String sql = "INSERT INTO notifications (to_user_id, from_user_id, timestamp, is_read) " +
                "VALUES (?, ?, ?, ?) " +
                "RETURNING id";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Verifică mai întâi dacă există deja o notificare similară
            if (!notificationExists(notification)) {
                stmt.setInt(1, notification.getToUser().getId());
                stmt.setInt(2, notification.getFromUser().getId());
                stmt.setTimestamp(3, Timestamp.valueOf(notification.getTimestamp()));
                stmt.setBoolean(4, notification.isRead());

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    notification.setId(rs.getInt("id"));
                }
            }
        }
    }

    private boolean notificationExists(Notification notification) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications " +
                "WHERE to_user_id = ? AND from_user_id = ? " +
                "AND timestamp = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, notification.getToUser().getId());
            stmt.setInt(2, notification.getFromUser().getId());
            stmt.setTimestamp(3, Timestamp.valueOf(notification.getTimestamp()));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public List<Notification> getNotificationsForUser(User user) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT n.*, " +
                "u1.id as from_id, u1.username as from_username, u1.password as from_password, " +
                "u2.id as to_id, u2.username as to_username, u2.password as to_password " +
                "FROM notifications n " +
                "JOIN users u1 ON n.from_user_id = u1.id " +
                "JOIN users u2 ON n.to_user_id = u2.id " +
                "WHERE n.to_user_id = ? " +
                "ORDER BY n.timestamp DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User fromUser = new User(
                        rs.getInt("from_id"),
                        rs.getString("from_username"),
                        rs.getString("from_password")
                );

                User toUser = new User(
                        rs.getInt("to_id"),
                        rs.getString("to_username"),
                        rs.getString("to_password")
                );

                Notification notification = new Notification(
                        rs.getInt("id"),
                        toUser,
                        fromUser,
                        rs.getTimestamp("timestamp").toLocalDateTime()
                );
                notification.setRead(rs.getBoolean("is_read"));
                notifications.add(notification);
            }
        }
        return notifications;
    }

    public void markAsRead(Integer notificationId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = true WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, notificationId);
            stmt.executeUpdate();
        }
    }
}