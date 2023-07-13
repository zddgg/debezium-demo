package io.debezium.text;

public class ParsingException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final Position position;

    public ParsingException(Position position) {
        this.position = position;
    }

    public ParsingException(Position position, String message, Throwable cause) {
        super(message, cause);
        this.position = position;
    }

    public ParsingException(Position position, String message) {
        super(message);
        this.position = position;
    }

    public Position getPosition() {
        return this.position;
    }
}
