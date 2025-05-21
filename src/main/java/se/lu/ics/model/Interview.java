package se.lu.ics.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

import se.lu.ics.exception.DataAccessException;

/**
 * Represents an interview for a job position.
 */
public class Interview {
    private String id;
    private Recruitment recruitment;
    private Applicant applicant;
    private LocalDateTime dateTime;
    private String location;
    private String interviewer;
    private InterviewStatus status;
    private String notes;

    /**
     * Constructor for a new interview
     * @param recruitment The recruitment for the interview
     * @param applicant The applicant being interviewed
     * @param dateTime The date and time of the interview
     * @param location The location of the interview
     * @param interviewer The name of the interviewer
     */
    public Interview(Recruitment recruitment, Applicant applicant, LocalDateTime dateTime, 
                    String location, String interviewer) {
        this.id = "INT-" + UUID.randomUUID().toString().substring(0, 8);
        this.recruitment = recruitment;
        this.applicant = applicant;
        this.dateTime = dateTime;
        this.location = location;
        this.interviewer = interviewer;
        this.status = InterviewStatus.SCHEDULED;
        this.notes = "";
    }

    /**
     * Constructor with all fields
     * @param id The interview ID
     * @param recruitment The recruitment for the interview
     * @param applicant The applicant being interviewed
     * @param dateTime The date and time of the interview
     * @param location The location of the interview
     * @param interviewer The name of the interviewer
     * @param status The status of the interview
     * @param notes Notes about the interview
     */
    public Interview(String id, Recruitment recruitment, Applicant applicant, 
                    LocalDateTime dateTime, String location, String interviewer, 
                    InterviewStatus status, String notes) {
        this.id = id;
        this.recruitment = recruitment;
        this.applicant = applicant;
        this.dateTime = dateTime;
        this.location = location;
        this.interviewer = interviewer;
        this.status = status;
        this.notes = notes;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Recruitment getRecruitment() {
        return recruitment;
    }

    public void setRecruitment(Recruitment recruitment) {
        this.recruitment = recruitment;
    }

    public Applicant getApplicant() {
        return applicant;
    }

    public void setApplicant(Applicant applicant) {
        this.applicant = applicant;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getInterviewer() {
        return interviewer;
    }

    public void setInterviewer(String interviewer) {
        this.interviewer = interviewer;
    }

    public InterviewStatus getStatus() {
        return status;
    }

    public void setStatus(InterviewStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Mark the interview as completed
     */
    public void complete() {
        this.status = InterviewStatus.COMPLETED;
    }

    /**
     * Mark the interview as cancelled
     * @return true if status was changed, false if already cancelled
     */
    public void cancel() {
        if (this.status == InterviewStatus.CANCELLED) {
            throw new IllegalStateException("Interview is already cancelled");
        }
        this.status = InterviewStatus.CANCELLED;
    }

    /**
     * Update the interview in the database
     * @param conn The database connection
     * @return true if successful, false otherwise
     */
    public boolean update(Connection conn) {
        try {
            String sql = "UPDATE interviews SET recruitment_id = ?, applicant_id = ?, date_time = ?, " +
                         "location = ?, interviewer = ?, status = ?, notes = ? WHERE id = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, recruitment.getId());
                pstmt.setString(2, applicant.getId());
                pstmt.setTimestamp(3, java.sql.Timestamp.valueOf(dateTime));
                pstmt.setString(4, location);
                pstmt.setString(5, interviewer);
                pstmt.setString(6, status.name());
                pstmt.setString(7, notes);
                pstmt.setString(8, id);
                
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating interview: " + id, e);
        }
    }
    
    @Override
    public String toString() {
        return String.format("Interview[id=%s, applicant=%s, date=%s]", 
                           id, applicant.getFullName(), dateTime);
    }
}
