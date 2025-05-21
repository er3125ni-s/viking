package se.lu.ics.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import se.lu.ics.exception.DataAccessException;

/**
 * Represents an applicant for a job position.
 */
public class Applicant {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate applicationDate;
    private int rank;

    /**
     * Constructor for a new applicant
     * @param firstName The applicant's first name
     * @param lastName The applicant's last name
     * @param email The applicant's email
     * @param phone The applicant's phone number
     */
    public Applicant(String firstName, String lastName, String email, String phone) {
        this.id = "APP-" + UUID.randomUUID().toString().substring(0, 8);
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.applicationDate = LocalDate.now();
        this.rank = 0;
    }

    /**
     * Constructor with all fields
     * @param id The applicant ID
     * @param firstName The applicant's first name
     * @param lastName The applicant's last name
     * @param email The applicant's email
     * @param phone The applicant's phone number
     * @param applicationDate The application date
     * @param rank The applicant's rank
     */
    public Applicant(String id, String firstName, String lastName, String email, String phone, 
                    LocalDate applicationDate, int rank) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.applicationDate = applicationDate;
        this.rank = rank;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    /**
     * Updates the applicant's rank
     * @param newRank The new rank to set (1-5, where 5 is highest)
     * @throws IllegalArgumentException if the rank is not between 1 and 5
     */
    public void updateRank(int newRank) {
        if (newRank < 0 || newRank > 5) {
            throw new IllegalArgumentException("Rank must be between 0 and 5");
        }
        this.rank = newRank;
    }

    /**
     * Get the rank as a descriptive string
     * @return A string description of the rank
     */
    public String getRankDescription() {
        switch (rank) {
            case 0: return "Not ranked";
            case 1: return "Poor fit";
            case 2: return "Below average";
            case 3: return "Average";
            case 4: return "Good fit";
            case 5: return "Excellent fit";
            default: return "Unknown";
        }
    }

    /**
     * Get the full name of the applicant
     * @return The applicant's full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Static method to find applicants for a specific recruitment
     * @param conn The database connection
     * @param recruitment The recruitment to find applicants for
     * @return List of applicants for the recruitment
     */
    public static List<Applicant> findByRecruitment(Connection conn, Recruitment recruitment) {
        List<Applicant> applicants = new ArrayList<>();
        String sql = "SELECT a.* FROM applicants a " +
                     "JOIN applications app ON a.id = app.applicant_id " +
                     "WHERE app.recruitment_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, recruitment.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String id = rs.getString("id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                LocalDate applicationDate = rs.getDate("application_date").toLocalDate();
                int rank = rs.getInt("rank");

                applicants.add(new Applicant(id, firstName, lastName, email, phone, applicationDate, rank));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding applicants for recruitment: " + recruitment.getId(), e);
        }
        
        return applicants;
    }
    
    /**
     * Save the applicant to the database (insert or update)
     * @param conn The database connection
     * @return true if successful, false otherwise
     */
    public boolean save(Connection conn) {
        try {
            // Check if the applicant exists
            String checkSql = "SELECT COUNT(*) FROM applicants WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setString(1, id);
                ResultSet rs = pstmt.executeQuery();
                rs.next();
                int count = rs.getInt(1);
                
                if (count == 0) {
                    // Insert
                    String insertSql = "INSERT INTO applicants (id, first_name, last_name, email, phone, application_date, rank) " +
                                       "VALUES (?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, id);
                        insertStmt.setString(2, firstName);
                        insertStmt.setString(3, lastName);
                        insertStmt.setString(4, email);
                        insertStmt.setString(5, phone);
                        insertStmt.setDate(6, java.sql.Date.valueOf(applicationDate));
                        insertStmt.setInt(7, rank);
                        return insertStmt.executeUpdate() > 0;
                    }
                } else {
                    // Update
                    String updateSql = "UPDATE applicants SET first_name = ?, last_name = ?, email = ?, " +
                                      "phone = ?, application_date = ?, rank = ? WHERE id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, firstName);
                        updateStmt.setString(2, lastName);
                        updateStmt.setString(3, email);
                        updateStmt.setString(4, phone);
                        updateStmt.setDate(5, java.sql.Date.valueOf(applicationDate));
                        updateStmt.setInt(6, rank);
                        updateStmt.setString(7, id);
                        return updateStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error saving applicant: " + getFullName(), e);
        }
    }
    
    /**
     * Delete the applicant from the database
     * @param conn The database connection
     * @return true if successful, false otherwise
     */
    public boolean delete(Connection conn) {
        try {
            String sql = "DELETE FROM applicants WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, id);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting applicant: " + getFullName(), e);
        }
    }
    
    @Override
    public String toString() {
        return getFullName();
    }
}
