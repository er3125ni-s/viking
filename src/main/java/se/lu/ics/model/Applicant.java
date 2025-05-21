package se.lu.ics.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Domänklass som beskriver en kandidat.
 * Persistens sköts av ApplicantDao – den här klassen är ren från JDBC-kod.
 */
public class Applicant {

    /** Primärnyckel i formatet "APP-<timestamp>-<rand>" */
    private String id;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    private final List<Recruitment> applicationHistory = new ArrayList<>();

    /** Poäng/ranking satt av HR (0–100) */
    private int rank;

    private LocalDateTime applicationDate = LocalDateTime.now();

    /* ---------- Konstruktorer ---------- */

    /** För ramverk / DAO. */
    public Applicant() {}

    public Applicant(String firstName, String lastName, String email, String phone) {
        this.id        = generateId();
        this.firstName = firstName;
        this.lastName  = lastName;
        this.email     = email;
        this.phone     = phone;
    }

    public Applicant(String id,
                     String firstName,
                     String lastName,
                     String email,
                     String phone,
                     LocalDateTime applicationDate,
                     int rank) {

        this.id              = id;
        this.firstName       = firstName;
        this.lastName        = lastName;
        this.email           = email;
        this.phone           = phone;
        this.applicationDate = applicationDate;
        this.rank            = rank;
    }

    /* ---------- Affärsmetoder ---------- */

    private static String generateId() {
        return "APP-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000);
    }

    public String getFullName() { return firstName + " " + lastName; }

    public void addApplication(Recruitment recruitment)   { applicationHistory.add(recruitment); }
    public void removeApplication(Recruitment recruitment){ applicationHistory.remove(recruitment); }
    public boolean hasAppliedFor(Recruitment r)           { return applicationHistory.contains(r); }

    /* ---------- Getters / Setters ---------- */

    public String        getId()               { return id; }
    public void          setId(String id)      { this.id = id; }

    public String        getFirstName()        { return firstName; }
    public void          setFirstName(String n){ this.firstName = n; }

    public String        getLastName()         { return lastName; }
    public void          setLastName(String n) { this.lastName = n; }

    public String        getEmail()            { return email; }
    public void          setEmail(String e)    { this.email = e; }

    public String        getPhone()            { return phone; }
    public void          setPhone(String p)    { this.phone = p; }

    public List<Recruitment> getApplicationHistory() { return applicationHistory; }

    public int           getRank()             { return rank; }
    public void          setRank(int r)        { this.rank = r; }

    public LocalDateTime getApplicationDate()  { return applicationDate; }
    public void          setApplicationDate(LocalDateTime d) { this.applicationDate = d; }

    /* ---------- equals / hashCode ---------- */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Applicant that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    /* ---------- toString ---------- */

    @Override
    public String toString() { return getFullName(); }
}
