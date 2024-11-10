# Chat Application

This project consists of a Chat Client and Chat Server implementation using Java RMI (Remote Method Invocation) for network communication.

## Chat-Client

The Chat-Client folder contains the client-side implementation of the chat application.

### Key Components:

1. `ChatClient.java`: Manages the connection to the server and provides methods for interacting with the chat service.
2. `ChatGUI.java`: Implements the graphical user interface for the chat client.
3. `ClientMain.java`: Contains the main method to start the chat client application.
4. `ClientCallbackImpl.java`: Implements the callback interface for receiving updates from the server.
5. `Message.java`: Represents a chat message with sender, recipient, content, and timestamp information.

### Features:

- Sign in and sign up functionality
- Join and leave chat rooms
- Send private messages and room messages
- File sharing capabilities
- Real-time updates for user status and new messages

## Chat-Server

The Chat-Server folder contains the server-side implementation of the chat application.

### Key Components:

1. `ChatServer.java`: Sets up the RMI registry and initializes the chat service.
2. `ChatService.java`: Defines the remote interface for the chat service.
3. `ChatServiceImpl.java`: Implements the ChatService interface and manages chat operations.
4. `ChatRoom.java`: Represents a chat room with its members and messaging functionality.
5. `DbContext.java`: Handles database operations for persisting chat data.
6. `Admin.java`: Extends the User class with additional privileges for room management.

### Features:

- User authentication and registration
- Chat room management (create, join, leave)
- Message routing between users and rooms
- File transfer support
- Persistence of user data and message history

## Getting Started

1. Compile all Java files in both Chat-Client and Chat-Server folders.
2. Start the Chat-Server by running the ChatServer class.
3. Launch the Chat-Client by running the ClientMain class or ChatGUI class.

Make sure to have the necessary RMI security policy in place and configure the database connection in the DbContext class before running the application.

## Dependencies

- Java RMI
- Swing (for GUI)
- JDBC (for database operations)
