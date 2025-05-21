package se.lu.ics.dao;

import java.util.List;
import se.lu.ics.model.Interview;
import se.lu.ics.model.Candidate;

/**
 * Data Access Object interface for Interview entities.
 */
public interface InterviewDao {
    
    /**
     * Find an interview by its ID
     * @param id The interview ID
     * @return The interview if found, null otherwise
     */
    Interview find(String id);
    
    /**
     * Find all interviews
     * @return List of all interviews
     */
    List<Interview> findAll();
    
    /**
     * Insert a new interview
     * @param interview The interview to insert
     * @return true if successful, false otherwise
     */
    boolean insert(Interview interview);
    
    /**
     * Update an existing interview
     * @param interview The interview to update
     * @return true if successful, false otherwise
     */
    boolean update(Interview interview);
    
    /**
     * Delete an interview
     * @param id The ID of the interview to delete
     * @return true if successful, false otherwise
     */
    boolean delete(String id);
    
    /**
     * Find interviews by candidate
     * @param candidate The candidate to find interviews for
     * @return List of interviews for the given candidate
     */
    List<Interview> findByCandidate(Candidate candidate);
} 