package se.lu.ics.dao;

import java.util.List;
import java.util.Optional;
import se.lu.ics.model.Applicant;
import se.lu.ics.model.Recruitment;

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
     * Insert a new applicant and set its generated ID
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
     * Find applicants by recruitment
     * @param recruitment The recruitment to find applicants for
     * @return List of applicants for the given recruitment
     */
    List<Applicant> findByRecruitment(String recruitmentId);
} 