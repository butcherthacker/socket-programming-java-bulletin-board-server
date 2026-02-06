public class NoteRecord {
    public final int id;
    public final int x;
    public final int y;
    public final String colour;
    public final String pinStatus;
    public final String message;

    public NoteRecord(int id, int x, int y, String colour, String pinStatus, String message) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.colour = colour;
        this.pinStatus = pinStatus;
        this.message = message;
    }
}
