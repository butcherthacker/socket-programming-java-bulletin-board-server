import java.io.*;
import java.net.*;
import java.util.*;

/**
 * ClientConnection - Handles communication with a single connected client.
 * 
 * Each client connection runs in its own thread, processing commands.
 * and sending responses according to the Bulletin Board Protocol.
 */
public class ClientConnection implements Runnable {
    private final Socket clientSocket;
    private final BoardState board;
    private final int clientId;

    /**
     * Creates a new ClientConnection.
     * 
     * @param clientSocket The socket connected to the client.
     * @param board The shared bulletin board.
     * @param clientId Unique identifier for this client (for logging).
     */
    public ClientConnection(Socket clientSocket, BoardState board, int clientId) {
        this.clientSocket = clientSocket;
        this.board = board;
        this.clientId = clientId;
    }

    @Override
    public void run() {
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            // Set up I/O streams.
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            System.out.println("[Client " + clientId + "] Connected from: " + clientSocket.getInetAddress());

            // Send HELLO handshake message.
            String helloMessage = buildHelloMessage();
            out.println(helloMessage);
            System.out.println("[Client " + clientId + "] Sent: " + helloMessage);

            // Process client commands.
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                inputLine = inputLine.trim();
                System.out.println("[Client " + clientId + "] Received: " + inputLine);

                String response = processCommand(inputLine);
                out.println(response);
                System.out.println("[Client " + clientId + "] Sent: " + response);

                // Check for disconnect.
                if (inputLine.toUpperCase().startsWith("DISCONNECT")) {
                    break;
                }
            }

            System.out.println("[Client " + clientId + "] Disconnected.");

        } catch (IOException e) {
            System.err.println("[Client " + clientId + "] Error: " + e.getMessage() + ".");
        } finally {
            // Close resources
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                System.err.println("[Client " + clientId + "] Error closing resources: " + e.getMessage() + ".");
            }
        }
    }

    /**
     * Builds the HELLO handshake message.
     * Format: HELLO <board_w> <board_h> <note_w> <note_h> COLOURS <n> <colour 1> ... <colour n>.
     */
    private String buildHelloMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("HELLO ")
          .append(board.getWidth()).append(" ")
          .append(board.getHeight()).append(" ")
          .append(board.getNoteWidth()).append(" ")
          .append(board.getNoteHeight()).append(" ")
          .append("COLOURS ");
        
        Set<String> colours = board.getValidColours();
        sb.append(colours.size());
        for (String colour : colours) {
            sb.append(" ").append(colour);
        }
        
        return sb.toString();
    }

    /**
     * Processes a single client command and returns the response.
     * 
     * @param command The command string from the client.
     * @return The response string to send back.
     */
    private String processCommand(String command) {
        try {
            String[] tokens = command.split("\\s+");
            if (tokens.length == 0 || tokens[0].isEmpty()) {
                return "ERROR INVALID_FORMAT Empty command.";
            }

            String cmd = tokens[0].toUpperCase();

            switch (cmd) {
                case "POST":
                    return handlePost(command);
                case "GET":
                    return handleGet(command);
                case "PIN":
                    return handlePin(command);
                case "UNPIN":
                    return handleUnpin(command);
                case "SHAKE":
                    return handleShake(command);
                case "CLEAR":
                    return handleClear(command);
                case "DISCONNECT":
                    return handleDisconnect(command);
                default:
                    return "ERROR INVALID_FORMAT Unknown command: " + cmd;
            }
        } catch (Exception e) {
            return "ERROR INVALID_FORMAT " + e.getMessage();
        }
    }

    /**
     * Handles POST command.
     * Format: POST <x> <y> <colour> <message>.
     */
    private String handlePost(String command) {
        try {
            String[] parts = command.split("\\s+");
            
            if (parts.length < 4) {
                return "ERROR INVALID_FORMAT POST requires X and Y coordinates, and a colour.";
            }

            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            String colour = parts[3];

            // Manually find where the message starts to preserve its spaces.
            int messageStartIndex = command.indexOf(colour) + colour.length();
            String message = command.substring(messageStartIndex).trim();

            int noteId = board.postNote(x, y, colour, message);
            return "OK NOTE " + noteId;

        } catch (NumberFormatException e) {
            return "ERROR INVALID_INT " + e.getMessage();
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            // Message format: "ERROR_CODE Description."
            return "ERROR " + msg;
        }
    }

    /**
     * Handles GET command.
     * Formats: GET PINS or GET [colour=<c>] [contains=<x> <y>] [refersTo=<s>].
     */
    private String handleGet(String command) {
        try {
            String input = command.substring(3).trim();

            if (input.toUpperCase().equals("PINS")) {
                return handleGetPins();
            } else {
                return handleGetQuery(input);
            }
        } catch (Exception e) {
            return "ERROR INVALID_FORMAT " + e.getMessage();
        }
    }

    /**
     * Handles GET PINS command.
     */
    private String handleGetPins() {
        List<int[]> pins = board.getPins();
        StringBuilder sb = new StringBuilder();
        sb.append("OK ").append(pins.size()).append("\n");
        for (int[] pin : pins) {
            sb.append("PIN ").append(pin[0]).append(" ").append(pin[1]).append("\n");
        }
        sb.append("END");
        return sb.toString();
    }

    /**
     * Handles GET query with filters.
     */
    private String handleGetQuery(String filters) {
        try {
            String colourFilter = null;
            int containsX = -1;
            int containsY = -1;
            String refersTo = null;

            if (!filters.isEmpty()) {
                String[] parts = filters.split(" ");
                int i = 0;
                while (i < parts.length) {
                    String filter = parts[i];
                    
                    if (filter.startsWith("colour=")) {
                        colourFilter = filter.substring(7);
                        i++;
                    } else if (filter.startsWith("contains=")) {
                        String coords = filter.substring(9);
                        if (i + 1 < parts.length) {
                            containsX = Integer.parseInt(coords);
                            containsY = Integer.parseInt(parts[i + 1]);
                            i += 2;
                        } else {
                            return "ERROR INVALID_FORMAT contains= requires X and Y coordinates.";
                        }
                    } else if (filter.startsWith("refersTo=")) {
                        refersTo = filter.substring(9);
                        i++;
                    } else {
                        i++;
                    }
                }
            }

            List<Note> results = board.queryNotes(colourFilter, containsX, containsY, refersTo);
            
            StringBuilder sb = new StringBuilder();
            sb.append("OK ").append(results.size()).append("\n");
            for (Note note : results) {
                sb.append(note.toProtocolString()).append("\n");
            }
            sb.append("END");
            return sb.toString();

        } catch (NumberFormatException e) {
            return "ERROR INVALID_INT " + e.getMessage() + ".";
        } catch (Exception e) {
            return "ERROR INVALID_FORMAT " + e.getMessage() + ".";
        }
    }

    /**
     * Handles PIN command.
     * Format: PIN <x> <y>.
     */
    private String handlePin(String command) {
        try {
            String[] parts = command.split("\\s+");
            if (parts.length != 3) {
                return "ERROR INVALID_FORMAT PIN requires X and Y coordinates.";
            }

            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            board.placePin(x, y);
            return "OK";

        } catch (NumberFormatException e) {
            return "ERROR INVALID_INT " + e.getMessage() + ".";
        } catch (IllegalArgumentException e) {
            return "ERROR " + e.getMessage() + ".";
        }
    }

    /**
     * Handles UNPIN command.
     * Format: UNPIN <x> <y>
     */
    private String handleUnpin(String command) {
        try {
            String[] parts = command.split("\\s+");
            if (parts.length != 3) {
                return "ERROR INVALID_FORMAT UNPIN requires X and Y coordinates.";
            }

            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            board.removePin(x, y);
            return "OK";

        } catch (NumberFormatException e) {
            return "ERROR INVALID_INT " + e.getMessage() + ".";
        } catch (IllegalArgumentException e) {
            return "ERROR " + e.getMessage() + ".";
        }
    }

    /**
     * Handles SHAKE command.
     * Format: SHAKE
     */
    private String handleShake(String command) {
        String[] parts = command.split("\\s+");
        if (parts.length != 1) {
            return "ERROR INVALID_FORMAT SHAKE takes no arguments.";
        }

        board.shake();
        return "OK";
    }

    /**
     * Handles CLEAR command.
     * Format: CLEAR
     */
    private String handleClear(String command) {
        String[] parts = command.split("\\s+");
        if (parts.length != 1) {
            return "ERROR INVALID_FORMAT CLEAR takes no arguments.";
        }

        board.clear();
        return "OK";
    }

    /**
     * Handles DISCONNECT command.
     * Format: DISCONNECT
     */
    private String handleDisconnect(String command) {
        String[] parts = command.split("\\s+");
        if (parts.length != 1) {
            return "ERROR INVALID_FORMAT DISCONNECT takes no arguments.";
        }

        return "OK";
    }

}
