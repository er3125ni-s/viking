package se.lu.ics.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import se.lu.ics.exception.DataAccessException;
import se.lu.ics.model.Applicant;
import se.lu.ics.model.Interview;
import se.lu.ics.model.InterviewStatus;
import se.lu.ics.model.Recruitment;
import se.lu.ics.model.Role;
import se.lu.ics.model.RecruitmentStatus;

/**
 * JDBC implementation of the InterviewDao interface.
 */
public class InterviewDaoJdbc implements InterviewDao {
    
    private final TransactionManager transactionManager;
    private final ApplicantDao applicantDao;
    private final RecruitmentDao recruitmentDao;
    
    /**
     * Constructor that takes a transaction manager and related DAOs
     * @param transactionManager The transaction manager to use
     * @param applicantDao The DAO for applicants
     * @param recruitmentDao The DAO for recruitments
     */
    public InterviewDaoJdbc(TransactionManager transactionManager, 
                           ApplicantDao applicantDao, 
                           RecruitmentDao recruitmentDao) {
        this.transactionManager = transactionManager;
        this.applicantDao = applicantDao;
        this.recruitmentDao = recruitmentDao;
    }
    
    /**
     * Maps a ResultSet row to an Interview object
     * @param rs The ResultSet containing interview data
     * @return A new Interview object
     * @throws SQLException If a database access error occurs
     */
    private Interview map(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String recruitmentId = rs.getString("recruitment_id");
        String applicantId = rs.getString("applicant_id");
        
        // Handle date parsing issues
        LocalDateTime dateTime;
        try {
            dateTime = rs.getTimestamp("date_time").toLocalDateTime();
        } catch (SQLException | NullPointerException e) {
            // Fallback to current time
            System.err.println("Error parsing date time for interview " + id + ": " + e.getMessage());
            dateTime = LocalDateTime.now();
        }
        
        String location = rs.getString("location");
        String interviewer = rs.getString("interviewer");
        String statusStr = rs.getString("status");
        InterviewStatus status;
        try {
            status = InterviewStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Handle case sensitivity and format issues
            if ("Cancelled".equals(statusStr) || "cancelled".equals(statusStr.toLowerCase())) {
                status = InterviewStatus.CANCELLED;
            } else if ("Scheduled".equals(statusStr) || "scheduled".equals(statusStr.toLowerCase())) {
                status = InterviewStatus.SCHEDULED;
            } else if ("Rescheduled".equals(statusStr) || "rescheduled".equals(statusStr.toLowerCase())) {
                status = InterviewStatus.RESCHEDULED;
            } else if ("Completed".equals(statusStr) || "completed".equals(statusStr.toLowerCase())) {
                status = InterviewStatus.COMPLETED;
            } else {
                throw new DataAccessException("Unknown interview status: " + statusStr, e);
            }
        }
        String notes = rs.getString("notes");
        
        // Get the related objects
        Optional<Recruitment> recruitmentOpt = recruitmentDao.find(recruitmentId);
        Optional<Applicant> applicantOpt = applicantDao.find(applicantId);
        
        // Handle missing recruitment with a fallback
        Recruitment recruitment;
        if (recruitmentOpt.isEmpty()) {
            // Create a dummy Role and Recruitment for data integrity
            Role dummyRole = new Role("Unknown", "Unknown Department");
            dummyRole.setId("0");
            recruitment = new Recruitment(recruitmentId, dummyRole, LocalDateTime.now().toLocalDate(), 
                          LocalDateTime.now().plusMonths(1).toLocalDate(), null, RecruitmentStatus.OPEN);
        } else {
            recruitment = recruitmentOpt.get();
        }
        
        // Handle missing applicant with a fallback
        Applicant applicant;
        if (applicantOpt.isEmpty()) {
            // Create a dummy Applicant for data integrity
            applicant = new Applicant("Unknown", "Unknown", "unknown@example.com", "N/A");
            applicant.setId(applicantId);
        } else {
            applicant = applicantOpt.get();
        }
        
        return new Interview(id, recruitment, applicant, dateTime, 
                           location, interviewer, status, notes);
    }
    
    @Override
    public Optional<Interview> find(String id) {
        String sql = "SELECT * FROM interviews WHERE id = ?";
        
        return transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, id);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            } catch (SQLException e) {
                throw new DataAccessException("Error finding interview with ID: " + id, e);
            }
        });
    }
    
    @Override
    public List<Interview> findAll() {
        List<Interview> interviews = new ArrayList<>();
        String sql = "SELECT * FROM interviews";
        
        return transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                
                while (rs.next()) {
                    interviews.add(map(rs));
                }
                return interviews;
            } catch (SQLException e) {
                throw new DataAccessException("Error finding all interviews", e);
            }
        });
    }
    
    @Override
    public List<Interview> findByRecruitment(String recruitmentId) {
        List<Interview> interviews = new ArrayList<>();
        String sql = "SELECT * FROM interviews WHERE recruitment_id = ?";
        
        return transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, recruitmentId);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    interviews.add(map(rs));
                }
                return interviews;
            } catch (SQLException e) {
                throw new DataAccessException("Error finding interviews for recruitment: " + recruitmentId, e);
            }
        });
    }
    
    @Override
    public List<Interview> findByApplicant(String applicantId) {
        List<Interview> interviews = new ArrayList<>();
        String sql = "SELECT * FROM interviews WHERE applicant_id = ?";
        
        return transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, applicantId);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    interviews.add(map(rs));
                }
                return interviews;
            } catch (SQLException e) {
                throw new DataAccessException("Error finding interviews for applicant: " + applicantId, e);
            }
        });
    }
    
    @Override
    public void insert(Interview interview) {
        String sql = "INSERT INTO interviews (id, recruitment_id, applicant_id, date_time, location, interviewer, status, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, interview.getId());
                pstmt.setString(2, interview.getRecruitment().getId());
                pstmt.setString(3, interview.getApplicant().getId());
                pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(interview.getDateTime()));
                pstmt.setString(5, interview.getLocation());
                pstmt.setString(6, interview.getInterviewer());
                pstmt.setString(7, interview.getStatus().name());
                pstmt.setString(8, interview.getNotes());
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new DataAccessException("Creating interview failed, no rows affected");
                }
                
                return null;
            } catch (SQLException e) {
                throw new DataAccessException("Error inserting interview", e);
            }
        });
    }
    
    @Override
    public void update(Interview interview) {
        String sql = "UPDATE interviews SET recruitment_id = ?, applicant_id = ?, date_time = ?, " +
                     "location = ?, interviewer = ?, status = ?, notes = ? WHERE id = ?";
        
        transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, interview.getRecruitment().getId());
                pstmt.setString(2, interview.getApplicant().getId());
                pstmt.setTimestamp(3, java.sql.Timestamp.valueOf(interview.getDateTime()));
                pstmt.setString(4, interview.getLocation());
                pstmt.setString(5, interview.getInterviewer());
                pstmt.setString(6, interview.getStatus().name());
                pstmt.setString(7, interview.getNotes());
                pstmt.setString(8, interview.getId());
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new DataAccessException("Updating interview failed, no rows affected");
                }
                
                return null;
            } catch (SQLException e) {
                throw new DataAccessException("Error updating interview: " + interview.getId(), e);
            }
        });
    }
    
    @Override
    public void delete(String id) {
        String sql = "DELETE FROM interviews WHERE id = ?";
        
        transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, id);
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new DataAccessException("Deleting interview failed, no rows affected");
                }
                
                return null;
            } catch (SQLException e) {
                throw new DataAccessException("Error deleting interview with ID: " + id, e);
            }
        });
    }
}