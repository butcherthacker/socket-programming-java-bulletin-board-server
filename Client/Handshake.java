import java.util.List;

public class Handshake {
    public final int boardW;
    public final int boardH;
    public final int noteW;
    public final int noteH;
    public final List<String> colours;

    public Handshake(int boardW, int boardH, int noteW, int noteH, List<String> colours) {
        this.boardW = boardW;
        this.boardH = boardH;
        this.noteW = noteW;
        this.noteH = noteH;
        this.colours = colours;
    }
}
