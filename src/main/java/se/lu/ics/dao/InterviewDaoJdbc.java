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
    
    private final DataSource dataSource;
    private final ApplicantDao applicantDao;
    private final RecruitmentDao recruitmentDao;
    
    /**
     * Constructor that takes a database connection
     * @param connection The database connection to use
     */
    public InterviewDaoJdbc(Connection connection) {
        throw new UnsupportedOperationException("Use DataSource constructor instead");
    }
    
    /**
     * Constructor that takes a datasource
     * @param ds The datasource to get a connection from
     */
    public InterviewDaoJdbc(DataSource ds) {
        this.dataSource = ds;
        this.applicantDao = new ApplicantDaoJdbc(ds);
        this.recruitmentDao = new RecruitmentDaoJdbc(ds);
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
        LocalDateTime dateTime = LocalDateTime.parse(rs.getString("date_time"));  // istället för getTimestamp()
        String location = rs.getString("location");
        String interviewer = rs.getString("interviewer");
        String status = rs.getString("status");
        String notes = rs.getString("notes");
        
        // Find the associated Applicant
        Applicant applicant = applicantDao.find(applicantId)
            .orElseThrow(() -> new SQLException("Associated Applicant not found for ID: " + applicantId));
        
        // Find the associated Recruitment
        Recruitment recruitment = recruitmentDao.find(recruitmentId)
            .orElseThrow(() -> new SQLException("Associated Recruitment not found for ID: " + recruitmentId));
        
        Interview interview = new Interview(id, recruitment, applicant, dateTime, location, interviewer, 
                InterviewStatus.valueOf(status), notes);
        
        return interview;
    }
    
    @Override
    public Optional<Interview> find(String id) {
        String sql = "SELECT * FROM interviews WHERE id = ?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
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
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
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
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, interview.getId());
            ps.setString(2, interview.getRecruitment().getId());
            ps.setString(3, interview.getApplicant().getId());
            ps.setString(4, interview.getDateTime().toString());
            ps.setString(5, interview.getLocation());
            ps.setString(6, interview.getInterviewer());
            ps.setString(7, interview.getStatus().name());
            ps.setString(8, interview.getNotes());
            
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting interview: " + e.getMessage());
        }
    }
    
    @Override
    public void update(Interview interview) {
        String sql = "UPDATE interviews SET recruitment_id = ?, applicant_id = ?, date_time = ?, location = ?, interviewer = ?, status = ?, notes = ? WHERE id = ?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, interview.getRecruitment().getId());
            ps.setString(2, interview.getApplicant().getId());
            ps.setString(3, interview.getDateTime().toString());
            ps.setString(4, interview.getLocation());
            ps.setString(5, interview.getInterviewer());
            ps.setString(6, interview.getStatus().name());
            ps.setString(7, interview.getNotes());
            ps.setString(8, interview.getId());
            
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating interview: " + e.getMessage());
        }
    }
    
    @Override
    public void delete(String id) {
        String sql = "DELETE FROM interviews WHERE id = ?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting interview: " + e.getMessage());
        }
    }
    
    @Override
    public List<Interview> findByApplicant(String applicantId) {
        List<Interview> interviews = new ArrayList<>();
        String sql = "SELECT * FROM interviews WHERE applicant_id = ?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, applicantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                interviews.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding interviews by applicant: " + e.getMessage());
        }
        return interviews;
    }
} 