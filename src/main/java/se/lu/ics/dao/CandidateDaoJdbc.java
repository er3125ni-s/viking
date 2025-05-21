package se.lu.ics.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import se.lu.ics.model.Candidate;
import se.lu.ics.model.Recruitment;
import se.lu.ics.service.DatabaseService;

/**
 * JDBC implementation of the CandidateDao interface.
 */
public class CandidateDaoJdbc implements CandidateDao {
    
    private final Connection connection;
    
    /**
     * Constructor that takes a database connection
     * @param connection The database connection to use
     */
    public CandidateDaoJdbc(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Maps a ResultSet row to a Candidate object
     * @param rs The ResultSet containing candidate data
     * @return A new Candidate object
     * @throws SQLException If a database access error occurs
     */
    private Candidate map(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String recruitmentId = rs.getString("recruitment_id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String status = rs.getString("status");
        
        // Find the associated Recruitment
        Recruitment recruitment = Recruitment.findById(connection, recruitmentId);
        if (recruitment == null) {
            throw new SQLException("Associated Recruitment not found for ID: " + recruitmentId);
        }
        
        return new Candidate(id, recruitment, name, email, phone, status);
    }
    
    @Override
    public Candidate find(String id) {
        String sql = "SELECT * FROM candidates WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return map(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding candidate: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public List<Candidate> findAll() {
        List<Candidate> candidates = new ArrayList<>();
        String sql = "SELECT * FROM candidates";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                candidates.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all candidates: " + e.getMessage());
        }
        return candidates;
    }
    
    @Override
    public boolean insert(Candidate candidate) {
        String sql = "INSERT INTO candidates (id, recruitment_id, name, email, phone, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, candidate.getId());
            pstmt.setString(2, candidate.getRecruitment().getId());
            pstmt.setString(3, candidate.getName());
            pstmt.setString(4, candidate.getEmail());
            pstmt.setString(5, candidate.getPhone());
            pstmt.setString(6, candidate.getStatus());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                // If using auto-increment keys, we would handle them here
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting candidate: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean update(Candidate candidate) {
        String sql = "UPDATE candidates SET recruitment_id = ?, name = ?, email = ?, phone = ?, status = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, candidate.getRecruitment().getId());
            pstmt.setString(2, candidate.getName());
            pstmt.setString(3, candidate.getEmail());
            pstmt.setString(4, candidate.getPhone());
            pstmt.setString(5, candidate.getStatus());
            pstmt.setString(6, candidate.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating candidate: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM candidates WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting candidate: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public List<Candidate> findByRecruitment(Recruitment recruitment) {
        List<Candidate> candidates = new ArrayList<>();
        String sql = "SELECT * FROM candidates WHERE recruitment_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, recruitment.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                candidates.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding candidates by recruitment: " + e.getMessage());
        }
        return candidates;
    }
} 