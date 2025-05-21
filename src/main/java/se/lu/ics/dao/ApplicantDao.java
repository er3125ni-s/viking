package se.lu.ics.dao;

import java.util.List;
import java.util.Optional;
import se.lu.ics.model.Applicant;

/**
 * Data Access Object interface for Applicant entities.
 */
public interface ApplicantDao {
    
    /**
     * Find an applicant by its ID
     * @param id The applicant ID
     * @return Optional containing the applicant if found, empty otherwise
     */
    Optional<Applicant> find(String id);
    
    /**
     * Find all applicants
     * @return List of all applicants
     */
    List<Applicant> findAll();
    
    /**
     * Insert a new applicant
     * @param applicant The applicant to insert
     */
    void insert(Applicant applicant);
    
    /**
     * Update an existing applicant
     * @param applicant The applicant to update
     */
    void update(Applicant applicant);
    
    /**
     * Delete an applicant
     * @param id The ID of the applicant to delete
     */
    void delete(String id);
    
    /**
     * Find all applicants for a recruitment
     * @param recruitmentId The recruitment ID
     * @return List of applicants for the recruitment
     */
    List<Applicant> findByRecruitment(String recruitmentId);
    
    /**
     * Count all applicants in the system
     * @return The total number of applicants
     */
    int countAll();
} 