package se.lu.ics.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

import javax.sql.DataSource;

import se.lu.ics.exception.DataAccessException;

/**
 * Manages database transactions to ensure atomicity of operations.
 * Provides methods to execute multiple database operations within a single transaction.
 * Supports nested transactions by reusing the same connection for nested calls.
 */
public class TransactionManager {
    
    private final DataSource dataSource;
    private final ThreadLocal<Connection> currentConnection = new ThreadLocal<>();
    private final ThreadLocal<Integer> transactionLevel = new ThreadLocal<>();
    
    /**
     * Constructor that takes a datasource
     * @param dataSource The datasource to get connections from
     */
    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * Get a connection from the datasource or reuse existing one if in a transaction
     * @return A database connection
     * @throws DataAccessException If a database access error occurs
     */
    public Connection getConnection() {
        Connection conn = currentConnection.get();
        if (conn != null) {
            return conn; // Return the existing connection for this thread
        }
        
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get database connection", e);
        }
    }
    
    /**
     * Execute a function within a transaction
     * @param <R> The return type of the function
     * @param operation The function to execute
     * @return The result of the function
     * @throws DataAccessException If a database access error occurs
     */
    public <R> R executeInTransaction(Function<Connection, R> operation) {
        boolean isOutermostTransaction = false;
        Connection conn = currentConnection.get();
        
        // Check if this is the outermost transaction
        if (conn == null) {
            try {
                conn = dataSource.getConnection();
                conn.setAutoCommit(false);
                currentConnection.set(conn);
                transactionLevel.set(1);
                isOutermostTransaction = true;
            } catch (SQLException e) {
                closeConnection(conn);
                throw new DataAccessException("Failed to start transaction", e);
            }
        } else {
            // Nested transaction, increment level
            transactionLevel.set(transactionLevel.get() + 1);
        }
        
        try {
            R result = operation.apply(conn);
            
            // Only commit if this is the outermost transaction
            if (isOutermostTransaction) {
                try {
                    conn.commit();
                } catch (SQLException e) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        throw new DataAccessException("Failed to rollback transaction", ex);
                    }
                    throw new DataAccessException("Failed to commit transaction", e);
                }
            }
            
            return result;
        } catch (Exception e) {
            // Only rollback if this is the outermost transaction
            if (isOutermostTransaction) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new DataAccessException("Failed to rollback transaction", ex);
                }
            }
            
            if (e instanceof DataAccessException) {
                throw (DataAccessException) e;
            }
            throw new DataAccessException("Transaction failed", e);
        } finally {
            if (isOutermostTransaction) {
                closeConnection(conn);
                currentConnection.remove();
                transactionLevel.remove();
            } else {
                // Decrement transaction level for nested transactions
                transactionLevel.set(transactionLevel.get() - 1);
            }
        }
    }
    
    /**
     * Close a database connection
     * @param conn The connection to close
     */
    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // Just log the error, can't throw in finally block
                System.err.println("Failed to close connection: " + e.getMessage());
            }
        }
    }
} 