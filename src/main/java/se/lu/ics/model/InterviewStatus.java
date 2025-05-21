package se.lu.ics.model;

/**
 * Enum representing the status of an interview.
 */
public enum InterviewStatus {
    /**
     * Interview is scheduled but has not yet occurred
     */
    SCHEDULED,
    
    /**
     * Interview has been rescheduled to a new date/time
     */
    RESCHEDULED,
    
    /**
     * Interview has been completed
     */
    COMPLETED,
    
    /**
     * Interview has been cancelled
     */
    CANCELLED
} 