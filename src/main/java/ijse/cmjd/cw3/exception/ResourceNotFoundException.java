package ijse.cmjd.cw3.exception;

/**
 * Thrown when a requested entity or resource does not exist (HTTP 404).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
