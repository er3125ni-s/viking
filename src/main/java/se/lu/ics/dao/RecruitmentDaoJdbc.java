package se.lu.ics.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import se.lu.ics.model.Recruitment;
import se.lu.ics.model.RecruitmentStatus;
import se.lu.ics.model.Role;
import se.lu.ics.service.DatabaseService;

/**
 * JDBC implementation of the RecruitmentDao interface.
 */
public class RecruitmentDaoJdbc implements RecruitmentDao {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DataSource dataSource;
    
    /**
     * Constructor that takes a DataSource
     * @param dataSource The DataSource to use for database connections
     */
    public RecruitmentDaoJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * Maps a ResultSet row to a Recruitment object
     * @param rs The ResultSet containing recruitment data
     * @return A new Recruitment object
     * @throws SQLException If a database access error occurs
     */
    private Recruitment mapRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        long roleId = rs.getLong("role_id");
        LocalDate createdAt = stringToDate(rs.getString("created_at"));
        String statusStr = rs.getString("status");
        RecruitmentStatus status = RecruitmentStatus.valueOf(statusStr);
        
        // For simplicity, we're not fetching the associated Role here
        // In a real app, you might want to fetch it or use a JOIN
        Role role = new Role(roleId);
        
        Recruitment recruitment = new Recruitment(id, role, createdAt, status);
        return recruitment;
    }
    
    /**
     * Convert String to LocalDate from database
     */
    private LocalDate stringToDate(String dateStr) {
        return dateStr != null ? LocalDate.parse(dateStr, DATE_FORMATTER) : null;
    }
    
    /**
     * Convert LocalDate to String for database storage
     */
    private String dateToString(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }
    
    @Override
    public Optional<Recruitment> find(long id) {
        String sql = "SELECT * FROM recruitments WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding recruitment: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    @Override
    public List<Recruitment> findAll() {
        List<Recruitment> recruitments = new ArrayList<>();
        String sql = "SELECT * FROM recruitments";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                recruitments.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all recruitments: " + e.getMessage());
        }
        return recruitments;
    }
    
    @Override
    public void insert(Recruitment recruitment) {
        String sql = "INSERT INTO recruitments (role_id, status, created_at) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setLong(1, recruitment.getRole().getId());
            pstmt.setString(2, recruitment.getStatus().toString());
            pstmt.setString(3, dateToString(recruitment.getCreatedAt()));
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating recruitment failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    recruitment.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating recruitment failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting recruitment: " + e.getMessage());
        }
    }
    
    @Override
    public void update(Recruitment recruitment) {
        String sql = "UPDATE recruitments SET role_id = ?, status = ?, created_at = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, recruitment.getRole().getId());
            pstmt.setString(2, recruitment.getStatus().toString());
            pstmt.setString(3, dateToString(recruitment.getCreatedAt()));
            pstmt.setLong(4, recruitment.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating recruitment: " + e.getMessage());
        }
    }
    
    @Override
    public void delete(long id) {
        String sql = "DELETE FROM recruitments WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting recruitment: " + e.getMessage());
        }
    }
    
    @Override
    public List<Recruitment> findByRole(long roleId) {
        List<Recruitment> recruitments = new ArrayList<>();
        String sql = "SELECT * FROM recruitments WHERE role_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, roleId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                recruitments.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding recruitments by role: " + e.getMessage());
        }
        return recruitments;
    }
} 