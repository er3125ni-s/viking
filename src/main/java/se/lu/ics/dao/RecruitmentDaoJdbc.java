package se.lu.ics.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import se.lu.ics.exception.DataAccessException;
import se.lu.ics.model.Recruitment;
import se.lu.ics.model.RecruitmentStatus;
import se.lu.ics.model.Role;

/**
 * JDBC implementation of the RecruitmentDao interface.
 */
public class RecruitmentDaoJdbc implements RecruitmentDao {
    
    private final TransactionManager transactionManager;
    private final RoleDao roleDao;
    
    /**
     * Constructor that takes a transaction manager and role DAO
     * @param transactionManager The transaction manager
     * @param roleDao The role DAO
     */
    public RecruitmentDaoJdbc(TransactionManager transactionManager, RoleDao roleDao) {
        this.transactionManager = transactionManager;
        this.roleDao = roleDao;
    }
    
    /**
     * Maps a ResultSet row to a Recruitment object
     * @param rs The ResultSet containing recruitment data
     * @return A new Recruitment object
     * @throws SQLException If a database access error occurs
     */
    private Recruitment mapRow(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String roleId = rs.getString("role_id");
        
        // Handle different date formats
        LocalDate postingDate;
        String postingDateStr = rs.getString("posting_date");
        if (postingDateStr.contains(" ")) {
            // Handle timestamp format "2025-05-20 22:05:14"
            postingDate = LocalDate.parse(postingDateStr.split(" ")[0]);
        } else {
            // Handle simple date format "2025-07-17"
            postingDate = LocalDate.parse(postingDateStr);
        }
        
        LocalDate applicationDeadline;
        String deadlineStr = rs.getString("application_deadline");
        if (deadlineStr.contains(" ")) {
            applicationDeadline = LocalDate.parse(deadlineStr.split(" ")[0]);
        } else {
            applicationDeadline = LocalDate.parse(deadlineStr);
        }
        
        LocalDate offerAcceptanceDate = null;
        String acceptanceDateStr = rs.getString("offer_acceptance_date");
        if (acceptanceDateStr != null && !acceptanceDateStr.isEmpty()) {
            if (acceptanceDateStr.contains(" ")) {
                offerAcceptanceDate = LocalDate.parse(acceptanceDateStr.split(" ")[0]);
            } else {
                offerAcceptanceDate = LocalDate.parse(acceptanceDateStr);
            }
        }
        
        String statusStr = rs.getString("status");
        RecruitmentStatus status;
        try {
            // Try to convert directly
            status = RecruitmentStatus.valueOf(statusStr.replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            // Handle non-standard status values
            if ("On_Hold".equals(statusStr) || "On Hold".equals(statusStr) || "Active".equals(statusStr)) {
                status = RecruitmentStatus.OPEN;
            } else {
                throw new DataAccessException("Unknown recruitment status: " + statusStr, e);
            }
        }
        
        // Fetch the associated Role using the RoleDao
        Role role = roleDao.find(roleId)
                .orElseThrow(() -> new DataAccessException("Role not found for ID: " + roleId));
        
        return new Recruitment(id, role, postingDate, applicationDeadline, offerAcceptanceDate, status);
    }
    
    @Override
    public Optional<Recruitment> find(String id) {
        String sql = "SELECT * FROM recruitments WHERE id = ?";
        
        return transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, id);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            } catch (SQLException e) {
                throw new DataAccessException("Error finding recruitment with ID: " + id, e);
            }
        });
    }
    
    @Override
    public List<Recruitment> findAll() {
        List<Recruitment> recruitments = new ArrayList<>();
        String sql = "SELECT * FROM recruitments";
        
        return transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                
                while (rs.next()) {
                    recruitments.add(mapRow(rs));
                }
                return recruitments;
            } catch (SQLException e) {
                throw new DataAccessException("Error finding all recruitments", e);
            }
        });
    }
    
    @Override
    public void insert(Recruitment recruitment) {
        String sql = "INSERT INTO recruitments (id, role_id, posting_date, application_deadline, offer_acceptance_date, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, recruitment.getId());
                pstmt.setString(2, recruitment.getRole().getId());
                pstmt.setString(3, recruitment.getPostingDate().toString());
                pstmt.setString(4, recruitment.getApplicationDeadline().toString());
                
                if (recruitment.getOfferAcceptanceDate() != null) {
                    pstmt.setString(5, recruitment.getOfferAcceptanceDate().toString());
                } else {
                    pstmt.setNull(5, java.sql.Types.VARCHAR);
                }
                
                pstmt.setString(6, recruitment.getStatus().name());
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new DataAccessException("Creating recruitment failed, no rows affected");
                }
                
                return null;
            } catch (SQLException e) {
                throw new DataAccessException("Error inserting recruitment: " + recruitment.getId(), e);
            }
        });
    }
    
    @Override
    public void update(Recruitment recruitment) {
        String sql = "UPDATE recruitments SET role_id = ?, posting_date = ?, application_deadline = ?, " +
                     "offer_acceptance_date = ?, status = ? WHERE id = ?";
        
        transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, recruitment.getRole().getId());
                pstmt.setString(2, recruitment.getPostingDate().toString());
                pstmt.setString(3, recruitment.getApplicationDeadline().toString());
                
                if (recruitment.getOfferAcceptanceDate() != null) {
                    pstmt.setString(4, recruitment.getOfferAcceptanceDate().toString());
                } else {
                    pstmt.setNull(4, java.sql.Types.VARCHAR);
                }
                
                pstmt.setString(5, recruitment.getStatus().name());
                pstmt.setString(6, recruitment.getId());
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new DataAccessException("Updating recruitment failed, no rows affected");
                }
                
                return null;
            } catch (SQLException e) {
                throw new DataAccessException("Error updating recruitment: " + recruitment.getId(), e);
            }
        });
    }
    
    @Override
    public void delete(String id) {
        String sql = "DELETE FROM recruitments WHERE id = ?";
        
        transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, id);
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new DataAccessException("Deleting recruitment failed, no rows affected");
                }
                
                return null;
            } catch (SQLException e) {
                throw new DataAccessException("Error deleting recruitment with ID: " + id, e);
            }
        });
    }
    
    @Override
    public List<Recruitment> findByRole(String roleId) {
        List<Recruitment> recruitments = new ArrayList<>();
        String sql = "SELECT * FROM recruitments WHERE role_id = ?";
        
        return transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, roleId);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    recruitments.add(mapRow(rs));
                }
                return recruitments;
            } catch (SQLException e) {
                throw new DataAccessException("Error finding recruitments for role: " + roleId, e);
            }
        });
    }
}