package se.lu.ics.exception;

/**
 * Exception thrown when there is an error accessing data.
 * This is a runtime exception to avoid forcing callers to handle checked exceptions.
 */
public class DataAccessException extends RuntimeException {
    
    /**
     * Constructor with error message
     * @param message The error message
     */
    public DataAccessException(String message) {
        super(message);
    }
    
    /**
     * Constructor with error message and cause
     * @param message The error message
     * @param cause The underlying cause of the exception
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
} 