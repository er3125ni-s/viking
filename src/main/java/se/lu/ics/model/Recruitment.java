package se.lu.ics.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;

import se.lu.ics.exception.DataAccessException;

/**
 * Represents a recruitment process for a job position.
 */
public class Recruitment {
    private String id;
    private Role role;
    private LocalDate postingDate;
    private LocalDate applicationDeadline;
    private LocalDate offerAcceptanceDate;
    private RecruitmentStatus status;
    
    // Transient field to store applicant count
    private transient int applicantCount = 0;
    
    // Store the current year's recruitment counter
    private static Map<Integer, Integer> yearCounters = new HashMap<>();

    /**
     * Constructor for a new recruitment
     * @param role The role being recruited for
     * @param applicationDeadline The deadline for applications
     */
    public Recruitment(Role role, LocalDate applicationDeadline) {
        this.id = generateId();
        this.role = role;
        this.postingDate = LocalDate.now();
        this.applicationDeadline = applicationDeadline;
        this.status = RecruitmentStatus.OPEN;
    }

    /**
     * Constructor with all fields
     * @param id The recruitment ID
     * @param role The role being recruited for
     * @param postingDate The date the position was posted
     * @param applicationDeadline The deadline for applications
     * @param offerAcceptanceDate The date an offer was accepted (can be null)
     * @param status The status of the recruitment
     */
    public Recruitment(String id, Role role, LocalDate postingDate, 
                      LocalDate applicationDeadline, LocalDate offerAcceptanceDate, 
                      RecruitmentStatus status) {
        this.id = id;
        this.role = role;
        this.postingDate = postingDate;
        this.applicationDeadline = applicationDeadline;
        this.offerAcceptanceDate = offerAcceptanceDate;
        this.status = status;
    }
    
    /**
     * Generate a new recruitment ID with format HR yyyy/x
     * @return The generated ID
     */
    private String generateId() {
        int year = Year.now().getValue();
        
        // Get the current counter for this year, or initialize to 0 if not present
        int counter = yearCounters.getOrDefault(year, 0);
        
        // Increment the counter
        counter++;
        
        // Update the counter in the map
        yearCounters.put(year, counter);
        
        // Format the ID
        return String.format("HR %d/%d", year, counter);
    }
    
    /**
     * Static method to initialize the year counters from existing recruitments in the database
     * @param conn Database connection
     */
    public static void initializeYearCounters(Connection conn) {
        try {
            // Clear any existing counters
            yearCounters.clear();
            
            // Query to get the max counter for each year
            String sql = "SELECT SUBSTRING(id, 4, 4) as year, " +
                         "MAX(CAST(SUBSTRING(id FROM POSITION('/' IN id) + 1) AS INTEGER)) as max_counter " +
                         "FROM recruitments GROUP BY year";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    int year = rs.getInt("year");
                    int maxCounter = rs.getInt("max_counter");
                    yearCounters.put(year, maxCounter);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error initializing recruitment counters", e);
        }
    }

    /**
     * Set default counter values for the current year
     */
    public static void setDefaultCounters() {
        int currentYear = Year.now().getValue();
        yearCounters.put(currentYear, 0); // Start with 0, will be incremented on first use
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDate getPostingDate() {
        return postingDate;
    }

    public void setPostingDate(LocalDate postingDate) {
        this.postingDate = postingDate;
    }

    public LocalDate getApplicationDeadline() {
        return applicationDeadline;
    }

    public void setApplicationDeadline(LocalDate applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }

    public LocalDate getOfferAcceptanceDate() {
        return offerAcceptanceDate;
    }

    public void setOfferAcceptanceDate(LocalDate offerAcceptanceDate) {
        this.offerAcceptanceDate = offerAcceptanceDate;
    }

    public RecruitmentStatus getStatus() {
        return status;
    }

    public void setStatus(RecruitmentStatus status) {
        this.status = status;
    }

    /**
     * Close the recruitment without hiring
     */
    public void close() {
        this.status = RecruitmentStatus.CLOSED;
    }

    /**
     * Complete the recruitment with a successful hire
     * @param offerAcceptanceDate The date the offer was accepted
     */
    public void complete(LocalDate offerAcceptanceDate) {
        this.status = RecruitmentStatus.COMPLETED;
        this.offerAcceptanceDate = offerAcceptanceDate;
    }

    /**
     * Get the number of applicants for this recruitment
     * @return The number of applicants
     */
    public int getApplicantCount() {
        return applicantCount;
    }

    /**
     * Set the number of applicants for this recruitment
     * @param count The number of applicants
     */
    public void setApplicantCount(int count) {
        this.applicantCount = count;
    }

    /**
     * Delete the recruitment from the database
     * @param conn The database connection
     * @return true if successful, false otherwise
     */
    public boolean delete(Connection conn) {
        try {
            String sql = "DELETE FROM recruitments WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, id);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting recruitment: " + id, e);
        }
    }
    
    @Override
    public String toString() {
        return String.format("%s - %s (%s)", id, role.getTitle(), status);
    }
}
