package ijse.cmjd.cw3.exception;

/**
 * Thrown when attempting to create or update a resource that violates a uniqueness constraint (HTTP 409).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
