import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Bulletin Board Server
 * 
 * A multithreaded TCP server that manages a shared bulletin board where multiple concurrent clients can post, query, pin, and manage notes.
 * 
 * Usage:
 *   java BulletinBoardServer <port> <board_width> <board_height> <note_width> <note_height> <colour1> ... <colourN>
 * 
 * Example:
 *   java BulletinBoardServer 4554 200 100 20 10 red white green yellow
 * 
 * The server:
 * - Listens on the specified port for client connections.
 * - Spawns a new thread for each connected client.
 * - Maintains a shared, synchronized bulletin board state.
 * - Supports atomic operations (POST, SHAKE, CLEAR).
 * - Never crashes due to client errors.
 */
public class BulletinBoardServer {

    public static void main(String[] args) {
        // Validate command-line arguments.
        if (args.length < 6) {
            printUsageAndExit();
        }

        try {
            // Parse port.
            int port = Integer.parseInt(args[0]);
            if (port < 1 || port > 65535) {
                System.err.println("Error: Port must be between 1 and 65535.");
                System.exit(1);
            }

            // Parse board dimensions.
            int boardWidth = Integer.parseInt(args[1]);
            if (boardWidth < 1) {
                System.err.println("Error: 'Board Width' must be at least 1.");
                System.exit(1);
            }

            int boardHeight = Integer.parseInt(args[2]);
            if (boardHeight < 1) {
                System.err.println("Error: 'Board Height' must be at least 1.");
                System.exit(1);
            }

            // Parse note dimensions.
            int noteWidth = Integer.parseInt(args[3]);
            if (noteWidth < 1) {
                System.err.println("Error: 'Note Width' must be at least 1.");
                System.exit(1);
            }

            int noteHeight = Integer.parseInt(args[4]);
            if (noteHeight < 1) {
                System.err.println("Error: 'Note Height' must be at least 1.");
                System.exit(1);
            }

            // Parse colours.
            Set<String> colours = new HashSet<>();
            for (int i = 5; i < args.length; i++) {
                colours.add(args[i]);
            }

            if (colours.size() < 1) {
                System.err.println("Error: At least one colour must be specified.");
                System.exit(1);
            }

            // Validate note fits on board.
            if (noteWidth > boardWidth || noteHeight > boardHeight) {
                System.err.println("Error: Note dimensions must fit within board dimensions.");
                System.exit(1);
            }

            // Create the shared board.
            BoardState board = new BoardState(boardWidth, boardHeight, noteWidth, noteHeight, colours);

            // Print configuration.
            System.out.println("Server starting on port " + port);
            System.out.println("Board: " + boardWidth + "x" + boardHeight + ", Notes: " + noteWidth + "x" + noteHeight);
            System.out.println("Colours: " + colours);

            // Start the server.
            startServer(port, board);

        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Starts the server and listens for client connections.
     * Creates a new thread for each client that connects.
     */
    private static void startServer(int port, BoardState board) throws IOException {
        ServerSocket serverSocket = null;
        int clientCounter = 0;

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("\nServer started successfully.");
            System.out.println("Listening on port " + port);
            System.out.println("Waiting for client connections...\n");

            // Accept client connections in a loop.
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientCounter++;
                    
                    // Create and start a new thread for this client.
                    ClientConnection handler = new ClientConnection(clientSocket, board, clientCounter);
                    Thread thread = new Thread(handler);
                    thread.start();
                    
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                    // Continue accepting other connections.
                }
            }

        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Server socket closed.");
            }
        }
    }



    /**
     * Prints usage information and exits.
     */
    private static void printUsageAndExit() {
        System.err.println("\nUsage: java BulletinBoardServer <port> <board_width> <board_height> <note_width> <note_height> <colour1> ... <colourN>");
        System.err.println("\nParameters:");
        System.err.println("  port          - Port number (1-65535, default: 4321)");
        System.err.println("  board_width   - Width of the bulletin board (positive integer)");
        System.err.println("  board_height  - Height of the bulletin board (positive integer)");
        System.err.println("  note_width    - Width of each note (positive integer)");
        System.err.println("  note_height   - Height of each note (positive integer)");
        System.err.println("  colour1...N    - At least one colour name (space-separated)");
        System.err.println("\nExamples:");
        System.err.println("  java BulletinBoardServer 4321 200 100 20 10 red white green yellow");
        System.err.println("  java BulletinBoardServer 4554 200 100 20 10 red white green yellow");
        System.err.println();
        System.exit(1);
    }
}
