package ijse.cmjd.cw3.exception;

/**
 * Thrown when an authenticated user attempts to access or modify another user's resource (HTTP 403).
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
