package pl.edu.agh.kis.florist.exceptions;

/**
 * Created by bzdeco on 23.01.17.
 */
public class InvalidUserException extends RuntimeException {
    public InvalidUserException() {
        super();
    }

    public InvalidUserException(String msg) {
        super(msg);
    }
}
