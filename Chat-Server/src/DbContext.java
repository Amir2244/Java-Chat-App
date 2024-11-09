import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbContext {
    private static DbContext instance;
    private Connection connection;
    private final Logger logger = Logger.getInstance();

    private DbContext(String dbName, String user, String password) {
        try {
            String baseUrl = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : "jdbc:mysql://localhost:3306/";

            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(baseUrl, user, password);

            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
            }

            connection = DriverManager.getConnection(baseUrl + dbName, user, password);

            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("""
                            CREATE TABLE IF NOT EXISTS users (
                                username varchar(50) NOT NULL,
                                password varchar(255) NOT NULL,
                                firstName varchar(50) NOT NULL,
                                lastName varchar(50) NOT NULL,
                                PRIMARY KEY (username)
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
                        """);

                stmt.executeUpdate("""
                            CREATE TABLE IF NOT EXISTS chat_rooms (
                                room_id bigint NOT NULL AUTO_INCREMENT,
                                room_name varchar(100) NOT NULL,
                                created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (room_id),
                                UNIQUE KEY room_name (room_name)
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
                        """);

                stmt.executeUpdate("""
                            CREATE TABLE IF NOT EXISTS messages (
                                id bigint NOT NULL AUTO_INCREMENT,
                                sender varchar(50) NOT NULL,
                                recipient varchar(50) NOT NULL,
                                content text NOT NULL,
                                timestamp bigint NOT NULL,
                                PRIMARY KEY (id),
                                KEY sender (sender),
                                KEY recipient (recipient),
                                FOREIGN KEY (sender) REFERENCES users (username),
                                FOREIGN KEY (recipient) REFERENCES users (username)
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
                        """);

                stmt.executeUpdate("""
                            CREATE TABLE IF NOT EXISTS room_members (
                                room_id bigint NOT NULL,
                                username varchar(50) NOT NULL,
                                joined_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (room_id,username),
                                KEY username (username),
                                FOREIGN KEY (room_id) REFERENCES chat_rooms (room_id),
                                FOREIGN KEY (username) REFERENCES users (username)
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
                        """);

                stmt.executeUpdate("""
                            CREATE TABLE IF NOT EXISTS room_messages (
                                id bigint NOT NULL AUTO_INCREMENT,
                                room_id bigint NOT NULL,
                                sender varchar(50) NOT NULL,
                                content text NOT NULL,
                                timestamp bigint NOT NULL,
                                PRIMARY KEY (id),
                                KEY room_id (room_id),
                                KEY sender (sender),
                                FOREIGN KEY (room_id) REFERENCES chat_rooms (room_id),
                                FOREIGN KEY (sender) REFERENCES users (username)
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
                        """);
                logger.info("Database and tables created/verified successfully");
            }

            logger.info("Database connection established successfully to: " + dbName);
        } catch (SQLException e) {
            logger.error("Database connection failed: " + e.getMessage());
            logger.error("Stack trace: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            logger.error("JDBC Driver not found: " + e.getMessage());
        }
    }


    public static DbContext getInstance(String dbName, String user, String password) {
        DbContext result = instance;
        if (result == null) {
            synchronized (DbContext.class) {
                result = instance;
                if (result == null) {
                    instance = result = new DbContext(dbName, user, password);
                }
            }
        }
        return result;
    }

    public boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password, firstName, lastName) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFirstName());
            pstmt.setString(4, user.getLastName());
            pstmt.executeUpdate();
            logger.info("New user registered successfully: " + user.getUsername());
            return true;
        } catch (SQLException e) {
            logger.error("Failed to add user " + user.getUsername() + ": " + e.getMessage());
            return false;
        }
    }

    public User getUser(String username) {
        String sql = "SELECT " + "* FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = UserFactory.createUser(
                        "regular",
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("firstName"),
                        rs.getString("lastName")
                );
                logger.info("User retrieved from database: " + username);
                return user;
            }
            logger.warn("User not found in database: " + username);
        } catch (SQLException e) {
            logger.error("Error retrieving user " + username + ": " + e.getMessage());
        }
        return null;
    }

    public void saveMessage(Message message) {
        String sql = "INSERT INTO messages (sender, recipient, content, timestamp) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, message.getSender());
            pstmt.setString(2, message.getRecipient());
            pstmt.setString(3, message.getContent());
            pstmt.setLong(4, message.getTimestamp());
            pstmt.executeUpdate();
            logger.info("Message saved: From " + message.getSender() + " to " + message.getRecipient());
        } catch (SQLException e) {
            logger.error("Failed to save message: " + e.getMessage());
        }
    }

    public List<Message> getMessageHistory(String signedInUser) {
        List<Message> messages = new ArrayList<>();
        String sql = """
                SELECT sender, recipient, content, timestamp FROM messages\s
                WHERE sender = ? OR recipient = ?
                ORDER BY timestamp""";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, signedInUser);
            pstmt.setString(2, signedInUser);

            ResultSet rs = pstmt.executeQuery();
            int messageCount = 0;
            while (rs.next()) {
                Message message = new Message(
                        rs.getString("sender"),
                        rs.getString("recipient"),
                        rs.getString("content"),
                        rs.getLong("timestamp")
                );
                messages.add(message);
                messageCount++;
            }
            logger.info("Retrieved " + messageCount + " messages for user " + signedInUser);
        } catch (SQLException e) {
            logger.error("Failed to retrieve message history: " + e.getMessage());
        }
        return messages;
    }

    public boolean authenticateUser(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                boolean authenticated = rs.getString("password").equals(password);
                if (authenticated) {
                    logger.info("User authenticated successfully: " + username);
                } else {
                    logger.warn("Failed authentication attempt for user: " + username);
                }
                return authenticated;
            }
            logger.warn("Authentication attempt for non-existent user: " + username);
        } catch (SQLException e) {
            logger.error("Authentication error for user " + username + ": " + e.getMessage());
        }
        return false;
    }


    public void saveRoom(ChatRoom room) {
        String sql = "INSERT INTO chat_rooms (room_name) VALUES (?) ON DUPLICATE KEY UPDATE room_name = room_name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, room.getName());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                long roomId = rs.getLong(1);
                logger.info("Chat room saved to database with ID " + roomId + ": " + room.getName());
            }
        } catch (SQLException e) {
            logger.error("Failed to save chat room: " + e.getMessage());
        }
    }


    public void saveRoomMessage(Message message, String roomName) {
        String getRoomIdSql = "SELECT room_id FROM chat_rooms WHERE room_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(getRoomIdSql)) {
            pstmt.setString(1, roomName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                long roomId = rs.getLong("room_id");
                String sql = "INSERT INTO room_messages (sender, room_id, content, timestamp) VALUES (?, ?, ?, ?)";
                try (PreparedStatement msgStmt = connection.prepareStatement(sql)) {
                    msgStmt.setString(1, message.getSender());
                    msgStmt.setLong(2, roomId);
                    msgStmt.setString(3, message.getContent());
                    msgStmt.setLong(4, message.getTimestamp());
                    msgStmt.executeUpdate();
                    logger.info("Room message saved: From " + message.getSender() + " in room " + roomName);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to save room message: " + e.getMessage());
        }
    }

    public void saveRoomMember(String username, String roomName) {
        String getRoomIdSql = "SELECT room_id FROM chat_rooms WHERE room_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(getRoomIdSql)) {
            pstmt.setString(1, roomName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                long roomId = rs.getLong("room_id");
                String sql = "INSERT INTO room_members (room_id, username) VALUES (?, ?)";
                try (PreparedStatement memberStmt = connection.prepareStatement(sql)) {
                    memberStmt.setLong(1, roomId);
                    memberStmt.setString(2, username);
                    memberStmt.executeUpdate();
                    logger.info("Room member saved: User " + username + " in room " + roomName);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to save room member: " + e.getMessage());
        }
    }

    public ChatRoom[] getAllRooms() {
        List<ChatRoom> rooms = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT room_name FROM chat_rooms")) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ChatRoom room = new ChatRoom(rs.getString("room_name"));
                rooms.add(room);
                logger.info("Retrieved chat room from database: " + room.getName());
            }
        } catch (SQLException e) {
            logger.error("Failed to retrieve chat rooms: " + e.getMessage());
        }
        return rooms.toArray(new ChatRoom[0]);
    }

    public void deleteRoom(String roomName) {
        String getRoomIdSql = "SELECT room_id FROM chat_rooms WHERE room_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(getRoomIdSql)) {
            pstmt.setString(1, roomName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                long roomId = rs.getLong("room_id");

                String deleteMembers = "DELETE FROM room_members WHERE room_id = ?";
                try (PreparedStatement memberStmt = connection.prepareStatement(deleteMembers)) {
                    memberStmt.setLong(1, roomId);
                    memberStmt.executeUpdate();
                }

                String deleteMessages = "DELETE FROM room_messages WHERE room_id = ?";
                try (PreparedStatement messageStmt = connection.prepareStatement(deleteMessages)) {
                    messageStmt.setLong(1, roomId);
                    messageStmt.executeUpdate();
                }

                String deleteRoom = "DELETE FROM chat_rooms WHERE room_id = ?";
                try (PreparedStatement roomStmt = connection.prepareStatement(deleteRoom)) {
                    roomStmt.setLong(1, roomId);
                    roomStmt.executeUpdate();
                }

                logger.info("Chat room and related data deleted from database: " + roomName);
            }
        } catch (SQLException e) {
            logger.error("Failed to delete chat room: " + e.getMessage());
        }
    }

    public boolean isUserInRoom(String username, String roomName) {
        String sql = """
                     SELECT rm.username\s
                     FROM room_members rm\s
                     JOIN chat_rooms cr ON rm.room_id = cr.room_id\s
                     WHERE rm.username = ? AND cr.room_name = ?
                \s""";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, roomName);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            logger.error("Failed to check room membership: " + e.getMessage());
            return false;
        }
    }

}