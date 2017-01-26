package pl.edu.agh.kis.florist.exceptions;

/**
 * Created by bzdeco on 26.01.17.
 */
public class AuthorizationRequiredException extends RuntimeException {
    public AuthorizationRequiredException() {
        super();
    }

    public AuthorizationRequiredException(String msg) {
        super(msg);
    }
}
