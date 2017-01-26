package pl.edu.agh.kis.florist.exceptions;

/**
 * Created by bzdeco on 12.01.17.
 */
public class InvalidPathException extends RuntimeException {
    public InvalidPathException() {
        super();
    }

    public InvalidPathException(String msg) {
        super(msg);
    }
}
