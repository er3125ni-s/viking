package se.lu.ics.dao;

import java.util.List;
import java.util.Optional;
import se.lu.ics.model.Recruitment;

/**
 * Data Access Object interface for Recruitment entities.
 */
public interface RecruitmentDao {
    
    /**
     * Find a recruitment by its ID
     * @param id The recruitment ID
     * @return Optional containing the recruitment if found, empty otherwise
     */
    Optional<Recruitment> find(String id);
    
    /**
     * Find all recruitments
     * @return List of all recruitments
     */
    List<Recruitment> findAll();
    
    /**
     * Insert a new recruitment
     * @param recruitment The recruitment to insert
     */
    void insert(Recruitment recruitment);
    
    /**
     * Update an existing recruitment
     * @param recruitment The recruitment to update
     */
    void update(Recruitment recruitment);
    
    /**
     * Delete a recruitment
     * @param id The ID of the recruitment to delete
     */
    void delete(String id);
    
    /**
     * Find recruitments by role ID
     * @param roleId The role ID
     * @return List of recruitments for the given role
     */
    List<Recruitment> findByRole(String roleId);
} 