package se.lu.ics.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import se.lu.ics.model.Interview;
import se.lu.ics.model.Applicant;
import se.lu.ics.model.Recruitment;
import se.lu.ics.model.InterviewStatus;

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
     * Constructor that takes a datasource
     * @param ds The datasource to get a connection from
     */
    public InterviewDaoJdbc(DataSource ds) {
        try {
            this.connection = ds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }
    
    /**
     * Maps a ResultSet row to an Interview object
     * @param rs The ResultSet containing interview data
     * @return A new Interview object
     * @throws SQLException If a database access error occurs
     */
    private Interview map(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String applicantId = rs.getString("applicant_id");
        String recruitmentId = rs.getString("recruitment_id");
        LocalDateTime dateTime = rs.getTimestamp("date_time").toLocalDateTime();
        String location = rs.getString("location");
        String interviewer = rs.getString("interviewer");
        String status = rs.getString("status");
        String notes = rs.getString("notes");
        
        // Find the associated Applicant
        Applicant applicant = findApplicant(applicantId)
            .orElseThrow(() -> new SQLException("Associated Applicant not found for ID: " + applicantId));
        
        // Find the associated Recruitment
        Recruitment recruitment = findRecruitment(recruitmentId)
            .orElseThrow(() -> new SQLException("Associated Recruitment not found for ID: " + recruitmentId));
        
        Interview interview = new Interview(id, recruitment, applicant, dateTime, location, interviewer, 
                InterviewStatus.valueOf(status), notes);
        
        return interview;
    }
    
    /**
     * Helper method to find an applicant by ID
     * @param applicantId The applicant ID
     * @return Optional containing the applicant if found
     */
    private Optional<Applicant> findApplicant(String applicantId) {
        ApplicantDao applicantDao = new ApplicantDaoJdbc(connection);
        return applicantDao.find(applicantId);
    }
    
    /**
     * Helper method to find a recruitment by ID
     * @param recruitmentId The recruitment ID
     * @return Optional containing the recruitment if found
     */
    private Optional<Recruitment> findRecruitment(String recruitmentId) {
        RecruitmentDao recruitmentDao = new RecruitmentDaoJdbc(connection);
        return recruitmentDao.find(recruitmentId);
    }
    
    @Override
    public Optional<Interview> find(String id) {
        String sql = "SELECT * FROM interviews WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding interview: " + e.getMessage());
        }
        return Optional.empty();
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
    public void insert(Interview interview) {
        String sql = "INSERT INTO interviews (id, recruitment_id, applicant_id, date_time, location, interviewer, status, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, interview.getId());
            pstmt.setString(2, interview.getRecruitment().getId());
            pstmt.setString(3, interview.getApplicant().getId());
            pstmt.setObject(4, interview.getDateTime());
            pstmt.setString(5, interview.getLocation());
            pstmt.setString(6, interview.getInterviewer());
            pstmt.setString(7, interview.getStatus().name());
            pstmt.setString(8, interview.getNotes());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting interview: " + e.getMessage());
        }
    }
    
    @Override
    public void update(Interview interview) {
        String sql = "UPDATE interviews SET recruitment_id = ?, applicant_id = ?, date_time = ?, location = ?, interviewer = ?, status = ?, notes = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, interview.getRecruitment().getId());
            pstmt.setString(2, interview.getApplicant().getId());
            pstmt.setObject(3, interview.getDateTime());
            pstmt.setString(4, interview.getLocation());
            pstmt.setString(5, interview.getInterviewer());
            pstmt.setString(6, interview.getStatus().name());
            pstmt.setString(7, interview.getNotes());
            pstmt.setString(8, interview.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating interview: " + e.getMessage());
        }
    }
    
    @Override
    public void delete(String id) {
        String sql = "DELETE FROM interviews WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting interview: " + e.getMessage());
        }
    }
    
    @Override
    public List<Interview> findByApplicant(String applicantId) {
        List<Interview> interviews = new ArrayList<>();
        String sql = "SELECT * FROM interviews WHERE applicant_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, applicantId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                interviews.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding interviews by applicant: " + e.getMessage());
        }
        return interviews;
    }
} 