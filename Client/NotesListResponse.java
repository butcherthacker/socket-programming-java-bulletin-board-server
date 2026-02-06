import java.util.List;

public class NotesListResponse extends Response {
    public final List<NoteRecord> notes;

    public NotesListResponse(List<NoteRecord> notes) {
        this.notes = notes;
    }
}
