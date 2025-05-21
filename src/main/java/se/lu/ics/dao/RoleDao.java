package se.lu.ics.dao;

import java.util.List;
import java.util.Optional;
import se.lu.ics.model.Role;

/**
 * Data Access Object interface for Role entities.
 */
public interface RoleDao {
    
    /**
     * Find a role by its ID
     * @param id The role ID
     * @return Optional containing the role if found, empty otherwise
     */
    Optional<Role> find(String id);
    
    /**
     * Find all roles
     * @return List of all roles
     */
    List<Role> findAll();
    
    /**
     * Insert a new role and set its generated ID
     * @param role The role to insert
     */
    void insert(Role role);
    
    /**
     * Update an existing role
     * @param role The role to update
     */
    void update(Role role);
    
    /**
     * Delete a role
     * @param id The ID of the role to delete
     */
    void delete(String id);
} 