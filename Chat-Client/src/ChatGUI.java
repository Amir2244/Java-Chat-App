import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatGUI extends JFrame implements ClientCallback {
    private static final Logger logger = Logger.getLogger(ChatGUI.class.getName());
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String PLEASE_LOGIN_FIRST = "Please login first";

    private final transient ChatClient client;
    private String currentUser;


    private JTextArea chatArea;
    private JTextField messageField;
    private JList<String> userList;
    private JList<String> roomList;
    private JList<String> roomClientsList;
    private DefaultListModel<String> userListModel;
    private DefaultListModel<String> roomListModel;
    private DefaultListModel<String> roomClientsModel;
    private final Set<String> joinedRooms = new HashSet<>();
    private JButton sendButton;
    private JButton joinRoomButton;
    private JButton leaveRoomButton;
    private JButton sendFileButton;

    public ChatGUI() {
        super("Chat Application");
        client = new ChatClient("localhost", 1099);
        initializeComponents();
        setupGUI();
        setupListeners();
        startRefreshTimers();
    }

    private void initializeComponents() {
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        messageField = new JTextField();
        sendFileButton = new JButton("Send File");
        userListModel = new DefaultListModel<>();
        roomListModel = new DefaultListModel<>();
        roomClientsModel = new DefaultListModel<>();

        userList = new JList<>(userListModel);
        roomList = new JList<>(roomListModel);
        roomClientsList = new JList<>(roomClientsModel);

        sendButton = new JButton("Send");
        joinRoomButton = new JButton("Join");
        leaveRoomButton = new JButton("Leave");
    }


    private void setupGUI() {
        setTitle("Chat Application");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createLeftPanel(), BorderLayout.WEST);
        add(createCenterPanel(), BorderLayout.CENTER);
        setJMenuBar(createMenuBar());
    }

    private void sendFile() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, PLEASE_LOGIN_FIRST);
            return;
        }

        String selectedRoom = roomList.getSelectedValue();
        String selectedUser = userList.getSelectedValue();

        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fileChooser.getSelectedFile();
                byte[] fileData = Files.readAllBytes(selectedFile.toPath());
                String fileName = selectedFile.getName();

                if (selectedRoom != null && joinedRooms.contains(selectedRoom)) {
                    client.sendFileToRoom(currentUser, selectedRoom, fileData, fileName);
                    chatArea.append("Sending file " + fileName + " to room: " + selectedRoom + "\n");
                } else if (selectedUser != null) {
                    client.sendFile(currentUser, selectedUser, fileData, fileName);
                    chatArea.append("Sending file " + fileName + " to user: " + selectedUser + "\n");
                } else {
                    JOptionPane.showMessageDialog(this, "Please select a room or user first");
                }
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Failed to send file", ex);
                JOptionPane.showMessageDialog(this, "Failed to send file: " + ex.getMessage());
            }
        }
    }


    @Override
    public void onFileReceived(Message fileMessage) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            int response = JOptionPane.showConfirmDialog(this,
                    "Received file " + fileMessage.getFileName() + " from " + fileMessage.getSender() + ". Save file?",
                    "File Received",
                    JOptionPane.YES_NO_OPTION);

            if (response == JOptionPane.YES_OPTION) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(new File(fileMessage.getFileName()));
                if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        Files.write(fileChooser.getSelectedFile().toPath(), fileMessage.getFileData());
                        chatArea.append("File saved: " + fileMessage.getFileName() + "\n");
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public void onRoomFileReceived(String roomName, Message fileMessage) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            int response = JOptionPane.showConfirmDialog(this,
                    "Received file " + fileMessage.getFileName() + " from " + fileMessage.getSender() + " in room " + roomName + ". Save file?",
                    "File Received",
                    JOptionPane.YES_NO_OPTION);

            if (response == JOptionPane.YES_OPTION) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(new File(fileMessage.getFileName()));
                if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        Files.write(fileChooser.getSelectedFile().toPath(), fileMessage.getFileData());
                        chatArea.append("File saved from room " + roomName + ": " + fileMessage.getFileName() + "\n");
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage());
                    }
                }
            }
        });
    }


    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(200, getHeight()));

        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setBorder(BorderFactory.createTitledBorder("Online Users"));

        JSplitPane roomSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JScrollPane roomScrollPane = new JScrollPane(roomList);
        JScrollPane clientsScrollPane = new JScrollPane(roomClientsList);
        roomScrollPane.setBorder(BorderFactory.createTitledBorder("Chat Rooms"));
        clientsScrollPane.setBorder(BorderFactory.createTitledBorder("Room Members"));

        roomSplitPane.setTopComponent(roomScrollPane);
        roomSplitPane.setBottomComponent(clientsScrollPane);
        roomSplitPane.setResizeWeight(0.5);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(joinRoomButton);
        buttonPanel.add(leaveRoomButton);

        leftPanel.add(userScrollPane, BorderLayout.NORTH);
        leftPanel.add(roomSplitPane, BorderLayout.CENTER);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);

        return leftPanel;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        JPanel messagePanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(sendFileButton);
        buttonPanel.add(sendButton);

        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(buttonPanel, BorderLayout.EAST);

        centerPanel.add(chatScrollPane, BorderLayout.CENTER);
        centerPanel.add(messagePanel, BorderLayout.SOUTH);

        return centerPanel;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Account");

        JMenuItem loginItem = new JMenuItem("Login");
        JMenuItem registerItem = new JMenuItem("Register");
        JMenuItem createRoomItem = new JMenuItem("Create Room");
        JMenuItem signOutItem = new JMenuItem("Sign Out");

        loginItem.addActionListener(_ -> showLoginDialog());
        registerItem.addActionListener(_ -> showRegisterDialog());
        createRoomItem.addActionListener(_ -> showCreateRoomDialog());
        signOutItem.addActionListener(_ -> signOut());

        menu.add(loginItem);
        menu.add(registerItem);
        menu.add(createRoomItem);
        menu.add(signOutItem);
        menuBar.add(menu);

        return menuBar;
    }

    private void signOut() {
        if (currentUser != null) {
            try {
                client.signOut(currentUser);
                client.unregisterCallback(currentUser);
                currentUser = null;
                chatArea.append("Signed out successfully\n");

                userListModel.clear();
                roomListModel.clear();
                roomClientsModel.clear();
                joinedRooms.clear();

            } catch (RemoteException ex) {
                logger.log(Level.WARNING, "Sign out failed", ex);
                JOptionPane.showMessageDialog(this, "Failed to sign out: " + ex.getMessage());
            }
        }
    }


    private void setupListeners() {
        sendButton.addActionListener(_ -> sendMessage());
        joinRoomButton.addActionListener(_ -> joinRoom());
        leaveRoomButton.addActionListener(_ -> leaveRoom());
        messageField.addActionListener(_ -> sendMessage());
        sendFileButton.addActionListener(_ -> sendFile());
        roomList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                try {
                    updateRoomClientsList();
                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selectedUser = userList.getSelectedValue();
                    if (selectedUser != null) {
                        showPrivateMessageDialog(selectedUser);
                    }
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (currentUser != null) {
                    try {
                        client.unregisterCallback(currentUser);
                    } catch (RemoteException ex) {
                        logger.log(Level.WARNING, "Error unregistering callback", ex);
                    }
                }
            }
        });
    }

    private void startRefreshTimers() {
        Timer refreshTimer = new Timer(5000, _ -> {
            if (currentUser != null) {
                try {
                    updateOnlineUsers();
                    updateRoomList();
                    updateRoomClientsList();
                } catch (RemoteException ex) {
                    logger.log(Level.WARNING, "Refresh failed", ex);
                }
            }
        });
        refreshTimer.start();
    }

    private void sendMessage() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, PLEASE_LOGIN_FIRST);
            return;
        }

        String content = messageField.getText().trim();
        if (!content.isEmpty()) {
            try {
                String selectedRoom = roomList.getSelectedValue();
                String selectedUser = userList.getSelectedValue();

                messageField.setText("");

                if (selectedRoom != null && joinedRooms.contains(selectedRoom)) {
                    client.sendMessageToRoom(currentUser, selectedRoom, content);
                } else if (selectedUser != null) {
                    client.sendMessage(currentUser, selectedUser, content);
                    String formattedMessage = String.format("[PRIVATE] You to %s: %s%n", selectedUser, content);
                    chatArea.append(formattedMessage);
                }
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            } catch (RemoteException ex) {
                logger.log(Level.WARNING, "Failed to send message", ex);
                JOptionPane.showMessageDialog(this, "Failed to send message: " + ex.getMessage());
            }
        }
    }

    private void updateOnlineUsers() throws RemoteException {
        String[] users = client.getOnlineUsers();
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : users) {
                if (!user.equals(currentUser)) {
                    userListModel.addElement(user);
                }
            }
        });
    }

    private void updateRoomList() throws RemoteException {
        String[] rooms = client.getAvailableRooms();
        SwingUtilities.invokeLater(() -> {
            String selected = roomList.getSelectedValue();
            roomListModel.clear();
            for (String room : rooms) {
                roomListModel.addElement(room);
            }
            if (selected != null) {
                roomList.setSelectedValue(selected, true);
            }
        });
    }

    private void updateRoomClientsList() throws RemoteException {
        String selectedRoom = roomList.getSelectedValue();
        if (selectedRoom != null) {
            String[] clients = client.getRoomClients(selectedRoom);
            SwingUtilities.invokeLater(() -> {
                roomClientsModel.clear();
                Arrays.stream(clients).forEach(c -> roomClientsModel.addElement(c));
            });
        }
    }

    private void loadMessageHistory() throws RemoteException {
        java.util.List<Message> history = client.getMessageHistory(currentUser);
        for (Message message : history) {
            displayMessage(message);
        }
    }

    private void showLoginDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        if (JOptionPane.showConfirmDialog(this, panel, "Login",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (client.signIn(username, password)) {
                    currentUser = username;
                    ClientCallback callback = (ClientCallback) UnicastRemoteObject.exportObject(this, 0);
                    client.registerCallback(username, callback);
                    chatArea.append("Logged in as: " + currentUser + "\n");
                    updateOnlineUsers();
                    updateRoomList();
                    loadMessageHistory();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials");
                }
            } catch (RemoteException ex) {
                logger.log(Level.WARNING, "Login failed", ex);
                JOptionPane.showMessageDialog(this, "Login failed: " + ex.getMessage());
            }
        }
    }

    private void showRegisterDialog() {
        JPanel panel = new JPanel(new GridLayout(4, 2));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("First Name:"));
        panel.add(firstNameField);
        panel.add(new JLabel("Last Name:"));
        panel.add(lastNameField);

        if (JOptionPane.showConfirmDialog(this, panel, "Register",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                client.signUp(usernameField.getText(),
                        new String(passwordField.getPassword()),
                        firstNameField.getText(),
                        lastNameField.getText());
                JOptionPane.showMessageDialog(this, "Registration successful!");
            } catch (RemoteException ex) {
                logger.log(Level.WARNING, "Registration failed", ex);
                JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage());
            }
        }
    }

    private void showCreateRoomDialog() {
        String roomName = JOptionPane.showInputDialog(this, "Enter room name:");
        if (roomName != null && !roomName.trim().isEmpty()) {
            try {
                client.addChatRoom(roomName);
                updateRoomList();
            } catch (RemoteException ex) {
                logger.log(Level.WARNING, "Failed to create room", ex);
                JOptionPane.showMessageDialog(this, "Failed to create room: " + ex.getMessage());
            }
        }
    }

    private void showPrivateMessageDialog(String recipient) {
        String message = JOptionPane.showInputDialog(this, "Message to " + recipient + ":");
        if (message != null && !message.trim().isEmpty()) {
            try {
                client.sendMessage(currentUser, recipient, message);
                String formattedMessage = String.format("[PRIVATE] You to %s: %s%n", recipient, message);
                chatArea.append(formattedMessage);
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            } catch (RemoteException ex) {
                logger.log(Level.WARNING, "Failed to send private message", ex);
                JOptionPane.showMessageDialog(this, "Failed to send message: " + ex.getMessage());
            }
        }
    }

    private void joinRoom() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, PLEASE_LOGIN_FIRST);
            return;
        }

        String selectedRoom = roomList.getSelectedValue();
        if (selectedRoom != null && !joinedRooms.contains(selectedRoom)) {
            try {
                if (client.joinRoom(currentUser, selectedRoom)) {
                    joinedRooms.add(selectedRoom);
                    chatArea.append("Successfully joined room: " + selectedRoom + "\n");
                    updateRoomClientsList();
                } else {
                    JOptionPane.showMessageDialog(this, "Could not join room. Room might no longer be available.");
                }
            } catch (RemoteException ex) {
                logger.log(Level.WARNING, "Failed to join room", ex);
                JOptionPane.showMessageDialog(this, "Failed to join room: " + ex.getMessage());
            }
        }
    }

    private void leaveRoom() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, PLEASE_LOGIN_FIRST);
            return;
        }

        String selectedRoom = roomList.getSelectedValue();
        if (selectedRoom != null) {
            try {
                if (client.leaveRoom(currentUser, selectedRoom)) {
                    joinedRooms.remove(selectedRoom);
                    chatArea.append("Left room: " + selectedRoom + "\n");
                    updateRoomClientsList();
                } else {
                    JOptionPane.showMessageDialog(this, "Could not leave room. You might not be in this room.");
                }
            } catch (RemoteException ex) {
                logger.log(Level.WARNING, "Failed to leave room", ex);
                JOptionPane.showMessageDialog(this, "Failed to leave room: " + ex.getMessage());
            }
        }
    }

    @Override
    public void onMessageReceived(Message message) throws RemoteException {
        SwingUtilities.invokeLater(() -> displayMessage(message));
    }

    @Override
    public void onRoomMessageReceived(String roomName, Message message) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            String formattedMessage = String.format("[%s] [%s] %s: %s%n",
                    new Date(message.getTimestamp()),
                    roomName,
                    message.getSender(),
                    message.getContent());
            chatArea.append(formattedMessage);
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    @Override
    public void onUserStatusChanged(String username, boolean online) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            if (online) {
                if (!userListModel.contains(username) && !username.equals(currentUser)) {
                    userListModel.addElement(username);
                }
            } else {
                userListModel.removeElement(username);
            }
        });
    }

    private void displayMessage(Message message) {
        String formattedMessage = String.format("[%s] %s: %s%n",
                new Date(message.getTimestamp()),
                message.getSender(),
                message.getContent());
        chatArea.append(formattedMessage);
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        System.setProperty("java.security.policy", "security.policy");
        System.setProperty("java.rmi.server.useCodebaseOnly", "false");

        SwingUtilities.invokeLater(() -> {
            ChatGUI gui = new ChatGUI();
            gui.setVisible(true);
        });
    }
}
