package pl.edu.agh.kis.florist.exceptions;

/**
 * Created by bzdeco on 21.01.17.
 */
public class FileUploadSQLException extends RuntimeException {
    public FileUploadSQLException() {
        super();
    }

    public FileUploadSQLException(String msg) {
        super(msg);
    }

    public FileUploadSQLException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
