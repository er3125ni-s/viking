package se.lu.ics.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import se.lu.ics.exception.DataAccessException;
import se.lu.ics.model.Applicant;

/**
 * JDBC implementation of the ApplicantDao interface.
 */
public class ApplicantDaoJdbc implements ApplicantDao {
    
    private final TransactionManager transactionManager;
    
    /**
     * Constructor that takes a transaction manager
     * @param transactionManager The transaction manager to use
     */
    public ApplicantDaoJdbc(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
    
    /**
     * Maps a ResultSet row to an Applicant object
     * @param rs The ResultSet containing applicant data
     * @return A new Applicant object
     * @throws SQLException If a database access error occurs
     */
    private Applicant map(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        
        // Fix for timestamp-based application_date
        LocalDate applicationDate;
        try {
            // First try to get as SQL Date
            java.sql.Date date = rs.getDate("application_date");
            if (date != null) {
                applicationDate = date.toLocalDate();
            } else {
                // If that fails, try to get as timestamp/long
                long timestamp = rs.getLong("application_date");
                // Convert from milliseconds to LocalDate
                applicationDate = java.time.Instant.ofEpochMilli(timestamp)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
            }
        } catch (Exception e) {
            // Fallback to current date if there's a problem with the date format
            applicationDate = LocalDate.now();
            System.err.println("Error parsing date for applicant " + id + ": " + e.getMessage());
        }
        
        int rank = rs.getInt("rank");
        
        return new Applicant(id, firstName, lastName, email, phone, applicationDate, rank);
    }
    
    @Override
    public Optional<Applicant> find(String id) {
        String sql = "SELECT * FROM applicants WHERE id = ?";
        
        return transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, id);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            } catch (SQLException e) {
                throw new DataAccessException("Error finding applicant with ID: " + id, e);
            }
        });
    }
    
    @Override
    public List<Applicant> findAll() {
        List<Applicant> applicants = new ArrayList<>();
        String sql = "SELECT * FROM applicants";
        
        return transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                
                while (rs.next()) {
                    applicants.add(map(rs));
                }
                return applicants;
            } catch (SQLException e) {
                throw new DataAccessException("Error finding all applicants", e);
            }
        });
    }
    
    @Override
    public void insert(Applicant applicant) {
        String sql = "INSERT INTO applicants (id, first_name, last_name, email, phone, application_date, rank) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, applicant.getId());
                pstmt.setString(2, applicant.getFirstName());
                pstmt.setString(3, applicant.getLastName());
                pstmt.setString(4, applicant.getEmail());
                pstmt.setString(5, applicant.getPhone());
                
                // Store application date as timestamp in milliseconds
                long timestamp = applicant.getApplicationDate().atStartOfDay()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toInstant().toEpochMilli();
                pstmt.setLong(6, timestamp);
                
                pstmt.setInt(7, applicant.getRank());
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new DataAccessException("Creating applicant failed, no rows affected");
                }
                
                return null;
            } catch (SQLException e) {
                throw new DataAccessException("Error inserting applicant: " + applicant.getFullName(), e);
            }
        });
    }
    
    @Override
    public void update(Applicant applicant) {
        String sql = "UPDATE applicants SET first_name = ?, last_name = ?, email = ?, " +
                     "phone = ?, application_date = ?, rank = ? WHERE id = ?";
        
        transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, applicant.getFirstName());
                pstmt.setString(2, applicant.getLastName());
                pstmt.setString(3, applicant.getEmail());
                pstmt.setString(4, applicant.getPhone());
                
                // Store application date as timestamp in milliseconds
                long timestamp = applicant.getApplicationDate().atStartOfDay()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toInstant().toEpochMilli();
                pstmt.setLong(5, timestamp);
                
                pstmt.setInt(6, applicant.getRank());
                pstmt.setString(7, applicant.getId());
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new DataAccessException("Updating applicant failed, no rows affected");
                }
                
                return null;
            } catch (SQLException e) {
                throw new DataAccessException("Error updating applicant: " + applicant.getFullName(), e);
            }
        });
    }
    
    @Override
    public void delete(String id) {
        String sql = "DELETE FROM applicants WHERE id = ?";
        
        transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, id);
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new DataAccessException("Deleting applicant failed, no rows affected");
                }
                
                return null;
            } catch (SQLException e) {
                throw new DataAccessException("Error deleting applicant with ID: " + id, e);
            }
        });
    }
    
    @Override
    public List<Applicant> findByRecruitment(String recruitmentId) {
        String sql = "SELECT a.* FROM applicants a " +
                     "JOIN applications app ON a.id = app.applicant_id " +
                     "WHERE app.recruitment_id = ?";
        
        return transactionManager.executeInTransaction(conn -> {
            List<Applicant> applicants = new ArrayList<>();
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, recruitmentId);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    applicants.add(map(rs));
                }
                return applicants;
            } catch (SQLException e) {
                throw new DataAccessException("Error finding applicants for recruitment: " + recruitmentId, e);
            }
        });
    }
    
    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM applicants";
        
        return transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            } catch (SQLException e) {
                throw new DataAccessException("Error counting applicants", e);
            }
        });
    }
}