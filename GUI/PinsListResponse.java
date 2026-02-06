import java.util.List;

public class PinsListResponse extends Response {
    public final List<PinRecord> pins;

    public PinsListResponse(List<PinRecord> pins) {
        this.pins = pins;
    }
}
