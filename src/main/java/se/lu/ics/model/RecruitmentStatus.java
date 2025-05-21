package se.lu.ics.model;

/**
 * Enum representing the status of a recruitment process.
 */
public enum RecruitmentStatus {
    /**
     * Recruitment is open and accepting applications
     */
    OPEN,
    
    /**
     * Recruitment is in the interviewing phase
     */
    INTERVIEWING,
    
    /**
     * Recruitment is in process (reviewing applications, interviewing)
     */
    IN_PROGRESS,
    
    /**
     * Recruitment is in the offer phase
     */
    OFFER_PHASE,
    
    /**
     * Recruitment has been completed successfully
     */
    COMPLETED,
    
    /**
     * Recruitment has been filled with a successful candidate
     */
    FILLED,
    
    /**
     * Recruitment has been closed without hiring
     */
    CLOSED,
    
    /**
     * Recruitment has been cancelled
     */
    CANCELLED;

    @Override
    public String toString() {
        return name().replace("_", " ");
    }
} 