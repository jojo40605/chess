package dataaccess;

import model.GameData;
import model.UserData;
import model.AuthData;
import chess.ChessGame;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseDataAccess implements DataAccess {

    // ===================== CLEAR =====================
    @Override
    public void clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM auth");
            stmt.executeUpdate("DELETE FROM games");
            stmt.executeUpdate("DELETE FROM users");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear database", e);
        }
    }

    // ===================== USER =====================
    @Override
    public void createUser(UserData user) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO users (username, password, email) VALUES (?, ?, ?)")) {
            stmt.setString(1, user.username());
            stmt.setString(2, user.password());
            stmt.setString(3, user.email());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create user", e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT username, password, email FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new UserData(rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"));
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get user", e);
        }
    }

    // ===================== AUTH =====================
    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO auth (token, username) VALUES (?, ?)")) {
            stmt.setString(1, auth.authToken());
            stmt.setString(2, auth.username());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create auth", e);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT token, username FROM auth WHERE token = ?")) {
            stmt.setString(1, authToken);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new AuthData(rs.getString("token"),
                        rs.getString("username"));
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get auth", e);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM auth WHERE token = ?")) {
            stmt.setString(1, authToken);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete auth", e);
        }
    }

    // ===================== GAMES =====================
    @Override
    public int createGame(GameData game) throws DataAccessException {
        int gameID = 0;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO games (whiteUsername, blackUsername, gameName, gameState) VALUES (?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, game.gameName());
            stmt.setBytes(4, serializeChessGame(game.game()));

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    gameID = keys.getInt(1);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to create game", e);
        }

        return gameID;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, whiteUsername, blackUsername, gameName, gameState FROM games WHERE id = ?")) {

            stmt.setInt(1, gameID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new GameData(
                        rs.getInt("id"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        deserializeChessGame(rs.getBytes("gameState"))
                );
            } else {
                return null;
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to get game", e);
        }
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        List<GameData> games = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(
                    "SELECT id, whiteUsername, blackUsername, gameName, gameState FROM games");

            while (rs.next()) {
                games.add(new GameData(
                        rs.getInt("id"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        deserializeChessGame(rs.getBytes("gameState"))
                ));
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to list games", e);
        }
        return games;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE games SET whiteUsername = ?, blackUsername = ?, gameName = ?, gameState = ? WHERE id = ?")) {

            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, game.gameName());
            stmt.setBytes(4, serializeChessGame(game.game()));
            stmt.setInt(5, game.gameID());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Failed to update game", e);
        }
    }

    // ===================== SERIALIZATION HELPERS =====================
    private byte[] serializeChessGame(ChessGame game) throws DataAccessException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(game);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new DataAccessException("Failed to serialize ChessGame", e);
        }
    }

    private ChessGame deserializeChessGame(byte[] data) throws DataAccessException {
        if (data == null) return null;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream in = new ObjectInputStream(bis)) {
            return (ChessGame) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new DataAccessException("Failed to deserialize ChessGame", e);
        }
    }
}