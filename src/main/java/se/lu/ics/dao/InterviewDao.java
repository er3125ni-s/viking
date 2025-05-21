package se.lu.ics.dao;

import java.util.List;
import java.util.Optional;
import se.lu.ics.model.Interview;
import se.lu.ics.model.Applicant;

/**
 * Data Access Object interface for Interview entities.
 */
public interface InterviewDao {
    
    /**
     * Find an interview by its ID
     * @param id The interview ID
     * @return Optional containing the interview if found, empty otherwise
     */
    Optional<Interview> find(String id);
    
    /**
     * Find all interviews
     * @return List of all interviews
     */
    List<Interview> findAll();
    
    /**
     * Insert a new interview and set its generated ID
     * @param interview The interview to insert
     */
    void insert(Interview interview);
    
    /**
     * Update an existing interview
     * @param interview The interview to update
     */
    void update(Interview interview);
    
    /**
     * Delete an interview
     * @param id The ID of the interview to delete
     */
    void delete(String id);
    
    /**
     * Find interviews by applicant
     * @param applicantId The ID of the applicant to find interviews for
     * @return List of interviews for the given applicant
     */
    List<Interview> findByApplicant(String applicantId);
} 