import java.util.*;

/**
 * BoardState - Manages the bulletin board state with thread-safe operations.
 *
 * The board class manages:
 * - Note storage and retrieval.
 * - Pin management.
 * - Pin and note validation (bounds checking, overlap detection).
 * - Atomic operations (POST, SHAKE, CLEAR).
 * - Thread-safe concurrent access using synchronized methods.
 */
public class BoardState {
    private final int width;
    private final int height;
    private final int noteWidth;
    private final int noteHeight;
    private final Set<String> validColours;
    
    private final Map<Integer, Note> notes; // note_id -> Note.
    private final Set<String> pins; // Set of "x,y" coordinate strings.
    private int nextNoteId;

    /**
     * Creates a new Board.
     * 
     * @param width Board width.
     * @param height Board height.
     * @param noteWidth Fixed note width.
     * @param noteHeight Fixed note height.
     * @param validColours Set of valid colour names.
     */
    public BoardState(int width, int height, int noteWidth, int noteHeight, Set<String> validColours) {
        this.width = width;
        this.height = height;
        this.noteWidth = noteWidth;
        this.noteHeight = noteHeight;
        this.validColours = validColours;
        
        this.notes = new HashMap<>();
        this.pins = new HashSet<>();
        this.nextNoteId = 1;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getNoteWidth() {
        return noteWidth;
    }

    public int getNoteHeight() {
        return noteHeight;
    }

    public Set<String> getValidColours() {
        return validColours;
    }

    /**
     * Posts a new note to the board.
     * 
     * @param x X-coordinate of upper-left corner.
     * @param y Y-coordinate of upper-left corner.
     * @param colour Note colour.
     * @param message Note message.
     * @return The ID of the newly created note.
     * @throws IllegalArgumentException if validation fails.
     */
    public synchronized int postNote(int x, int y, String colour, String message) throws IllegalArgumentException {
        // Validate colour.
        if (!validColours.contains(colour)) {
            throw new IllegalArgumentException("COLOUR_NOT_SUPPORTED Colour '" + colour + "' is not supported.");
        }
        
        // Validate bounds.
        if (x < 0 || y < 0 || x + noteWidth > width || y + noteHeight > height) {
            throw new IllegalArgumentException("OUT_OF_BOUNDS Note does not fit within board boundaries.");
        }
        
        // Check for complete overlap (notes have same coordinates).
        for (Note existingNote : notes.values()) {
            if (existingNote.getX() == x && existingNote.getY() == y) {
                throw new IllegalArgumentException("OVERLAP_ERROR Note completely overlaps existing note at (" + x + "," + y + ").");
            }
        }
        
        // Create and add the note.
        int noteId = nextNoteId++;
        Note note = new Note(noteId, x, y, colour, message);
        notes.put(noteId, note);
        
        return noteId;
    }

    /**
     * Places a pin at the specified coordinate.
     * 
     * @param x X-coordinate of pin.
     * @param y Y-coordinate of pin.
     * @throws IllegalArgumentException if validation fails.
     */
    public synchronized void placePin(int x, int y) throws IllegalArgumentException {
        // Validate board boundaries.
        if (x < 0 || y < 0 || x >= width || y >= height) {
            throw new IllegalArgumentException("OUT_OF_BOUNDS Pin coordinate is outside board boundaries.");
        }
        
        // Check if any notes contain this coordinate.
        boolean foundNote = false;
        for (Note note : notes.values()) {
            if (note.contains(x, y, noteWidth, noteHeight)) {
                foundNote = true;
                break;
            }
        }
        
        // Must pin at least one note.
        if (!foundNote) {
            throw new IllegalArgumentException("NO_NOTE_AT_COORDINATE No note found at coordinate (" + x + "," + y + ").");
        }
        
        // Add pin globally and to all affected notes.
        String pinKey = x + "," + y;
        if (!pins.contains(pinKey)) {
            pins.add(pinKey);
            for (Note note : notes.values()) {
                if (note.contains(x, y, noteWidth, noteHeight)) {
                    note.addPin(x, y);
                }
            }
        }
    }

    /**
     * Removes a pin at the specified coordinate.
     * 
     * @param x X-coordinate of pin.
     * @param y Y-coordinate of pin.
     * @throws IllegalArgumentException if validation fails.
     */
    public synchronized void removePin(int x, int y) throws IllegalArgumentException {
        // Validate board boundaries.
        if (x < 0 || y < 0 || x >= width || y >= height) {
            throw new IllegalArgumentException("OUT_OF_BOUNDS Pin coordinate is outside board boundaries.");
        }
        
        String pinKey = x + "," + y;
        
        // Check if pin exists.
        if (!pins.contains(pinKey)) {
            throw new IllegalArgumentException("PIN_NOT_FOUND No pin exists at coordinate (" + x + "," + y + ").");
        }
        
        // Remove pin globally and from all notes.
        pins.remove(pinKey);
        for (Note note : notes.values()) {
            if (note.contains(x, y, noteWidth, noteHeight)) {
                note.removePin(x, y);
            }
        }
    }

    /**
     * Removes all unpinned notes from the board.
     */
    public synchronized void shake() {
        // Remove all unpinned notes.
        Iterator<Map.Entry<Integer, Note>> it = notes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Note> entry = it.next();
            if (!entry.getValue().isPinned()) {
                it.remove();
            }
        }
    }

    /**
     * Removes all notes and pins from the board.
     */
    public synchronized void clear() {
        notes.clear();
        pins.clear();
    }

    /**
     * Queries notes based on filter criteria.
     * 
     * @param colourFilter Optional colour filter (null = no filter).
     * @param containsX X-coordinate for contains filter (-1 = no filter).
     * @param containsY Y-coordinate for contains filter (-1 = no filter).
     * @param refersTo Optional substring filter (null = no filter).
     * @return List of matching notes.
     */
    public synchronized List<Note> queryNotes(String colourFilter, int containsX, int containsY, String refersTo) {
        List<Note> results = new ArrayList<>();
        
        for (Note note : notes.values()) {
            boolean matches = true;
            
            // Apply colour filter.
            if (colourFilter != null && !note.getColour().equals(colourFilter)) {
                matches = false;
            }
            
            // Apply contains filter.
            if (matches && containsX != -1 && containsY != -1) {
                if (!note.contains(containsX, containsY, noteWidth, noteHeight)) {
                    matches = false;
                }
            }
            
            // Apply refersTo filter.
            if (matches && refersTo != null) {
                if (!note.getMessage().contains(refersTo)) {
                    matches = false;
                }
            }
            
            if (matches) {
                results.add(note);
            }
        }
        
        return results;
    }

    /**
     * Gets all pin coordinates.
     * 
     * @return List of pin coordinate pairs [x, y].
     */
    public synchronized List<int[]> getPins() {
        List<int[]> result = new ArrayList<>();
        for (String pinKey : pins) {
            String[] parts = pinKey.split(",");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            result.add(new int[]{x, y});
        }
        return result;
    }
}