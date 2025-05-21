package se.lu.ics.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import se.lu.ics.exception.DataAccessException;
import se.lu.ics.model.Role;

/**
 * JDBC implementation of the RoleDao interface.
 */
public class RoleDaoJdbc implements RoleDao {
    
    private final TransactionManager transactionManager;
    
    /**
     * Constructor that takes a transaction manager
     * @param transactionManager The transaction manager to use
     */
    public RoleDaoJdbc(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
    
    /**
     * Maps a ResultSet row to a Role object
     * @param rs The ResultSet containing role data
     * @return A new Role object
     * @throws SQLException If a database access error occurs
     */
    private Role map(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        String department = rs.getString("department");
        
        return new Role(id, title, description, department);
    }
    
    @Override
    public Optional<Role> find(String id) {
        String sql = "SELECT * FROM roles WHERE id = ?";
        
        return transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, id);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            } catch (SQLException e) {
                throw new DataAccessException("Error finding role with ID: " + id, e);
            }
        });
    }
    
    @Override
    public List<Role> findAll() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT * FROM roles";
        
        return transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                
                while (rs.next()) {
                    roles.add(map(rs));
                }
                return roles;
            } catch (SQLException e) {
                throw new DataAccessException("Error finding all roles", e);
            }
        });
    }
    
    @Override
    public void insert(Role role) {
        String sql = "INSERT INTO roles (title, description, department) VALUES (?, ?, ?)";
        
        transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, role.getTitle());
                pstmt.setString(2, role.getDescription());
                pstmt.setString(3, role.getDepartment());
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new DataAccessException("Creating role failed, no rows affected");
                }
                
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        role.setId(generatedKeys.getString(1));
                    } else {
                        throw new DataAccessException("Creating role failed, no ID obtained");
                    }
                }
                
                return null;
            } catch (SQLException e) {
                throw new DataAccessException("Error inserting role: " + role.getTitle(), e);
            }
        });
    }
    
    @Override
    public void update(Role role) {
        String sql = "UPDATE roles SET title = ?, description = ?, department = ? WHERE id = ?";
        
        transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, role.getTitle());
                pstmt.setString(2, role.getDescription());
                pstmt.setString(3, role.getDepartment());
                pstmt.setString(4, role.getId());
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new DataAccessException("Updating role failed, no rows affected");
                }
                
                return null;
            } catch (SQLException e) {
                throw new DataAccessException("Error updating role: " + role.getTitle(), e);
            }
        });
    }
    
    @Override
    public void delete(String id) {
        String sql = "DELETE FROM roles WHERE id = ?";
        
        transactionManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, id);
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new DataAccessException("Deleting role failed, no rows affected");
                }
                
                return null;
            } catch (SQLException e) {
                throw new DataAccessException("Error deleting role with ID: " + id, e);
            }
        });
    }
}