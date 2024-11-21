package socialnetwork.socialnetwork.repository;

import socialnetwork.socialnetwork.domain.*;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FriendshipRepoDB implements AbstractRepo<Integer, Friendship> {
    private final String url = "jdbc:postgresql://localhost:5432/SocialNetworkDB";
    private final String user = "postgres";
    private final String password = "ericdas777";
    private final Connection connection;

    public FriendshipRepoDB() throws SQLException {
        try {
            this.connection = DriverManager.getConnection(url, user, password);
            try (Statement statement = connection.createStatement()) {
                // Ensure the friendships table exists
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS friendships (" +
                                "id SERIAL PRIMARY KEY," +
                                "user1_id INTEGER REFERENCES users(id) ON DELETE CASCADE," +
                                "user2_id INTEGER REFERENCES users(id) ON DELETE CASCADE," +
                                "status VARCHAR(20) NOT NULL," +
                                "data_adaugare DATE)"
                );
            }
        } catch (SQLException e) {
            throw new SQLException("Error initializing the database", e);
        }
    }

    @Override
    public Optional<Friendship> save(Friendship friendship) throws IOException {
        String sql = "INSERT INTO friendships (user1_id, user2_id, status, data_adaugare) VALUES (?, ?, ?, ?) RETURNING id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, friendship.getUser1().getId());
            statement.setInt(2, friendship.getUser2().getId());
            statement.setString(3, friendship.getStatus().name());
            statement.setDate(4, Date.valueOf(friendship.getDate()));

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                friendship.setId(resultSet.getInt("id"));
                return Optional.of(friendship);
            }
        } catch (SQLException e) {
            throw new IOException("Error saving friendship to database", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Friendship> remove(Integer id) throws IOException {
        Optional<Friendship> friendship = findOne(id);
        if (friendship.isPresent()) {
            String sql = "DELETE FROM friendships WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new IOException("Error removing friendship from database", e);
            }
        }
        return friendship;
    }

    @Override
    public Iterable<Friendship> findAll() throws IOException {
        List<Friendship> friendships = new ArrayList<>();
        String sql = "SELECT * FROM friendships";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int user1Id = resultSet.getInt("user1_id");
                int user2Id = resultSet.getInt("user2_id");
                LocalDate data_adaugare = resultSet.getDate("data_adaugare").toLocalDate();
                FriendshipStatus status = FriendshipStatus.valueOf(resultSet.getString("status"));

                // Load user objects for user1 and user2
                User user1 = findUserById(user1Id).get();
                User user2 = findUserById(user2Id).get();
                friendships.add(new Friendship(id, user1, user2, status, data_adaugare));
            }
        } catch (SQLException e) {
            throw new IOException("Error retrieving friendships from database", e);
        }
        return friendships;
    }

    @Override
    public Optional<Friendship> findOne(Integer id) throws IOException {
        String sql = "SELECT * FROM friendships WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int user1Id = resultSet.getInt("user1_id");
                int user2Id = resultSet.getInt("user2_id");
                LocalDate data = resultSet.getDate("data_adaugare").toLocalDate();
                FriendshipStatus status = FriendshipStatus.valueOf(resultSet.getString("status"));

                // Load user objects for user1 and user2
                User user1 = findUserById(user1Id).get();
                User user2 = findUserById(user2Id).get();
                return Optional.of(new Friendship(id, user1, user2, status, data));
            }
        } catch (SQLException e) {
            throw new IOException("Error retrieving friendship from database", e);
        }
        return Optional.empty();
    }

    public Optional<User> findUserById(int id) throws IOException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                return Optional.of(new User(id, username, password));
            }
        } catch (SQLException e) {
            throw new IOException("Error retrieving username by ID", e);
        }
        return Optional.empty();
    }
}

