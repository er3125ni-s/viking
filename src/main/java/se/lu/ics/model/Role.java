package se.lu.ics.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Domänklass för en roll/befattning.
 * Innehåller ingen databaskod – all persistens hanteras av RoleDao.
 */
public class Role {

    /** Primärnyckel – null tills DAO:n sparar objektet. */
    private Long id;

    private String title;
    private String description;
    private String department;

    /** Aggregat: alla rekryteringar som hör till rollen. */
    private final List<Recruitment> recruitments = new ArrayList<>();

    /** Hjälpfält för snabb visning i UI:t. */
    private int ongoingRecruitments;

    /* ---------- Konstruktorer ---------- */

    public Role() { /* för ramverk / JAXB / JavaFX */ }

    public Role(String title, String description, String department) {
        this.title       = title;
        this.description = description;
        this.department  = department;
    }

    /* ---------- Affärsmetoder ---------- */

    public void addRecruitment(Recruitment recruitment) {
        recruitments.add(recruitment);
        if (recruitment.getStatus() == RecruitmentStatus.ACTIVE) {
            ongoingRecruitments++;
        }
    }

    public void removeRecruitment(Recruitment recruitment) {
        recruitments.remove(recruitment);
        if (recruitment.getStatus() == RecruitmentStatus.ACTIVE) {
            ongoingRecruitments--;
        }
    }

    /* ---------- Getters / Setters ---------- */

    public Long   getId()          { return id; }
    public void   setId(Long id)   { this.id = id; }

    public String getTitle()       { return title; }
    public void   setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void   setDescription(String description) { this.description = description; }

    public String getDepartment()  { return department; }
    public void   setDepartment(String department) { this.department = department; }

    public List<Recruitment> getRecruitments() { return recruitments; }

    public int getOngoingRecruitments() { return ongoingRecruitments; }

    /* ---------- equals / hashCode ---------- */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role role)) return false;
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /* ---------- toString ---------- */

    @Override
    public String toString() {
        return title;
    }
}
