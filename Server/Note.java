import java.util.HashSet;
import java.util.Set;

/**
 * Notes are rectangular objects that are placed such that they completely fit within the boundaries of the board and do not completely overlap. 
 * 
 * Notes contain:
 * - A unique ID assigned by the server.
 * - A unique (x, y) coordinate for its upper-left corner.
 * - A colour that has been selected from the server’s list of supported colours (defined at startup).
 * - A text message string.
 * - Potential set of pin coordinates for any pins 'pinning' the note.
 */
public class Note {
    private final int id;
    private final int x;
    private final int y;
    private final String colour;
    private final String message;
    private final Set<String> pins; // "X, Y" coordinates of pins on the note.

    /**
     * Creates a new Note.
     * 
     * @param id Unique server-assigned identifier.
     * @param x X-coordinate of upper-left corner.
     * @param y Y-coordinate of upper-left corner.
     * @param colour Note colour (must be valid).
     * @param message Note message content.
     */
    public Note(int id, int x, int y, String colour, String message) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.colour = colour;
        if (message != null) {
            this.message = message;
        } else {
            this.message = "";
        }
        this.pins = new HashSet<>();
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getColour() {
        return colour;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Checks if this note contains the given coordinate.
     * 
     * @param px X-coordinate to check.
     * @param py Y-coordinate to check.
     * @param noteWidth Width of all notes.
     * @param noteHeight Height of all notes.
     * @return true if (px, py) is within this note's boundaries.
     */
    public boolean contains(int px, int py, int noteWidth, int noteHeight) {
        return px >= x && px < x + noteWidth && py >= y && py < y + noteHeight;
    }

    /**
     * Adds a pin at the specified coordinate to this note.
     * 
     * @param px Pin's X-coordinate.
     * @param py Pin's Y-coordinate.
     */
    public void addPin(int px, int py) {
        pins.add(px + "," + py);
    }

    /**
     * Removes a pin at the specified coordinate from this note.
     * 
     * @param px Pin's X-coordinate.
     * @param py Pin's Y-coordinate.
     */
    public void removePin(int px, int py) {
        pins.remove(px + "," + py);
    }

    /**
     * Checks if this note is 'pinned' (has at least one active pin).
     * 
     * 
     * @return true if the note has one or more pins.
     */
    public boolean isPinned() {
        return !pins.isEmpty();
    }

    /**
     * Returns the pin status as a protocol string.
     * 
     * @return "PINNED" or "UNPINNED".
     */
    public String getPinStatus() {
        return isPinned() ? "PINNED" : "UNPINNED";
    }

    /**
     * Protocol response line for note.
     * Format: NOTE <id> <x> <y> <colour> <pin_status> <message>
     * 
     * @return Protocol-formatted note string.
     */
    public String toProtocolString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NOTE ").append(id).append(" ");
        sb.append(x).append(" ").append(y).append(" ");
        sb.append(colour).append(" ").append(getPinStatus());
        if (!message.isEmpty()) sb.append(" ").append(message);
        return sb.toString();
    }
}
