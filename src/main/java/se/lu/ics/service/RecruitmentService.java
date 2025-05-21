package se.lu.ics.service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;

import se.lu.ics.dao.ApplicantDao;
import se.lu.ics.dao.InterviewDao;
import se.lu.ics.dao.RecruitmentDao;
import se.lu.ics.dao.RoleDao;
import se.lu.ics.dao.TransactionManager;
import se.lu.ics.model.Applicant;
import se.lu.ics.model.Interview;
import se.lu.ics.model.InterviewStatus;
import se.lu.ics.model.Recruitment;
import se.lu.ics.model.Role;
import se.lu.ics.exception.DataAccessException;
import se.lu.ics.exception.ServiceException;

/**
 * Service layer for recruitment operations.
 * Coordinates database operations across multiple entities.
 */
public class RecruitmentService {
    private final RoleDao roleDao;
    private final RecruitmentDao recruitmentDao;
    private final ApplicantDao applicantDao;
    private final InterviewDao interviewDao;
    private final TransactionManager transactionManager;
    
    /**
     * Constructor with dependency injection
     * @param roleDao The role DAO
     * @param recruitmentDao The recruitment DAO
     * @param applicantDao The applicant DAO
     * @param interviewDao The interview DAO
     * @param transactionManager The transaction manager
     */
    public RecruitmentService(RoleDao roleDao, RecruitmentDao recruitmentDao, 
                            ApplicantDao applicantDao, InterviewDao interviewDao,
                            TransactionManager transactionManager) {
        this.roleDao = roleDao;
        this.recruitmentDao = recruitmentDao;
        this.applicantDao = applicantDao;
        this.interviewDao = interviewDao;
        this.transactionManager = transactionManager;
    }
    
    // ========== Role Operations ==========
    
    /**
     * Get all roles
     * @return List of all roles
     * @throws ServiceException if a data access error occurs
     */
    public List<Role> getAllRoles() {
        try {
            return roleDao.findAll();
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to get all roles", e);
        }
    }
    
    /**
     * Add a new role
     * @param role The role to add
     * @throws ServiceException if a data access error occurs
     */
    public void addRole(Role role) {
        try {
            roleDao.insert(role);
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to add role: " + role.getTitle(), e);
        }
    }
    
    /**
     * Create a new role
     * @param title The role title
     * @param description The role description
     * @param department The department
     * @return The created role
     * @throws ServiceException if a data access error occurs
     */
    public Role createRole(String title, String description, String department) {
        Role role = new Role(title, department);
        role.setDescription(description);
        try {
            roleDao.insert(role);
            return role;
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to create role: " + title, e);
        }
    }
    
    /**
     * Find a role by ID
     * @param id The role ID
     * @return The role, if found
     * @throws ServiceException if a data access error occurs
     */
    public Optional<Role> findRole(long id) {
        try {
            return roleDao.find(String.valueOf(id));
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to find role with ID: " + id, e);
        }
    }
    
    // ========== Recruitment Operations ==========
    
    /**
     * Get all recruitments
     * @return List of all recruitments
     * @throws ServiceException if a data access error occurs
     */
    public List<Recruitment> getAllRecruitments() {
        try {
            return recruitmentDao.findAll();
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to get all recruitments", e);
        }
    }
    
    /**
     * Add a new recruitment
     * @param recruitment The recruitment to add
     * @throws ServiceException if a data access error occurs
     */
    public void addRecruitment(Recruitment recruitment) {
        try {
            recruitmentDao.insert(recruitment);
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to add recruitment for role: " + 
                                      recruitment.getRole().getTitle(), e);
        }
    }
    
    /**
     * Create a new recruitment
     * @param role The role
     * @param deadline The application deadline
     * @return The created recruitment
     * @throws ServiceException if a data access error occurs
     */
    public Recruitment createRecruitment(Role role, LocalDate deadline) {
        try {
            Recruitment recruitment = new Recruitment(role, deadline);
            recruitmentDao.insert(recruitment);
            return recruitment;
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to create recruitment for role: " + role.getTitle(), e);
        }
    }
    
    /**
     * Update an existing recruitment
     * @param recruitment The recruitment to update
     * @throws ServiceException if a data access error occurs
     */
    public void updateRecruitment(Recruitment recruitment) {
        try {
            recruitmentDao.update(recruitment);
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to update recruitment: " + recruitment.getId(), e);
        }
    }
    
    /**
     * Find a recruitment by ID
     * @param id The recruitment ID
     * @return The recruitment, if found
     * @throws ServiceException if a data access error occurs
     */
    public Optional<Recruitment> findRecruitment(String id) {
        try {
            return recruitmentDao.find(id);
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to find recruitment with ID: " + id, e);
        }
    }
    
    /**
     * Delete a recruitment by ID
     * @param id The recruitment ID to delete
     * @return true if successful, false otherwise
     * @throws ServiceException if a data access error occurs
     */
    public boolean deleteRecruitment(String id) {
        try {
            return transactionManager.executeInTransaction(conn -> {
                try {
                    // First delete related interviews
                    try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM interviews WHERE recruitment_id = ?")) {
                        stmt.setString(1, id);
                        stmt.executeUpdate();
                    }
                    
                    // Then delete applications
                    try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM applications WHERE recruitment_id = ?")) {
                        stmt.setString(1, id);
                        stmt.executeUpdate();
                    }
                    
                    // Finally delete the recruitment
                    try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM recruitments WHERE id = ?")) {
                        stmt.setString(1, id);
                        int rows = stmt.executeUpdate();
                        return rows > 0;
                    }
                } catch (SQLException e) {
                    throw new DataAccessException("Error deleting recruitment: " + id, e);
                }
            });
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to delete recruitment: " + id, e);
        }
    }
    
    // ========== Applicant Operations ==========
    
    /**
     * Find an applicant by ID
     * @param id The applicant ID
     * @return The applicant, if found
     * @throws ServiceException if a data access error occurs
     */
    public Optional<Applicant> findApplicant(String id) {
        try {
            return applicantDao.find(id);
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to find applicant with ID: " + id, e);
        }
    }
    
    /**
     * Get all applicants for a specific recruitment
     * @param recruitment The recruitment to get applicants for
     * @return List of applicants for the recruitment
     * @throws ServiceException if a data access error occurs
     */
    public List<Applicant> getApplicantsForRecruitment(Recruitment recruitment) {
        try {
            return applicantDao.findByRecruitment(recruitment.getId());
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to get applicants for recruitment: " + 
                                      recruitment.getId(), e);
        }
    }
    
    /**
     * Update an applicant's rank
     * @param applicant The applicant to update
     * @param newRank The new rank to assign
     * @throws ServiceException if a data access error or validation error occurs
     */
    public void updateApplicantRank(Applicant applicant, int newRank) {
        try {
            applicant.updateRank(newRank);
            applicantDao.update(applicant);
        } catch (IllegalArgumentException e) {
            throw new ServiceException("Invalid rank: " + e.getMessage(), e);
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to update applicant rank", e);
        }
    }
    
    /**
     * Get a list of ranked applicants for a specific recruitment, ordered by rank (highest first)
     * @param recruitment The recruitment to get applicants for
     * @return List of ranked applicants ordered by rank
     * @throws ServiceException if a data access error occurs
     */
    public List<Applicant> getRankedApplicantsForRecruitment(Recruitment recruitment) {
        try {
            List<Applicant> applicants = applicantDao.findByRecruitment(recruitment.getId());
            // Filter out unranked applicants (rank=0) and sort by rank descending
            List<Applicant> rankedApplicants = new ArrayList<>();
            for (Applicant applicant : applicants) {
                if (applicant.getRank() > 0) {
                    rankedApplicants.add(applicant);
                }
            }
            // Sort by rank (highest first)
            rankedApplicants.sort((a1, a2) -> Integer.compare(a2.getRank(), a1.getRank()));
            return rankedApplicants;
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to get ranked applicants", e);
        }
    }
    
    /**
     * Get a list of unranked applicants for a specific recruitment
     * @param recruitment The recruitment to get applicants for
     * @return List of unranked applicants
     * @throws ServiceException if a data access error occurs
     */
    public List<Applicant> getUnrankedApplicantsForRecruitment(Recruitment recruitment) {
        try {
            List<Applicant> applicants = applicantDao.findByRecruitment(recruitment.getId());
            // Filter to keep only unranked applicants (rank=0)
            return applicants.stream()
                .filter(a -> a.getRank() == 0)
                .collect(java.util.stream.Collectors.toList());
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to get unranked applicants", e);
        }
    }
    
    /**
     * Add an applicant to a recruitment
     * @param applicant The applicant to add
     * @param recruitment The recruitment to add the applicant to
     * @throws ServiceException if a data access error occurs
     */
    public void addApplicantToRecruitment(Applicant applicant, Recruitment recruitment) {
        try {
            // Use proper transaction handling
            transactionManager.executeInTransaction(conn -> {
                // First make sure the applicant exists in the applicants table
                Optional<Applicant> existingApplicant = applicantDao.find(applicant.getId());
                if (existingApplicant.isEmpty()) {
                    applicantDao.insert(applicant);
                }
                
                // Then create the application record
                try {
                    String sql = "INSERT INTO applications (applicant_id, recruitment_id, application_date) " +
                                "VALUES (?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, applicant.getId());
                        pstmt.setString(2, recruitment.getId());
                        
                        // Store as timestamp
                        long timestamp = applicant.getApplicationDate().atStartOfDay()
                                        .atZone(java.time.ZoneId.systemDefault())
                                        .toInstant().toEpochMilli();
                        pstmt.setLong(3, timestamp);
                        
                        // Check if application already exists
                        String checkSql = "SELECT COUNT(*) FROM applications WHERE applicant_id = ? AND recruitment_id = ?";
                        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                            checkStmt.setString(1, applicant.getId());
                            checkStmt.setString(2, recruitment.getId());
                            ResultSet rs = checkStmt.executeQuery();
                            rs.next();
                            int count = rs.getInt(1);
                            
                            if (count == 0) {
                                pstmt.executeUpdate();
                            }
                        }
                    }
                } catch (SQLException e) {
                    throw new DataAccessException("Error linking applicant to recruitment", e);
                }
                
                return null;
            });
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to add applicant to recruitment: " + 
                                     recruitment.getId(), e);
        }
    }
    
    /**
     * Remove an applicant from a recruitment
     * @param applicant The applicant to remove
     * @param recruitment The recruitment to remove the applicant from
     * @throws ServiceException if a data access error occurs
     */
    public void removeApplicantFromRecruitment(Applicant applicant, Recruitment recruitment) {
        try {
            transactionManager.executeInTransaction(conn -> {
                try {
                    // First delete any related interviews
                    String deleteInterviewsSql = "DELETE FROM interviews WHERE applicant_id = ? AND recruitment_id = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(deleteInterviewsSql)) {
                        pstmt.setString(1, applicant.getId());
                        pstmt.setString(2, recruitment.getId());
                        pstmt.executeUpdate();
                    }
                    
                    // Then delete the application record
                    String deleteApplicationSql = "DELETE FROM applications WHERE applicant_id = ? AND recruitment_id = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(deleteApplicationSql)) {
                        pstmt.setString(1, applicant.getId());
                        pstmt.setString(2, recruitment.getId());
                        pstmt.executeUpdate();
                    }
                    
                    return null;
                } catch (SQLException e) {
                    throw new DataAccessException("Error removing applicant from recruitment", e);
                }
            });
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to remove applicant from recruitment: " + 
                                     recruitment.getId(), e);
        }
    }
    
    // ========== Interview Operations ==========
    
    /**
     * Get all interviews
     * @return List of all interviews
     * @throws ServiceException if a data access error occurs
     */
    public List<Interview> getAllInterviews() {
        try {
            return interviewDao.findAll();
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to get all interviews", e);
        }
    }
    
    /**
     * Get all scheduled interviews
     * @return List of scheduled interviews
     * @throws ServiceException if a data access error occurs
     */
    public List<Interview> getInterviewSchedule() {
        try {
            List<Interview> interviews = interviewDao.findAll();
            // Filter to only include scheduled or rescheduled interviews
            return interviews.stream()
                .filter(i -> i.getStatus() == InterviewStatus.SCHEDULED || 
                          i.getStatus() == InterviewStatus.RESCHEDULED)
                .collect(java.util.stream.Collectors.toList());
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to get interview schedule", e);
        }
    }
    
    /**
     * Find an interview by ID
     * @param id The interview ID
     * @return The interview, if found
     * @throws ServiceException if a data access error occurs
     */
    public Optional<Interview> findInterview(String id) {
        try {
            return interviewDao.find(id);
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to find interview with ID: " + id, e);
        }
    }
    
    /**
     * Schedule a new interview
     * @param recruitment The recruitment the interview is for
     * @param applicant The applicant being interviewed
     * @param dateTime The date and time of the interview
     * @param location The location of the interview
     * @param interviewer The name of the interviewer
     * @return The newly created interview
     * @throws ServiceException if a data access error occurs
     */
    public Interview scheduleInterview(Recruitment recruitment, Applicant applicant,
                                     LocalDateTime dateTime, String location, String interviewer) {
        try {
            // First ensure the applicant is linked to this recruitment
            List<Applicant> recruitmentApplicants = applicantDao.findByRecruitment(recruitment.getId());
            boolean applicantFound = recruitmentApplicants.stream()
                .anyMatch(a -> a.getId().equals(applicant.getId()));
                
            if (!applicantFound) {
                // Add the applicant to the recruitment if not already there
                addApplicantToRecruitment(applicant, recruitment);
            }
            
            // Create and save the interview
            Interview interview = new Interview(recruitment, applicant, dateTime, location, interviewer);
            interviewDao.insert(interview);
            return interview;
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to schedule interview", e);
        }
    }
    
    /**
     * Reschedule an interview
     * @param interview The interview to reschedule
     * @param newDateTime The new date and time
     * @throws ServiceException if a data access error occurs
     */
    public void rescheduleInterview(Interview interview, LocalDateTime newDateTime) {
        try {
            interview.setDateTime(newDateTime);
            interview.setStatus(InterviewStatus.RESCHEDULED);
            interviewDao.update(interview);
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to reschedule interview", e);
        }
    }
    
    /**
     * Cancel an interview
     * @param interview The interview to cancel
     * @throws ServiceException if a data access error occurs or interview cannot be cancelled
     */
    public void cancelInterview(Interview interview) {
        try {
            interview.cancel();
            interviewDao.update(interview);
        } catch (IllegalStateException e) {
            // Re-throw with more context
            throw new ServiceException("Cannot cancel interview: " + e.getMessage(), e);
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to cancel interview", e);
        }
    }
    
    /**
     * Update an interview in the database
     * @param interview The interview to update
     * @throws ServiceException if a data access error occurs
     */
    public void updateInterview(Interview interview) {
        try {
            interviewDao.update(interview);
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to update interview", e);
        }
    }
    
    /**
     * Get the most popular role based on number of applicants
     * @return The most popular role, or null if no data available
     */
    public Role getMostPopularRole() {
        try {
            return transactionManager.executeInTransaction(conn -> {
                String sql = "SELECT r.role_id, COUNT(app.applicant_id) as applicant_count " +
                             "FROM recruitments r " +
                             "JOIN applications app ON r.id = app.recruitment_id " +
                             "GROUP BY r.role_id " +
                             "ORDER BY applicant_count DESC " +
                             "LIMIT 1";
                
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    try {
                        ResultSet rs = pstmt.executeQuery();
                        
                        if (rs.next()) {
                            String roleId = rs.getString("role_id");
                            Optional<Role> role = roleDao.find(roleId);
                            return role.orElse(null);
                        }
                        return null;
                    } catch (SQLException e) {
                        throw new DataAccessException("Error querying for most popular role", e);
                    }
                } catch (SQLException e) {
                    throw new DataAccessException("Error preparing statement for most popular role", e);
                }
            });
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to find most popular role", e);
        }
    }
    
    /**
     * Calculate the average days to acceptance
     * @return The average days to acceptance
     */
    public double getAverageDaysToAcceptance() {
        try {
            return transactionManager.executeInTransaction(conn -> {
                String sql = "SELECT AVG(JULIANDAY(offer_acceptance_date) - JULIANDAY(posting_date)) AS avg_days " +
                             "FROM recruitments " +
                             "WHERE status = 'FILLED' AND offer_acceptance_date IS NOT NULL";
                
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    ResultSet rs = pstmt.executeQuery();
                    
                    if (rs.next()) {
                        double avgDays = rs.getDouble("avg_days");
                        return Double.isNaN(avgDays) ? 0 : avgDays;
                    }
                    return 0.0;
                } catch (SQLException e) {
                    throw new DataAccessException("Error calculating average days to acceptance", e);
                }
            });
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to calculate average days to acceptance", e);
        }
    }
    
    /**
     * Calculate the average interviews per offer
     * @return The average interviews per offer
     */
    public double getAverageInterviewsPerOffer() {
        try {
            return transactionManager.executeInTransaction(conn -> {
                String sql = "SELECT AVG(interview_count) AS avg_interviews FROM " +
                            "(SELECT r.id, COUNT(i.id) AS interview_count " +
                            "FROM recruitments r " +
                            "LEFT JOIN interviews i ON r.id = i.recruitment_id " + 
                            "WHERE r.status = 'FILLED' " +
                            "GROUP BY r.id)";
                
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    ResultSet rs = pstmt.executeQuery();
                    
                    if (rs.next()) {
                        double avgInterviews = rs.getDouble("avg_interviews");
                        return Double.isNaN(avgInterviews) ? 0 : avgInterviews;
                    }
                    return 0.0;
                } catch (SQLException e) {
                    throw new DataAccessException("Error calculating average interviews per offer", e);
                }
            });
        } catch (DataAccessException e) {
            throw new ServiceException("Failed to calculate average interviews per offer", e);
        }
    }
}
