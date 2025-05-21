package se.lu.ics.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import se.lu.ics.model.Interview;
import se.lu.ics.model.Candidate;

/**
 * JDBC implementation of the InterviewDao interface.
 */
public class InterviewDaoJdbc implements InterviewDao {
    
    private final Connection connection;
    
    /**
     * Constructor that takes a database connection
     * @param connection The database connection to use
     */
    public InterviewDaoJdbc(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Maps a ResultSet row to an Interview object
     * @param rs The ResultSet containing interview data
     * @return A new Interview object
     * @throws SQLException If a database access error occurs
     */
    private Interview map(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String candidateId = rs.getString("candidate_id");
        String dateStr = rs.getString("date");
        LocalDate date = LocalDate.parse(dateStr);
        String result = rs.getString("result");
        
        // Find the associated Candidate
        Candidate candidate = findCandidate(candidateId);
        if (candidate == null) {
            throw new SQLException("Associated Candidate not found for ID: " + candidateId);
        }
        
        return new Interview(id, candidate, date, result);
    }
    
    /**
     * Helper method to find a candidate by ID
     * @param candidateId The candidate ID
     * @return The candidate if found, null otherwise
     */
    private Candidate findCandidate(String candidateId) {
        CandidateDao candidateDao = new CandidateDaoJdbc(connection);
        return candidateDao.find(candidateId);
    }
    
    @Override
    public Interview find(String id) {
        String sql = "SELECT * FROM interviews WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return map(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding interview: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public List<Interview> findAll() {
        List<Interview> interviews = new ArrayList<>();
        String sql = "SELECT * FROM interviews";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                interviews.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all interviews: " + e.getMessage());
        }
        return interviews;
    }
    
    @Override
    public boolean insert(Interview interview) {
        String sql = "INSERT INTO interviews (id, candidate_id, date, result) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, interview.getId());
            pstmt.setString(2, interview.getCandidate().getId());
            pstmt.setString(3, interview.getDate().toString()); // ISO-8601 format
            pstmt.setString(4, interview.getResult());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                // If using auto-increment keys, we would handle them here
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting interview: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean update(Interview interview) {
        String sql = "UPDATE interviews SET candidate_id = ?, date = ?, result = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, interview.getCandidate().getId());
            pstmt.setString(2, interview.getDate().toString()); // ISO-8601 format
            pstmt.setString(3, interview.getResult());
            pstmt.setString(4, interview.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating interview: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM interviews WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting interview: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public List<Interview> findByCandidate(Candidate candidate) {
        List<Interview> interviews = new ArrayList<>();
        String sql = "SELECT * FROM interviews WHERE candidate_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, candidate.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                interviews.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding interviews by candidate: " + e.getMessage());
        }
        return interviews;
    }
} 