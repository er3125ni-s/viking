package se.lu.ics.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Role {
    private String id;
    private String title;
    private String description;
    private String department;
    private List<Recruitment> recruitments;
    private int ongoingRecruitments;

    public Role(String title, String description, String department) {
        this.id = generateId();
        this.title = title;
        this.description = description;
        this.department = department;
        this.recruitments = new ArrayList<>();
        this.ongoingRecruitments = 0;
    }
    
    public Role(String id, String title, String description, String department) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.department = department;
        this.recruitments = new ArrayList<>();
        this.ongoingRecruitments = 0;
    }

    private String generateId() {
        // Generate a unique ID for the role
        return "ROLE-" + System.currentTimeMillis();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public List<Recruitment> getRecruitments() {
        return recruitments;
    }

    public int getOngoingRecruitments() {
        return ongoingRecruitments;
    }

    public void addRecruitment(Recruitment recruitment) {
        recruitments.add(recruitment);
        if (recruitment.getStatus() == RecruitmentStatus.ACTIVE) {
            ongoingRecruitments++;
        }
    }

    public void removeRecruitment(Recruitment recruitment) {
        recruitments.remove(recruitment);
        if (recruitment.getStatus() == RecruitmentStatus.ACTIVE) {
            ongoingRecruitments--;
        }
    }
    
    // Database operations
    public boolean save(Connection conn) {
        String sql = "INSERT INTO roles (id, title, description, department) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, title);
            pstmt.setString(3, description);
            pstmt.setString(4, department);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving role: " + e.getMessage());
            return false;
        }
    }
    
    public boolean update(Connection conn) {
        String sql = "UPDATE roles SET title = ?, description = ?, department = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setString(3, department);
            pstmt.setString(4, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating role: " + e.getMessage());
            return false;
        }
    }
    
    public boolean delete(Connection conn) {
        String sql = "DELETE FROM roles WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting role: " + e.getMessage());
            return false;
        }
    }
    
    public static Role findById(Connection conn, String roleId) {
        String sql = "SELECT * FROM roles WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roleId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Role(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("department")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error finding role: " + e.getMessage());
        }
        return null;
    }
    
    public static List<Role> findAll(Connection conn) {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT * FROM roles";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                roles.add(new Role(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("department")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error finding roles: " + e.getMessage());
        }
        return roles;
    }

    @Override
    public String toString() {
        return title;
    }
} 