package se.lu.ics.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import se.lu.ics.service.DatabaseService;

public class Recruitment {
    private String id;
    private Role role;
    private LocalDate applicationDeadline;
    private LocalDateTime postingDate;
    private LocalDateTime offerAcceptanceDate;
    private RecruitmentStatus status;
    private List<Applicant> applicants;
    private List<Interview> interviews;
    private int applicantCount;
    private int interviewCount;
    private int offerCount;

    public Recruitment(Role role, LocalDate applicationDeadline) {
        this.id = generateId();
        this.role = role;
        this.applicationDeadline = applicationDeadline;
        this.postingDate = LocalDateTime.now();
        this.status = RecruitmentStatus.ACTIVE;
        this.applicants = new ArrayList<>();
        this.interviews = new ArrayList<>();
        this.applicantCount = 0;
        this.interviewCount = 0;
        this.offerCount = 0;
    }
    
    public Recruitment(String id, Role role, LocalDate applicationDeadline, 
                      LocalDateTime postingDate, LocalDateTime offerAcceptanceDate, 
                      RecruitmentStatus status) {
        this.id = id;
        this.role = role;
        this.applicationDeadline = applicationDeadline;
        this.postingDate = postingDate;
        this.offerAcceptanceDate = offerAcceptanceDate;
        this.status = status;
        this.applicants = new ArrayList<>();
        this.interviews = new ArrayList<>();
        this.applicantCount = 0;
        this.interviewCount = 0;
        this.offerCount = 0;
    }

    private String generateId() {
        int year = LocalDate.now().getYear();
        // In a real application, this would be persisted and incremented
        return String.format("HR %d/%d", year, System.currentTimeMillis() % 1000);
    }

    // Getters and setters
    public String getId() { return id; }
    public Role getRole() { return role; }
    public LocalDate getApplicationDeadline() { return applicationDeadline; }
    public void setApplicationDeadline(LocalDate deadline) { this.applicationDeadline = deadline; }
    public LocalDateTime getPostingDate() { return postingDate; }
    public RecruitmentStatus getStatus() { return status; }
    public void setStatus(RecruitmentStatus status) { this.status = status; }
    public List<Applicant> getApplicants() { return applicants; }
    public List<Interview> getInterviews() { return interviews; }
    public int getApplicantCount() { return applicantCount; }
    public int getInterviewCount() { return interviewCount; }
    public int getOfferCount() { return offerCount; }
    public LocalDateTime getOfferAcceptanceDate() { return offerAcceptanceDate; }

    public void addApplicant(Applicant applicant) {
        applicants.add(applicant);
        applicantCount++;
    }

    public void removeApplicant(Applicant applicant) {
        applicants.remove(applicant);
        applicantCount--;
    }

    public void addInterview(Interview interview) {
        interviews.add(interview);
        interviewCount++;
    }

    public void removeInterview(Interview interview) {
        interviews.remove(interview);
        interviewCount--;
    }

    public void recordOfferAcceptance() {
        this.offerAcceptanceDate = LocalDateTime.now();
        this.offerCount++;
    }

    public long getDaysToAcceptance() {
        if (offerAcceptanceDate == null) return 0;
        return java.time.Duration.between(postingDate, offerAcceptanceDate).toDays();
    }

    public double getInterviewsPerOffer() {
        if (offerCount == 0) return 0;
        return (double) interviewCount / offerCount;
    }
    
    // Database operations
    public boolean save(Connection conn) {
        String sql = "INSERT INTO recruitments (id, role_id, application_deadline, posting_date, " +
                    "offer_acceptance_date, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, role.getId());
            pstmt.setString(3, DatabaseService.dateToString(applicationDeadline));
            pstmt.setString(4, DatabaseService.dateTimeToString(postingDate));
            pstmt.setString(5, DatabaseService.dateTimeToString(offerAcceptanceDate));
            pstmt.setString(6, status.toString());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving recruitment: " + e.getMessage());
            return false;
        }
    }
    
    public boolean update(Connection conn) {
        String sql = "UPDATE recruitments SET role_id = ?, application_deadline = ?, " +
                    "posting_date = ?, offer_acceptance_date = ?, status = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role.getId());
            pstmt.setString(2, DatabaseService.dateToString(applicationDeadline));
            pstmt.setString(3, DatabaseService.dateTimeToString(postingDate));
            pstmt.setString(4, DatabaseService.dateTimeToString(offerAcceptanceDate));
            pstmt.setString(5, status.toString());
            pstmt.setString(6, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating recruitment: " + e.getMessage());
            return false;
        }
    }
    
    public boolean delete(Connection conn) {
        String sql = "DELETE FROM recruitments WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting recruitment: " + e.getMessage());
            return false;
        }
    }
    
    public static Recruitment findById(Connection conn, String recruitmentId) {
        String sql = "SELECT * FROM recruitments WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, recruitmentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Role role = Role.findById(conn, rs.getString("role_id"));
                return new Recruitment(
                    rs.getString("id"),
                    role,
                    DatabaseService.stringToDate(rs.getString("application_deadline")),
                    DatabaseService.stringToDateTime(rs.getString("posting_date")),
                    DatabaseService.stringToDateTime(rs.getString("offer_acceptance_date")),
                    RecruitmentStatus.valueOf(rs.getString("status").toUpperCase().replace(" ", "_"))
                );
            }
        } catch (SQLException e) {
            System.err.println("Error finding recruitment: " + e.getMessage());
        }
        return null;
    }
    
    public static List<Recruitment> findAll(Connection conn) {
        List<Recruitment> recruitments = new ArrayList<>();
        String sql = "SELECT * FROM recruitments";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Role role = Role.findById(conn, rs.getString("role_id"));
                recruitments.add(new Recruitment(
                    rs.getString("id"),
                    role,
                    DatabaseService.stringToDate(rs.getString("application_deadline")),
                    DatabaseService.stringToDateTime(rs.getString("posting_date")),
                    DatabaseService.stringToDateTime(rs.getString("offer_acceptance_date")),
                    RecruitmentStatus.valueOf(rs.getString("status").toUpperCase().replace(" ", "_"))
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error finding recruitments: " + e.getMessage());
        }
        return recruitments;
    }
    
    public static List<Recruitment> findByDateRange(Connection conn, LocalDate start, LocalDate end) {
        List<Recruitment> recruitments = new ArrayList<>();
        String sql = "SELECT * FROM recruitments WHERE date(posting_date) BETWEEN ? AND ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, DatabaseService.dateToString(start));
            pstmt.setString(2, DatabaseService.dateToString(end));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Role role = Role.findById(conn, rs.getString("role_id"));
                recruitments.add(new Recruitment(
                    rs.getString("id"),
                    role,
                    DatabaseService.stringToDate(rs.getString("application_deadline")),
                    DatabaseService.stringToDateTime(rs.getString("posting_date")),
                    DatabaseService.stringToDateTime(rs.getString("offer_acceptance_date")),
                    RecruitmentStatus.valueOf(rs.getString("status").toUpperCase().replace(" ", "_"))
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error finding recruitments by date range: " + e.getMessage());
        }
        return recruitments;
    }
    
    public static List<Recruitment> findByRole(Connection conn, Role role) {
        List<Recruitment> recruitments = new ArrayList<>();
        String sql = "SELECT * FROM recruitments WHERE role_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                recruitments.add(new Recruitment(
                    rs.getString("id"),
                    role,
                    DatabaseService.stringToDate(rs.getString("application_deadline")),
                    DatabaseService.stringToDateTime(rs.getString("posting_date")),
                    DatabaseService.stringToDateTime(rs.getString("offer_acceptance_date")),
                    RecruitmentStatus.valueOf(rs.getString("status").toUpperCase().replace(" ", "_"))
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error finding recruitments by role: " + e.getMessage());
        }
        return recruitments;
    }
    
    public int countApplicants(Connection conn) {
        String sql = "SELECT COUNT(*) FROM applications WHERE recruitment_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting applicants: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", id, role.getTitle());
    }
} 