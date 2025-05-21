package se.lu.ics.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Recruitment {
    private String id; // Format: HR yyyy/x
    private Role role;
    private LocalDate postingDate;
    private LocalDate applicationDeadline;
    private List<Applicant> applicants;
    private List<Interview> interviews;
    private boolean isActive;
    private LocalDate offerAcceptanceDate;

    public Recruitment(String id, Role role, LocalDate postingDate, LocalDate applicationDeadline) {
        this.id = id;
        this.role = role;
        this.postingDate = postingDate;
        this.applicationDeadline = applicationDeadline;
        this.applicants = new ArrayList<>();
        this.interviews = new ArrayList<>();
        this.isActive = true;
        this.offerAcceptanceDate = null;
        role.incrementOngoingRecruitments();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public LocalDate getPostingDate() {
        return postingDate;
    }

    public LocalDate getApplicationDeadline() {
        return applicationDeadline;
    }

    public void setApplicationDeadline(LocalDate applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }

    public List<Applicant> getApplicants() {
        return applicants;
    }

    public void addApplicant(Applicant applicant) {
        this.applicants.add(applicant);
    }

    public List<Interview> getInterviews() {
        return interviews;
    }

    public void addInterview(Interview interview) {
        this.interviews.add(interview);
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        if (this.isActive && !active) {
            role.decrementOngoingRecruitments();
        } else if (!this.isActive && active) {
            role.incrementOngoingRecruitments();
        }
        this.isActive = active;
    }

    public LocalDate getOfferAcceptanceDate() {
        return offerAcceptanceDate;
    }

    public void setOfferAcceptanceDate(LocalDate offerAcceptanceDate) {
        this.offerAcceptanceDate = offerAcceptanceDate;
    }

    public int getTotalApplicants() {
        return applicants.size();
    }

    public int getTotalInterviews() {
        return interviews.size();
    }

    public long getDaysToAcceptance() {
        if (offerAcceptanceDate != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(postingDate, offerAcceptanceDate);
        }
        return -1;
    }
} 