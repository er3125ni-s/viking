package se.lu.ics.model;

public enum InterviewStatus {
    SCHEDULED("Scheduled"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    RESCHEDULED("Rescheduled"),
    NO_SHOW("No Show");

    private final String displayName;

    InterviewStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
} 