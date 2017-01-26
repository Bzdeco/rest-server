package pl.edu.agh.kis.florist.exceptions;

/**
 * Created by bzdeco on 23.01.17.
 */
public class FailedAuthenticationException extends RuntimeException {
    public FailedAuthenticationException() {
        super();
    }

    public FailedAuthenticationException(String msg) {
        super(msg);
    }
}
