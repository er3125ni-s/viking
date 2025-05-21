package se.lu.ics.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import se.lu.ics.model.Recruitment;
import se.lu.ics.model.RecruitmentStatus;
import se.lu.ics.model.Role;
import se.lu.ics.service.DatabaseService;

/**
 * JDBC implementation of the RecruitmentDao interface.
 */
public class RecruitmentDaoJdbc implements RecruitmentDao {
    
    private final Connection connection;
    
    /**
     * Constructor that takes a database connection
     * @param connection The database connection to use
     */
    public RecruitmentDaoJdbc(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Maps a ResultSet row to a Recruitment object
     * @param rs The ResultSet containing recruitment data
     * @return A new Recruitment object
     * @throws SQLException If a database access error occurs
     */
    private Recruitment map(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String roleId = rs.getString("role_id");
        LocalDate createdAt = DatabaseService.stringToDate(rs.getString("created_at"));
        String statusStr = rs.getString("status");
        RecruitmentStatus status = RecruitmentStatus.valueOf(statusStr);
        
        // Find the associated Role
        Role role = Role.findById(connection, roleId);
        if (role == null) {
            throw new SQLException("Associated Role not found for ID: " + roleId);
        }
        
        return new Recruitment(id, role, createdAt, status);
    }
    
    @Override
    public Recruitment find(String id) {
        String sql = "SELECT * FROM recruitments WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return map(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding recruitment: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public List<Recruitment> findAll() {
        List<Recruitment> recruitments = new ArrayList<>();
        String sql = "SELECT * FROM recruitments";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                recruitments.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all recruitments: " + e.getMessage());
        }
        return recruitments;
    }
    
    @Override
    public boolean insert(Recruitment recruitment) {
        String sql = "INSERT INTO recruitments (id, role_id, status, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, recruitment.getId());
            pstmt.setString(2, recruitment.getRole().getId());
            pstmt.setString(3, recruitment.getStatus().toString());
            pstmt.setString(4, DatabaseService.dateToString(recruitment.getCreatedAt()));
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                // If using auto-increment keys, we would handle them here
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting recruitment: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean update(Recruitment recruitment) {
        String sql = "UPDATE recruitments SET role_id = ?, status = ?, created_at = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, recruitment.getRole().getId());
            pstmt.setString(2, recruitment.getStatus().toString());
            pstmt.setString(3, DatabaseService.dateToString(recruitment.getCreatedAt()));
            pstmt.setString(4, recruitment.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating recruitment: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM recruitments WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting recruitment: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Find recruitments by Role
     * @param role The Role to search for
     * @return List of recruitments for the given role
     */
    public List<Recruitment> findByRole(Role role) {
        List<Recruitment> recruitments = new ArrayList<>();
        String sql = "SELECT * FROM recruitments WHERE role_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, role.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                recruitments.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding recruitments by role: " + e.getMessage());
        }
        return recruitments;
    }
} 