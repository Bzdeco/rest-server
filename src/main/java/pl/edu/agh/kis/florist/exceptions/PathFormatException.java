package pl.edu.agh.kis.florist.exceptions;

/**
 * Created by bzdeco on 21.01.17.
 */
public class PathFormatException extends RuntimeException {
    public PathFormatException() {
        super();
    }

    public PathFormatException(String msg) {
        super(msg);
    }
}
