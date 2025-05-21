package se.lu.ics.dao;

import java.util.List;
import se.lu.ics.model.Recruitment;

/**
 * Data Access Object interface for Recruitment entities.
 */
public interface RecruitmentDao {
    
    /**
     * Find a recruitment by its ID
     * @param id The recruitment ID
     * @return The recruitment if found, null otherwise
     */
    Recruitment find(String id);
    
    /**
     * Find all recruitments
     * @return List of all recruitments
     */
    List<Recruitment> findAll();
    
    /**
     * Insert a new recruitment
     * @param recruitment The recruitment to insert
     * @return true if successful, false otherwise
     */
    boolean insert(Recruitment recruitment);
    
    /**
     * Update an existing recruitment
     * @param recruitment The recruitment to update
     * @return true if successful, false otherwise
     */
    boolean update(Recruitment recruitment);
    
    /**
     * Delete a recruitment
     * @param id The ID of the recruitment to delete
     * @return true if successful, false otherwise
     */
    boolean delete(String id);
} 