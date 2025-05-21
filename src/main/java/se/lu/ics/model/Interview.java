package se.lu.ics.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Domänklass för en intervju­händelse.
 * Persistens hanteras av InterviewDao – här finns bara data + regler.
 */
public class Interview {

    /** Primärnyckel i formatet "INT-<timestamp>-<rand>" */
    private String id;

    private Recruitment      recruitment;
    private Applicant        applicant;
    private LocalDateTime    dateTime;
    private String           location;
    private String           interviewer;
    private InterviewStatus  status = InterviewStatus.SCHEDULED;
    private String           notes  = "";

    /* ---------- Konstruktorer ---------- */

    /** För ramverk / DAO:er. */
    public Interview() {}

    public Interview(Recruitment recruitment,
                     Applicant   applicant,
                     LocalDateTime dateTime,
                     String location,
                     String interviewer) {

        this.id          = generateId();
        this.recruitment = recruitment;
        this.applicant   = applicant;
        this.dateTime    = dateTime;
        this.location    = location;
        this.interviewer = interviewer;
    }

    public Interview(String id,
                     Recruitment recruitment,
                     Applicant applicant,
                     LocalDateTime dateTime,
                     String location,
                     String interviewer,
                     InterviewStatus status,
                     String notes) {

        this.id          = id;
        this.recruitment = recruitment;
        this.applicant   = applicant;
        this.dateTime    = dateTime;
        this.location    = location;
        this.interviewer = interviewer;
        this.status      = status;
        this.notes       = notes;
    }

    /* ---------- Affärsmetoder ---------- */

    private static String generateId() {
        return "INT-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000);
    }

    public void cancel()                 { this.status = InterviewStatus.CANCELLED; }
    public void complete()               { this.status = InterviewStatus.COMPLETED; }
    public void reschedule(LocalDateTime newDateTime) {
        this.dateTime = newDateTime;
        this.status   = InterviewStatus.RESCHEDULED;
    }

    /* ---------- Getters / Setters ---------- */

    public String         getId()          { return id; }
    public void           setId(String id) { this.id = id; }

    public Recruitment    getRecruitment() { return recruitment; }
    public void           setRecruitment(Recruitment r) { this.recruitment = r; }

    public Applicant      getApplicant()   { return applicant; }
    public void           setApplicant(Applicant a) { this.applicant = a; }

    public LocalDateTime  getDateTime()    { return dateTime; }
    public void           setDateTime(LocalDateTime dt) { this.dateTime = dt; }

    public String         getLocation()    { return location; }
    public void           setLocation(String location) { this.location = location; }

    public String         getInterviewer() { return interviewer; }
    public void           setInterviewer(String i) { this.interviewer = i; }

    public InterviewStatus getStatus()     { return status; }
    public void            setStatus(InterviewStatus s) { this.status = s; }

    public String         getNotes()       { return notes; }
    public void           setNotes(String n) { this.notes = n; }

    /* ---------- equals / hashCode ---------- */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Interview that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /* ---------- toString ---------- */

    @Override
    public String toString() {
        return String.format("%s - %s with %s",
                dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                applicant != null ? applicant.getFullName() : "<no applicant>",
                interviewer);
    }
}
