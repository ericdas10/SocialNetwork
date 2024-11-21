package socialnetwork.socialnetwork.repository;

import socialnetwork.socialnetwork.domain.User;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepoDB implements AbstractRepo<Integer, User>{
    private final Connection connection;

    public UserRepoDB() throws SQLException {
        try {
            this.connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/SocialNetworkDB", "postgres", "ericdas777");
            try (Statement statement = connection.createStatement()) {
                // Ensure the users table exists
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS users (" +
                                "id SERIAL PRIMARY KEY," +
                                "username VARCHAR(100) UNIQUE NOT NULL," +
                                "password VARCHAR(100) NOT NULL)"
                );
            }
        } catch (SQLException e) {
            throw new SQLException("Error initializing the database", e);
        }
    }

    @Override
    public Optional<User> save(User entity) throws IOException {
        String sql = "INSERT INTO Users (username, password) VALUES (?, ?) RETURNING id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getUsername());
            statement.setString(2, entity.getPassword());

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                entity.setId(resultSet.getInt("id"));
                return Optional.of(entity);
            }
        } catch (SQLException e) {
            throw new IOException("Error saving user to database " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> remove(Integer id) throws IOException {
        Optional<User> entity = findOne(id);
        if (entity.isPresent()) {
            String sql = "DELETE FROM users WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new IOException("Error removing user from database", e);
            }
        }
        return entity;
    }

    @Override
    public Iterable<User> findAll() throws IOException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                users.add(new User(id, username, password));
            }
        } catch (SQLException e) {
            throw new IOException("Error retrieving users from database", e);
        }
        return users;
    }

    @Override
    public Optional<User> findOne(Integer id) throws IOException {
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
            throw new IOException("Error retrieving user from database", e);
        }
        return Optional.empty();
    }
}
