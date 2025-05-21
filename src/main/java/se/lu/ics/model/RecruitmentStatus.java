package se.lu.ics.model;

public enum RecruitmentStatus {
    ACTIVE("Active"),
    ON_HOLD("On Hold"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    OFFER_ACCEPTED("Offer Accepted"),
    OFFER_DECLINED("Offer Declined");

    private final String displayName;

    RecruitmentStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
} 