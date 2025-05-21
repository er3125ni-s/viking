package se.lu.ics.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service class for database operations using SQLite
 */
public class DatabaseService {
    private static final String DB_URL = "jdbc:sqlite:vikingexpress.db";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private Connection connection;
    
    /**
     * Initialize the database connection and create tables if they don't exist
     */
    public DatabaseService() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            createTables();
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create database tables if they don't exist
     */
    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create Roles table
            stmt.execute("CREATE TABLE IF NOT EXISTS roles (" +
                    "id TEXT PRIMARY KEY, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT, " +
                    "department TEXT)");
            
            // Create Recruitments table
            stmt.execute("CREATE TABLE IF NOT EXISTS recruitments (" +
                    "id TEXT PRIMARY KEY, " +
                    "role_id TEXT NOT NULL, " +
                    "application_deadline TEXT NOT NULL, " +
                    "posting_date TEXT NOT NULL, " +
                    "offer_acceptance_date TEXT, " +
                    "status TEXT NOT NULL, " +
                    "FOREIGN KEY (role_id) REFERENCES roles(id))");
            
            // Create Applicants table
            stmt.execute("CREATE TABLE IF NOT EXISTS applicants (" +
                    "id TEXT PRIMARY KEY, " +
                    "first_name TEXT NOT NULL, " +
                    "last_name TEXT NOT NULL, " +
                    "email TEXT NOT NULL, " +
                    "phone TEXT, " +
                    "application_date TEXT NOT NULL, " +
                    "rank INTEGER DEFAULT 0)");
            
            // Create Applications junction table
            stmt.execute("CREATE TABLE IF NOT EXISTS applications (" +
                    "applicant_id TEXT NOT NULL, " +
                    "recruitment_id TEXT NOT NULL, " +
                    "application_date TEXT NOT NULL, " +
                    "PRIMARY KEY (applicant_id, recruitment_id), " +
                    "FOREIGN KEY (applicant_id) REFERENCES applicants(id), " +
                    "FOREIGN KEY (recruitment_id) REFERENCES recruitments(id))");
            
            // Create Interviews table
            stmt.execute("CREATE TABLE IF NOT EXISTS interviews (" +
                    "id TEXT PRIMARY KEY, " +
                    "recruitment_id TEXT NOT NULL, " +
                    "applicant_id TEXT NOT NULL, " +
                    "date_time TEXT NOT NULL, " +
                    "location TEXT, " +
                    "interviewer TEXT, " +
                    "status TEXT NOT NULL, " +
                    "notes TEXT, " +
                    "FOREIGN KEY (recruitment_id) REFERENCES recruitments(id), " +
                    "FOREIGN KEY (applicant_id) REFERENCES applicants(id))");
        }
    }
    
    /**
     * Close the database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
    
    /**
     * Get a database connection
     * @return Connection object
     */
    public Connection getConnection() {
        return connection;
    }
    
    /**
     * Convert LocalDate to String for database storage
     */
    public static String dateToString(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }
    
    /**
     * Convert LocalDateTime to String for database storage
     */
    public static String dateTimeToString(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : null;
    }
    
    /**
     * Convert String to LocalDate from database
     */
    public static LocalDate stringToDate(String dateStr) {
        return dateStr != null ? LocalDate.parse(dateStr, DATE_FORMATTER) : null;
    }
    
    /**
     * Convert String to LocalDateTime from database
     */
    public static LocalDateTime stringToDateTime(String dateTimeStr) {
        return dateTimeStr != null ? LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER) : null;
    }
} 