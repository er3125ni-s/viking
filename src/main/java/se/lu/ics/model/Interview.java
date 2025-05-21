package se.lu.ics.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import se.lu.ics.service.DatabaseService;
import java.util.ArrayList;
import java.util.List;

public class Interview {
    private String id;
    private Recruitment recruitment;
    private Applicant applicant;
    private LocalDateTime dateTime;
    private String location;
    private String interviewer;
    private InterviewStatus status;
    private String notes;

    public Interview(Recruitment recruitment, Applicant applicant, LocalDateTime dateTime, 
                    String location, String interviewer) {
        this.id = generateId();
        this.recruitment = recruitment;
        this.applicant = applicant;
        this.dateTime = dateTime;
        this.location = location;
        this.interviewer = interviewer;
        this.status = InterviewStatus.SCHEDULED;
        this.notes = "";
    }
    
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

    private String generateId() {
        return String.format("INT-%d-%d", 
            System.currentTimeMillis(), 
            (int)(Math.random() * 1000));
    }

    // Getters and setters
    public String getId() { return id; }
    public Recruitment getRecruitment() { return recruitment; }
    public Applicant getApplicant() { return applicant; }
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getInterviewer() { return interviewer; }
    public void setInterviewer(String interviewer) { this.interviewer = interviewer; }
    public InterviewStatus getStatus() { return status; }
    public void setStatus(InterviewStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public void cancel() {
        this.status = InterviewStatus.CANCELLED;
    }

    public void complete() {
        this.status = InterviewStatus.COMPLETED;
    }

    public void reschedule(LocalDateTime newDateTime) {
        this.dateTime = newDateTime;
        this.status = InterviewStatus.RESCHEDULED;
    }
    
    // Database operations
    public boolean save(Connection conn) {
        String sql = "INSERT INTO interviews (id, recruitment_id, applicant_id, date_time, " +
                    "location, interviewer, status, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, recruitment.getId());
            pstmt.setString(3, applicant.getId());
            pstmt.setString(4, DatabaseService.dateTimeToString(dateTime));
            pstmt.setString(5, location);
            pstmt.setString(6, interviewer);
            pstmt.setString(7, status.toString());
            pstmt.setString(8, notes);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving interview: " + e.getMessage());
            return false;
        }
    }
    
    public boolean update(Connection conn) {
        String sql = "UPDATE interviews SET date_time = ?, location = ?, interviewer = ?, " +
                    "status = ?, notes = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, DatabaseService.dateTimeToString(dateTime));
            pstmt.setString(2, location);
            pstmt.setString(3, interviewer);
            pstmt.setString(4, status.toString());
            pstmt.setString(5, notes);
            pstmt.setString(6, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating interview: " + e.getMessage());
            return false;
        }
    }
    
    public boolean delete(Connection conn) {
        String sql = "DELETE FROM interviews WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting interview: " + e.getMessage());
            return false;
        }
    }
    
    public static Interview findById(Connection conn, String interviewId) {
        String sql = "SELECT * FROM interviews WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, interviewId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Recruitment recruitment = Recruitment.findById(conn, rs.getString("recruitment_id"));
                Applicant applicant = Applicant.findById(conn, rs.getString("applicant_id"));
                return new Interview(
                    rs.getString("id"),
                    recruitment,
                    applicant,
                    DatabaseService.stringToDateTime(rs.getString("date_time")),
                    rs.getString("location"),
                    rs.getString("interviewer"),
                    InterviewStatus.valueOf(rs.getString("status").toUpperCase().replace(" ", "_")),
                    rs.getString("notes")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error finding interview: " + e.getMessage());
        }
        return null;
    }
    
    public static List<Interview> findAll(Connection conn) {
        List<Interview> interviews = new ArrayList<>();
        String sql = "SELECT * FROM interviews";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Recruitment recruitment = Recruitment.findById(conn, rs.getString("recruitment_id"));
                Applicant applicant = Applicant.findById(conn, rs.getString("applicant_id"));
                interviews.add(new Interview(
                    rs.getString("id"),
                    recruitment,
                    applicant,
                    DatabaseService.stringToDateTime(rs.getString("date_time")),
                    rs.getString("location"),
                    rs.getString("interviewer"),
                    InterviewStatus.valueOf(rs.getString("status").toUpperCase().replace(" ", "_")),
                    rs.getString("notes")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error finding interviews: " + e.getMessage());
        }
        return interviews;
    }
    
    public static List<Interview> findByRecruitment(Connection conn, Recruitment recruitment) {
        List<Interview> interviews = new ArrayList<>();
        String sql = "SELECT * FROM interviews WHERE recruitment_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, recruitment.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Applicant applicant = Applicant.findById(conn, rs.getString("applicant_id"));
                interviews.add(new Interview(
                    rs.getString("id"),
                    recruitment,
                    applicant,
                    DatabaseService.stringToDateTime(rs.getString("date_time")),
                    rs.getString("location"),
                    rs.getString("interviewer"),
                    InterviewStatus.valueOf(rs.getString("status").toUpperCase().replace(" ", "_")),
                    rs.getString("notes")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error finding interviews by recruitment: " + e.getMessage());
        }
        return interviews;
    }
    
    public static List<Interview> findByApplicant(Connection conn, Applicant applicant) {
        List<Interview> interviews = new ArrayList<>();
        String sql = "SELECT * FROM interviews WHERE applicant_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, applicant.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Recruitment recruitment = Recruitment.findById(conn, rs.getString("recruitment_id"));
                interviews.add(new Interview(
                    rs.getString("id"),
                    recruitment,
                    applicant,
                    DatabaseService.stringToDateTime(rs.getString("date_time")),
                    rs.getString("location"),
                    rs.getString("interviewer"),
                    InterviewStatus.valueOf(rs.getString("status").toUpperCase().replace(" ", "_")),
                    rs.getString("notes")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error finding interviews by applicant: " + e.getMessage());
        }
        return interviews;
    }

    @Override
    public String toString() {
        return String.format("%s - %s with %s", 
            dateTime.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            applicant.getFullName(),
            interviewer);
    }
} 