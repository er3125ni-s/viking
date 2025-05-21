package se.lu.ics.dao;

/**
 * Runtime exception thrown when a DAO encounters a data access error.
 * This serves as the base exception for all data access related exceptions.
 */
public class DataAccessException extends RuntimeException {
    
    /**
     * Constructor with message and cause
     * 
     * @param message Message describing the error
     * @param cause The underlying cause (typically SQLException)
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructor with message only
     * 
     * @param message Message describing the error
     */
    public DataAccessException(String message) {
        super(message);
    }
}