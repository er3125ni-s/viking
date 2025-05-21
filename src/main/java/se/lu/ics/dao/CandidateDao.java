package se.lu.ics.dao;

import java.util.List;
import se.lu.ics.model.Candidate;
import se.lu.ics.model.Recruitment;

/**
 * Data Access Object interface for Candidate entities.
 */
public interface CandidateDao {
    
    /**
     * Find a candidate by its ID
     * @param id The candidate ID
     * @return The candidate if found, null otherwise
     */
    Candidate find(String id);
    
    /**
     * Find all candidates
     * @return List of all candidates
     */
    List<Candidate> findAll();
    
    /**
     * Insert a new candidate
     * @param candidate The candidate to insert
     * @return true if successful, false otherwise
     */
    boolean insert(Candidate candidate);
    
    /**
     * Update an existing candidate
     * @param candidate The candidate to update
     * @return true if successful, false otherwise
     */
    boolean update(Candidate candidate);
    
    /**
     * Delete a candidate
     * @param id The ID of the candidate to delete
     * @return true if successful, false otherwise
     */
    boolean delete(String id);
    
    /**
     * Find candidates by recruitment
     * @param recruitment The recruitment to find candidates for
     * @return List of candidates for the given recruitment
     */
    List<Candidate> findByRecruitment(Recruitment recruitment);
} 