package se.lu.ics.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import se.lu.ics.service.DatabaseService;

public class Applicant {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private List<Recruitment> applicationHistory;
    private int rank;
    private LocalDateTime applicationDate;

    public Applicant(String firstName, String lastName, String email, String phone) {
        this.id = generateId();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.applicationHistory = new ArrayList<>();
        this.rank = 0;
        this.applicationDate = LocalDateTime.now();
    }
    
    public Applicant(String id, String firstName, String lastName, String email, 
                    String phone, LocalDateTime applicationDate, int rank) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.applicationHistory = new ArrayList<>();
        this.rank = rank;
        this.applicationDate = applicationDate;
    }

    private String generateId() {
        // Generate a unique ID for the applicant
        return String.format("APP-%d-%d", 
            System.currentTimeMillis(), 
            (int)(Math.random() * 1000));
    }

    // Getters and setters
    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public List<Recruitment> getApplicationHistory() { return applicationHistory; }
    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public LocalDateTime getApplicationDate() { return applicationDate; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void addApplication(Recruitment recruitment) {
        applicationHistory.add(recruitment);
    }

    public void removeApplication(Recruitment recruitment) {
        applicationHistory.remove(recruitment);
    }

    public boolean hasAppliedFor(Recruitment recruitment) {
        return applicationHistory.contains(recruitment);
    }
    
    // Database operations
    public boolean save(Connection conn) {
        String sql = "INSERT INTO applicants (id, first_name, last_name, email, phone, application_date, rank) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, firstName);
            pstmt.setString(3, lastName);
            pstmt.setString(4, email);
            pstmt.setString(5, phone);
            pstmt.setString(6, DatabaseService.dateTimeToString(applicationDate));
            pstmt.setInt(7, rank);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving applicant: " + e.getMessage());
            return false;
        }
    }
    
    public boolean update(Connection conn) {
        String sql = "UPDATE applicants SET first_name = ?, last_name = ?, email = ?, " +
                    "phone = ?, rank = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, email);
            pstmt.setString(4, phone);
            pstmt.setInt(5, rank);
            pstmt.setString(6, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating applicant: " + e.getMessage());
            return false;
        }
    }
    
    public boolean delete(Connection conn) {
        String sql = "DELETE FROM applicants WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting applicant: " + e.getMessage());
            return false;
        }
    }
    
    public boolean applyForRecruitment(Connection conn, Recruitment recruitment) {
        String sql = "INSERT INTO applications (applicant_id, recruitment_id, application_date) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, recruitment.getId());
            pstmt.setString(3, DatabaseService.dateTimeToString(LocalDateTime.now()));
            boolean result = pstmt.executeUpdate() > 0;
            if (result) {
                addApplication(recruitment);
                recruitment.addApplicant(this);
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Error applying for recruitment: " + e.getMessage());
            return false;
        }
    }
    
    public boolean withdrawApplication(Connection conn, Recruitment recruitment) {
        String sql = "DELETE FROM applications WHERE applicant_id = ? AND recruitment_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, recruitment.getId());
            boolean result = pstmt.executeUpdate() > 0;
            if (result) {
                removeApplication(recruitment);
                recruitment.removeApplicant(this);
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Error withdrawing application: " + e.getMessage());
            return false;
        }
    }
    
    public static Applicant findById(Connection conn, String applicantId) {
        String sql = "SELECT * FROM applicants WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, applicantId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Applicant(
                    rs.getString("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    DatabaseService.stringToDateTime(rs.getString("application_date")),
                    rs.getInt("rank")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error finding applicant: " + e.getMessage());
        }
        return null;
    }
    
    public static List<Applicant> findAll(Connection conn) {
        List<Applicant> applicants = new ArrayList<>();
        String sql = "SELECT * FROM applicants";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                applicants.add(new Applicant(
                    rs.getString("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    DatabaseService.stringToDateTime(rs.getString("application_date")),
                    rs.getInt("rank")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error finding applicants: " + e.getMessage());
        }
        return applicants;
    }
    
    public static List<Applicant> findByRecruitment(Connection conn, Recruitment recruitment) {
        List<Applicant> applicants = new ArrayList<>();
        String sql = "SELECT a.* FROM applicants a " +
                    "JOIN applications app ON a.id = app.applicant_id " +
                    "WHERE app.recruitment_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, recruitment.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                applicants.add(new Applicant(
                    rs.getString("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    DatabaseService.stringToDateTime(rs.getString("application_date")),
                    rs.getInt("rank")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error finding applicants by recruitment: " + e.getMessage());
        }
        return applicants;
    }
    
    public List<Recruitment> loadApplicationHistory(Connection conn) {
        List<Recruitment> history = new ArrayList<>();
        String sql = "SELECT r.* FROM recruitments r " +
                    "JOIN applications a ON r.id = a.recruitment_id " +
                    "WHERE a.applicant_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Role role = Role.findById(conn, rs.getString("role_id"));
                history.add(new Recruitment(
                    rs.getString("id"),
                    role,
                    DatabaseService.stringToDate(rs.getString("application_deadline")),
                    DatabaseService.stringToDateTime(rs.getString("posting_date")),
                    DatabaseService.stringToDateTime(rs.getString("offer_acceptance_date")),
                    RecruitmentStatus.valueOf(rs.getString("status"))
                ));
            }
            this.applicationHistory = history;
        } catch (SQLException e) {
            System.err.println("Error loading application history: " + e.getMessage());
        }
        return history;
    }

    @Override
    public String toString() {
        return getFullName();
    }
} 