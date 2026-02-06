public class ErrorResponse extends Response {
    public final String code;
    public final String description;
    public final String rawLine;

    public ErrorResponse(String code, String description, String rawLine) {
        this.code = code;
        this.description = description;
        this.rawLine = rawLine;
    }
}
