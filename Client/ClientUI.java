import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class ClientUI extends JFrame {
    private ProtocolClient client;
    private Handshake handshake;
    private boolean requestInProgress = false;

    // Connection panel components
    private JTextField hostField;
    private JTextField portField;
    private JButton connectButton;
    private JButton disconnectButton;
    private JLabel statusLabel;
    private JLabel handshakeLabel;

    // POST panel components
    private JTextField postXField;
    private JTextField postYField;
    private JComboBox<String> postColourCombo;
    private JTextField postMessageField;
    private JButton postButton;

    // GET panel components
    private JCheckBox getColourCheckbox;
    private JComboBox<String> getColourCombo;
    private JCheckBox getContainsCheckbox;
    private JTextField getContainsXField;
    private JTextField getContainsYField;
    private JCheckBox getRefersToCheckbox;
    private JTextField getRefersToField;
    private JButton getNotesButton;
    private JButton getPinsButton;

    // PIN/UNPIN panel components
    private JTextField pinXField;
    private JTextField pinYField;
    private JButton pinButton;
    private JButton unpinButton;

    // Management panel components
    private JButton shakeButton;
    private JButton clearButton;

    // Output area
    private JTextArea outputArea;

    public ClientUI() {
        super("Bulletin Board Client");
        client = new ProtocolClient();
        initComponents();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        updateConnectionState(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout(5, 5));

        // Connection panel
        JPanel connectionPanel = createConnectionPanel();
        add(connectionPanel, BorderLayout.NORTH);

        // Commands panel (tabbed)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("POST", createPostPanel());
        tabbedPane.addTab("GET", createGetPanel());
        tabbedPane.addTab("PIN/UNPIN", createPinPanel());
        tabbedPane.addTab("Management", createManagementPanel());
        add(tabbedPane, BorderLayout.CENTER);

        // Output panel
        JPanel outputPanel = createOutputPanel();
        add(outputPanel, BorderLayout.SOUTH);
    }

    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Connection"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Host:"), gbc);

        gbc.gridx = 1;
        hostField = new JTextField("127.0.0.1", 15);
        panel.add(hostField, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Port:"), gbc);

        gbc.gridx = 3;
        portField = new JTextField("6789", 8);
        panel.add(portField, gbc);

        gbc.gridx = 4;
        connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> handleConnect());
        panel.add(connectButton, gbc);

        gbc.gridx = 5;
        disconnectButton = new JButton("Disconnect");
        disconnectButton.addActionListener(e -> handleDisconnect());
        panel.add(disconnectButton, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 6;
        statusLabel = new JLabel("Status: Not connected");
        statusLabel.setForeground(Color.RED);
        panel.add(statusLabel, gbc);

        gbc.gridy = 2;
        handshakeLabel = new JLabel(" ");
        handshakeLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        panel.add(handshakeLabel, gbc);

        return panel;
    }

    private JPanel createPostPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("X:"), gbc);

        gbc.gridx = 1;
        postXField = new JTextField(8);
        panel.add(postXField, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Y:"), gbc);

        gbc.gridx = 3;
        postYField = new JTextField(8);
        panel.add(postYField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Colour:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 3;
        postColourCombo = new JComboBox<>();
        panel.add(postColourCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        panel.add(new JLabel("Message:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 3;
        postMessageField = new JTextField(30);
        panel.add(postMessageField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        postButton = new JButton("POST Note");
        postButton.addActionListener(e -> handlePost());
        panel.add(postButton, gbc);

        return panel;
    }

    private JPanel createGetPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Colour filter
        gbc.gridx = 0; gbc.gridy = 0;
        getColourCheckbox = new JCheckBox("Filter by colour:");
        panel.add(getColourCheckbox, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        getColourCombo = new JComboBox<>();
        panel.add(getColourCombo, gbc);

        // Contains filter
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        getContainsCheckbox = new JCheckBox("Filter by contains:");
        panel.add(getContainsCheckbox, gbc);

        gbc.gridx = 1;
        JPanel containsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        containsPanel.add(new JLabel("X:"));
        getContainsXField = new JTextField(6);
        containsPanel.add(getContainsXField);
        containsPanel.add(new JLabel("Y:"));
        getContainsYField = new JTextField(6);
        containsPanel.add(getContainsYField);
        gbc.gridwidth = 2;
        panel.add(containsPanel, gbc);

        // RefersTo filter
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        getRefersToCheckbox = new JCheckBox("Filter by refersTo:");
        panel.add(getRefersToCheckbox, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        getRefersToField = new JTextField(20);
        panel.add(getRefersToField, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        getNotesButton = new JButton("GET Notes");
        getNotesButton.addActionListener(e -> handleGetNotes());
        buttonPanel.add(getNotesButton);

        getPinsButton = new JButton("GET PINS");
        getPinsButton.addActionListener(e -> handleGetPins());
        buttonPanel.add(getPinsButton);

        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JPanel createPinPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("X:"), gbc);

        gbc.gridx = 1;
        pinXField = new JTextField(10);
        panel.add(pinXField, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Y:"), gbc);

        gbc.gridx = 3;
        pinYField = new JTextField(10);
        panel.add(pinYField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        pinButton = new JButton("PIN");
        pinButton.addActionListener(e -> handlePin());
        buttonPanel.add(pinButton);

        unpinButton = new JButton("UNPIN");
        unpinButton.addActionListener(e -> handleUnpin());
        buttonPanel.add(unpinButton);

        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JPanel createManagementPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 30));

        shakeButton = new JButton("SHAKE (Remove Unpinned)");
        shakeButton.addActionListener(e -> handleShake());
        panel.add(shakeButton);

        clearButton = new JButton("CLEAR (Remove All)");
        clearButton.addActionListener(e -> handleClear());
        panel.add(clearButton);

        return panel;
    }

    private JPanel createOutputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Output"));
        panel.setPreferredSize(new Dimension(0, 250));

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void handleConnect() {
        String host = hostField.getText().trim();
        String portText = portField.getText().trim();

        if (host.isEmpty()) {
            appendError("Host cannot be empty");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            appendError("Invalid port number");
            return;
        }

        setControlsEnabled(false);
        appendInfo("Connecting to " + host + ":" + port + "...");

        SwingWorker<Handshake, Void> worker = new SwingWorker<Handshake, Void>() {
            @Override
            protected Handshake doInBackground() throws Exception {
                return client.connect(host, port);
            }

            @Override
            protected void done() {
                try {
                    handshake = get();
                    appendInfo("Connected! Received HELLO:");
                    appendInfo("  Board: " + handshake.boardW + "x" + handshake.boardH +
                             ", Note: " + handshake.noteW + "x" + handshake.noteH);
                    appendInfo("  Colours: " + String.join(", ", handshake.colours));

                    handshakeLabel.setText("Board: " + handshake.boardW + "x" + handshake.boardH +
                                         " | Note: " + handshake.noteW + "x" + handshake.noteH +
                                         " | Colours: " + String.join(", ", handshake.colours));

                    populateColourDropdowns(handshake.colours);
                    updateConnectionState(true);
                } catch (Exception e) {
                    appendError("Connection failed: " + e.getMessage());
                    // Ensure client is cleaned up on failed connection
                    try {
                        client.close();
                    } catch (Exception ex) {
                        // Ignore cleanup errors
                    }
                    updateConnectionState(false);
                }
            }
        };
        worker.execute();
    }

    private void handleDisconnect() {
        setControlsEnabled(false);
        appendInfo("Disconnecting...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                client.disconnect();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    appendInfo("Disconnected");
                } catch (Exception e) {
                    appendError("Disconnect error: " + e.getMessage());
                } finally {
                    // Always clean up, even if disconnect fails
                    try {
                        client.close();
                    } catch (Exception ex) {
                        // Ignore cleanup errors
                    }
                }
                updateConnectionState(false);
                handshake = null;
                handshakeLabel.setText(" ");
            }
        };
        worker.execute();
    }

    private void handlePost() {
        String xText = postXField.getText().trim();
        String yText = postYField.getText().trim();
        String colour = (String) postColourCombo.getSelectedItem();
        String message = postMessageField.getText().trim();

        if (xText.isEmpty() || yText.isEmpty()) {
            appendError("X and Y are required");
            return;
        }

        int x, y;
        try {
            x = Integer.parseInt(xText);
            y = Integer.parseInt(yText);
        } catch (NumberFormatException e) {
            appendError("X and Y must be integers");
            return;
        }

        if (x < 0 || y < 0) {
            appendError("X and Y must be non-negative");
            return;
        }

        if (colour == null || colour.isEmpty()) {
            appendError("Please select a colour");
            return;
        }

        String command = "POST " + x + " " + y + " " + colour;
        if (!message.isEmpty()) {
            command += " " + message;
        }

        sendCommand(command);
    }

    private void handleGetNotes() {
        StringBuilder command = new StringBuilder("GET");

        if (getColourCheckbox.isSelected()) {
            String colour = (String) getColourCombo.getSelectedItem();
            if (colour != null && !colour.isEmpty()) {
                command.append(" colour=").append(colour);
            }
        }

        if (getContainsCheckbox.isSelected()) {
            String xText = getContainsXField.getText().trim();
            String yText = getContainsYField.getText().trim();
            if (xText.isEmpty() || yText.isEmpty()) {
                appendError("Contains filter requires both X and Y");
                return;
            }
            try {
                int x = Integer.parseInt(xText);
                int y = Integer.parseInt(yText);
                command.append(" contains=").append(x).append(" ").append(y);
            } catch (NumberFormatException e) {
                appendError("Contains X and Y must be integers");
                return;
            }
        }

        if (getRefersToCheckbox.isSelected()) {
            String refersTo = getRefersToField.getText().trim();
            if (refersTo.isEmpty()) {
                appendError("RefersTo cannot be empty when checked");
                return;
            }
            if (refersTo.contains(" ")) {
                appendError("RefersTo must be a single token (no spaces)");
                return;
            }
            command.append(" refersTo=").append(refersTo);
        }

        sendCommand(command.toString());
    }

    private void handleGetPins() {
        sendCommand("GET PINS");
    }

    private void handlePin() {
        String xText = pinXField.getText().trim();
        String yText = pinYField.getText().trim();

        if (xText.isEmpty() || yText.isEmpty()) {
            appendError("X and Y are required");
            return;
        }

        int x, y;
        try {
            x = Integer.parseInt(xText);
            y = Integer.parseInt(yText);
        } catch (NumberFormatException e) {
            appendError("X and Y must be integers");
            return;
        }

        sendCommand("PIN " + x + " " + y);
    }

    private void handleUnpin() {
        String xText = pinXField.getText().trim();
        String yText = pinYField.getText().trim();

        if (xText.isEmpty() || yText.isEmpty()) {
            appendError("X and Y are required");
            return;
        }

        int x, y;
        try {
            x = Integer.parseInt(xText);
            y = Integer.parseInt(yText);
        } catch (NumberFormatException e) {
            appendError("X and Y must be integers");
            return;
        }

        sendCommand("UNPIN " + x + " " + y);
    }

    private void handleShake() {
        sendCommand("SHAKE");
    }

    private void handleClear() {
        sendCommand("CLEAR");
    }

    private void sendCommand(String command) {
        setControlsEnabled(false);
        requestInProgress = true;
        appendInfo("> " + command);

        SwingWorker<Response, Void> worker = new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() throws Exception {
                return client.sendAndRead(command);
            }

            @Override
            protected void done() {
                try {
                    Response response = get();
                    handleResponse(response);
                } catch (Exception e) {
                    appendError("Command failed: " + e.getMessage());
                    if (!client.isConnected()) {
                        appendError("Server disconnected unexpectedly");
                        updateConnectionState(false);
                        handshake = null;
                        handshakeLabel.setText(" ");
                    }
                } finally {
                    requestInProgress = false;
                    setControlsEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void handleResponse(Response response) {
        if (response instanceof OkResponse) {
            appendInfo("< OK");
        } else if (response instanceof OkNoteResponse) {
            OkNoteResponse okNote = (OkNoteResponse) response;
            appendInfo("< OK NOTE " + okNote.noteId);
        } else if (response instanceof ErrorResponse) {
            ErrorResponse error = (ErrorResponse) response;
            appendError("< " + error.rawLine);
        } else if (response instanceof NotesListResponse) {
            NotesListResponse notesList = (NotesListResponse) response;
            appendInfo("< OK " + notesList.notes.size() + " notes:");
            appendInfo(String.format("  %-4s %-4s %-4s %-10s %-10s %s",
                                    "ID", "X", "Y", "Colour", "Status", "Message"));
            appendInfo("  " + "-".repeat(60));
            for (NoteRecord note : notesList.notes) {
                appendInfo(String.format("  %-4d %-4d %-4d %-10s %-10s %s",
                                        note.id, note.x, note.y, note.colour,
                                        note.pinStatus, note.message));
            }
            appendInfo("< END");
        } else if (response instanceof PinsListResponse) {
            PinsListResponse pinsList = (PinsListResponse) response;
            appendInfo("< OK " + pinsList.pins.size() + " pins:");
            for (PinRecord pin : pinsList.pins) {
                appendInfo("  PIN " + pin.x + " " + pin.y);
            }
            appendInfo("< END");
        }
    }

    private void populateColourDropdowns(List<String> colours) {
        postColourCombo.removeAllItems();
        getColourCombo.removeAllItems();

        for (String colour : colours) {
            postColourCombo.addItem(colour);
            getColourCombo.addItem(colour);
        }
    }

    private void updateConnectionState(boolean connected) {
        if (connected) {
            statusLabel.setText("Status: Connected");
            statusLabel.setForeground(new Color(0, 128, 0));
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
        } else {
            statusLabel.setText("Status: Not connected");
            statusLabel.setForeground(Color.RED);
            connectButton.setEnabled(true);
            disconnectButton.setEnabled(false);
        }
        setControlsEnabled(connected);
    }

    private void setControlsEnabled(boolean enabled) {
        boolean commandsEnabled = enabled && !requestInProgress && handshake != null;

        postButton.setEnabled(commandsEnabled);
        getNotesButton.setEnabled(commandsEnabled);
        getPinsButton.setEnabled(commandsEnabled);
        pinButton.setEnabled(commandsEnabled);
        unpinButton.setEnabled(commandsEnabled);
        shakeButton.setEnabled(commandsEnabled);
        clearButton.setEnabled(commandsEnabled);

        if (!enabled || handshake == null) {
            postColourCombo.setEnabled(false);
            getColourCombo.setEnabled(false);
        } else {
            postColourCombo.setEnabled(commandsEnabled);
            getColourCombo.setEnabled(commandsEnabled);
        }
    }

    private void appendInfo(String message) {
        outputArea.append(message + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    private void appendError(String message) {
        outputArea.append("ERROR: " + message + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }
}
