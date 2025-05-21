package se.lu.ics.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import se.lu.ics.model.Applicant;
import se.lu.ics.model.Recruitment;

/**
 * JDBC implementation of the ApplicantDao interface.
 */
public class ApplicantDaoJdbc implements ApplicantDao {
    
    private final Connection connection;
    
    /**
     * Constructor that takes a database connection
     * @param connection The database connection to use
     */
    public ApplicantDaoJdbc(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Constructor that takes a datasource
     * @param ds The datasource to get a connection from
     */
    public ApplicantDaoJdbc(DataSource ds) {
        try {
            this.connection = ds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
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
        LocalDateTime applicationDate = rs.getTimestamp("application_date").toLocalDateTime();
        int rank = rs.getInt("rank");
        
        return new Applicant(id, firstName, lastName, email, phone, applicationDate, rank);
    }
    
    @Override
    public Optional<Applicant> find(String id) {
        String sql = "SELECT * FROM applicants WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding applicant: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    @Override
    public List<Applicant> findAll() {
        List<Applicant> applicants = new ArrayList<>();
        String sql = "SELECT * FROM applicants";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                applicants.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all applicants: " + e.getMessage());
        }
        return applicants;
    }
    
    @Override
    public void insert(Applicant applicant) {
        String sql = "INSERT INTO applicants (id, first_name, last_name, email, phone, application_date, rank) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, applicant.getId());
            pstmt.setString(2, applicant.getFirstName());
            pstmt.setString(3, applicant.getLastName());
            pstmt.setString(4, applicant.getEmail());
            pstmt.setString(5, applicant.getPhone());
            pstmt.setObject(6, applicant.getApplicationDate());
            pstmt.setInt(7, applicant.getRank());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting applicant: " + e.getMessage());
        }
    }
    
    @Override
    public void update(Applicant applicant) {
        String sql = "UPDATE applicants SET first_name = ?, last_name = ?, email = ?, phone = ?, application_date = ?, rank = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, applicant.getFirstName());
            pstmt.setString(2, applicant.getLastName());
            pstmt.setString(3, applicant.getEmail());
            pstmt.setString(4, applicant.getPhone());
            pstmt.setObject(5, applicant.getApplicationDate());
            pstmt.setInt(6, applicant.getRank());
            pstmt.setString(7, applicant.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating applicant: " + e.getMessage());
        }
    }
    
    @Override
    public void delete(String id) {
        String sql = "DELETE FROM applicants WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting applicant: " + e.getMessage());
        }
    }
    
    @Override
    public List<Applicant> findByRecruitment(String recruitmentId) {
        List<Applicant> applicants = new ArrayList<>();
        String sql = "SELECT a.* FROM applicants a " +
                     "JOIN applicant_recruitments ar ON a.id = ar.applicant_id " +
                     "WHERE ar.recruitment_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, recruitmentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                applicants.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding applicants by recruitment: " + e.getMessage());
        }
        return applicants;
    }
} 