package se.lu.ics.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Domänklass som beskriver en rekryteringsprocess.
 * Innehåller ingen databaskod – allt persisteras via RecruitmentDao.
 */
public class Recruitment {

    /** Kravspec: "HR yyyy/x"-format (sparas som TEXT i databasen). */
    private String id;

    private Role role;
    private LocalDate applicationDeadline;
    private LocalDateTime postingDate;
    private LocalDateTime offerAcceptanceDate;
    private RecruitmentStatus status = RecruitmentStatus.ACTIVE;

    /* Samlingar */
    private final List<Applicant> applicants = new ArrayList<>();
    private final List<Interview> interviews = new ArrayList<>();

    /* ---------- Konstruktorer ---------- */

    /** För ramverk / DAO. */
    public Recruitment() {}

    public Recruitment(Role role, LocalDate applicationDeadline) {
        this.id = generateHrId();
        this.role = role;
        this.applicationDeadline = applicationDeadline;
        this.postingDate = LocalDateTime.now();
    }

    /* ---------- Affärsmetoder ---------- */

    /** Skapar ID enligt "HR <år>/<löpnummer>" – enkel millisekvens tills DAO sätter permanent värde. */
    private static String generateHrId() {
        int year = LocalDate.now().getYear();
        return "HR " + year + "/" + (System.currentTimeMillis() % 1000);
    }

    public void addApplicant(Applicant a)    { applicants.add(a); }
    public void removeApplicant(Applicant a) { applicants.remove(a); }

    public void addInterview(Interview i)    { interviews.add(i); }
    public void removeInterview(Interview i) { interviews.remove(i); }

    /** Anropas när ett erbjudande accepteras. */
    public void recordOfferAcceptance() {
        this.offerAcceptanceDate = LocalDateTime.now();
    }

    public long getDaysToAcceptance() {
        return offerAcceptanceDate == null
               ? 0
               : Duration.between(postingDate, offerAcceptanceDate).toDays();
    }

    public double getInterviewsPerOffer() {
        return offerAcceptanceDate == null ? 0 : (double) interviews.size();
    }

    /* ---------- Getters / Setters ---------- */

    public String           getId()                     { return id; }
    public void             setId(String id)            { this.id = id; }

    public Role             getRole()                   { return role; }
    public void             setRole(Role role)          { this.role = role; }

    public LocalDate        getApplicationDeadline()    { return applicationDeadline; }
    public void             setApplicationDeadline(LocalDate d) { this.applicationDeadline = d; }

    public LocalDateTime    getPostingDate()            { return postingDate; }
    public void             setPostingDate(LocalDateTime dt) { this.postingDate = dt; }

    public LocalDateTime    getOfferAcceptanceDate()    { return offerAcceptanceDate; }
    public RecruitmentStatus getStatus()                { return status; }
    public void             setStatus(RecruitmentStatus s) { this.status = s; }

    public List<Applicant>  getApplicants()             { return applicants; }
    public List<Interview>  getInterviews()             { return interviews; }

    /* ---------- equals / hashCode ---------- */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recruitment that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    /* ---------- toString ---------- */

    @Override
    public String toString() {
        return id + " - " + (role != null ? role.getTitle() : "<no role>");
    }
}
