import java.io.*;
import java.net.*;
import java.util.*;

public class ProtocolClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public Handshake connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Read HELLO line
        String helloLine = in.readLine();
        if (helloLine == null) {
            throw new IOException("Server closed connection before sending HELLO");
        }

        return parseHandshake(helloLine);
    }

    private Handshake parseHandshake(String line) throws IOException {
        // Expected: HELLO <board_w> <board_h> <note_w> <note_h> COLOURS <k> <colour1> ... <colourk>
        String[] tokens = line.split("\\s+");
        if (tokens.length < 7 || !tokens[0].equals("HELLO") || !tokens[5].equals("COLOURS")) {
            throw new IOException("Invalid HELLO format: " + line);
        }

        try {
            int boardW = Integer.parseInt(tokens[1]);
            int boardH = Integer.parseInt(tokens[2]);
            int noteW = Integer.parseInt(tokens[3]);
            int noteH = Integer.parseInt(tokens[4]);
            int k = Integer.parseInt(tokens[6]);

            List<String> colours = new ArrayList<>();
            for (int i = 7; i < 7 + k && i < tokens.length; i++) {
                colours.add(tokens[i]);
            }

            return new Handshake(boardW, boardH, noteW, noteH, colours);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid HELLO numeric values: " + line);
        }
    }

    public void disconnect() throws IOException {
        if (out != null) {
            out.println("DISCONNECT");
        }
        if (in != null) {
            // Read OK response
            in.readLine();
        }
        close();
    }

    public Response sendAndRead(String commandLine) throws IOException {
        if (out == null || in == null) {
            throw new IOException("Not connected");
        }

        out.println(commandLine);

        String responseLine = in.readLine();
        if (responseLine == null) {
            throw new IOException("Server closed connection");
        }

        return parseResponse(responseLine, commandLine);
    }

    private Response parseResponse(String line, String sentCommand) throws IOException {
        if (line.startsWith("ERROR")) {
            // ERROR <CODE> <description...>
            String[] tokens = line.split("\\s+", 3);
            String code = tokens.length > 1 ? tokens[1] : "";
            String description = tokens.length > 2 ? tokens[2] : "";
            return new ErrorResponse(code, description, line);
        }

        if (line.equals("OK")) {
            return new OkResponse();
        }

        if (line.startsWith("OK NOTE")) {
            // OK NOTE <note_id>
            String[] tokens = line.split("\\s+");
            if (tokens.length >= 3) {
                int noteId = Integer.parseInt(tokens[2]);
                return new OkNoteResponse(noteId);
            }
            throw new IOException("Invalid OK NOTE format: " + line);
        }

        if (line.startsWith("OK ")) {
            // Multi-line list: OK <count>
            String[] tokens = line.split("\\s+");
            int count = Integer.parseInt(tokens[1]);

            // Determine if this is notes or pins based on command
            if (sentCommand.startsWith("GET PINS")) {
                return readPinsList(count);
            } else {
                return readNotesList(count);
            }
        }

        throw new IOException("Unexpected response: " + line);
    }

    private NotesListResponse readNotesList(int count) throws IOException {
        List<NoteRecord> notes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String line = in.readLine();
            if (line == null) {
                throw new IOException("Unexpected end of stream while reading notes");
            }
            notes.add(parseNoteRecord(line));
        }

        // Read END line
        String endLine = in.readLine();
        if (endLine == null || !endLine.equals("END")) {
            throw new IOException("Expected END, got: " + endLine);
        }

        return new NotesListResponse(notes);
    }

    private PinsListResponse readPinsList(int count) throws IOException {
        List<PinRecord> pins = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String line = in.readLine();
            if (line == null) {
                throw new IOException("Unexpected end of stream while reading pins");
            }
            pins.add(parsePinRecord(line));
        }

        // Read END line
        String endLine = in.readLine();
        if (endLine == null || !endLine.equals("END")) {
            throw new IOException("Expected END, got: " + endLine);
        }

        return new PinsListResponse(pins);
    }

    private NoteRecord parseNoteRecord(String line) throws IOException {
        // NOTE <id> <x> <y> <colour> <PINNED|UNPINNED> <message...optional>
        String[] tokens = line.split("\\s+", 7);
        if (tokens.length < 6 || !tokens[0].equals("NOTE")) {
            throw new IOException("Invalid NOTE format: " + line);
        }

        int id = Integer.parseInt(tokens[1]);
        int x = Integer.parseInt(tokens[2]);
        int y = Integer.parseInt(tokens[3]);
        String colour = tokens[4];
        String pinStatus = tokens[5];
        String message = tokens.length > 6 ? tokens[6] : "";

        return new NoteRecord(id, x, y, colour, pinStatus, message);
    }

    private PinRecord parsePinRecord(String line) throws IOException {
        // PIN <x> <y>
        String[] tokens = line.split("\\s+");
        if (tokens.length < 3 || !tokens[0].equals("PIN")) {
            throw new IOException("Invalid PIN format: " + line);
        }

        int x = Integer.parseInt(tokens[1]);
        int y = Integer.parseInt(tokens[2]);

        return new PinRecord(x, y);
    }

    public void close() {
        try {
            if (in != null) in.close();
        } catch (IOException e) {
            // Ignore
        }
        try {
            if (out != null) out.close();
        } catch (Exception e) {
            // Ignore
        }
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            // Ignore
        }
        socket = null;
        in = null;
        out = null;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}
