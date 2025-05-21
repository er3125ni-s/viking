package se.lu.ics.service;

import se.lu.ics.dao.*;
import se.lu.ics.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Tjänstelager för all HR-logik.
 * Inga SQL-anrop direkt – all persistens via DAO-gränssnitt.
 */
public class RecruitmentService {

    private final RoleDao        roleDao;
    private final RecruitmentDao recruitmentDao;
    private final ApplicantDao   applicantDao;
    private final InterviewDao   interviewDao;

    public RecruitmentService(RoleDao roleDao,
                              RecruitmentDao recruitmentDao,
                              ApplicantDao applicantDao,
                              InterviewDao interviewDao) {

        this.roleDao        = roleDao;
        this.recruitmentDao = recruitmentDao;
        this.applicantDao   = applicantDao;
        this.interviewDao   = interviewDao;
    }

    /* ---------- Role ---------- */

    public Role createRole(String title, String desc, String dept) {
        Role r = new Role(title, desc, dept);
        roleDao.insert(r);
        return r;
    }

    public List<Role> getAllRoles()                  { return roleDao.findAll(); }
    public Optional<Role> getRoleById(Long id)       { return roleDao.find(id); }

    public Role getMostPopularRole() {
        return getRecruitmentCountsByRole().entrySet().stream()
                                           .max(Map.Entry.comparingByValue)
                                           .map(Map.Entry::getKey)
                                           .orElse(null);
    }

    /* ---------- Recruitment ---------- */

    public Recruitment createRecruitment(Role role, LocalDate deadline) {
        if (deadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline cannot be in the past.");
        }
        Recruitment rec = new Recruitment(role, deadline);
        recruitmentDao.insert(rec);
        return rec;
    }

    public List<Recruitment> getAllRecruitments()           { return recruitmentDao.findAll(); }
    public Optional<Recruitment> getRecruitmentById(String id) { return recruitmentDao.find(id); }
    public List<Recruitment> getRecruitmentsByRole(Long roleId){ return recruitmentDao.findByRole(roleId); }

    public double getAverageDaysToAcceptance() {
        return recruitmentDao.findAll().stream()
                             .filter(r -> r.getOfferAcceptanceDate() != null)
                             .mapToLong(Recruitment::getDaysToAcceptance)
                             .average()
                             .orElse(0.0);
    }

    public double getAverageInterviewsPerOffer() {
        return recruitmentDao.findAll().stream()
                             .filter(r -> r.getOfferCount() > 0)
                             .mapToDouble(Recruitment::getInterviewsPerOffer)
                             .average()
                             .orElse(0.0);
    }

    /* ---------- Applicant ---------- */

    public Applicant createApplicant(String first, String last, String email, String phone) {
        Applicant a = new Applicant(first, last, email, phone);
        applicantDao.insert(a);
        return a;
    }

    public List<Applicant> getAllApplicants()                   { return applicantDao.findAll(); }
    public Optional<Applicant> getApplicantById(String id)      { return applicantDao.find(id); }
    public List<Applicant> getApplicantsByRecruitment(String rid){ return applicantDao.findByRecruitment(rid); }

    /* ---------- Interview ---------- */

    public Interview scheduleInterview(Recruitment recruitment,
                                       Applicant applicant,
                                       LocalDateTime dateTime,
                                       String location,
                                       String interviewer,
                                       InterviewStatus status,
                                       String notes) {

        Interview i = new Interview(recruitment, applicant, dateTime, location, interviewer);
        i.setStatus(status);
        i.setNotes(notes);
        interviewDao.insert(i);
        return i;
    }

    public List<Interview> getAllInterviews()                   { return interviewDao.findAll(); }
    public Optional<Interview> getInterviewById(String id)      { return interviewDao.find(id); }
    public List<Interview> getInterviewsByApplicant(String aid) { return interviewDao.findByApplicant(aid); }

    public List<Interview> getInterviewSchedule() {
        return interviewDao.findAll().stream()
                           .sorted(Comparator.comparing(Interview::getDateTime))
                           .toList();
    }

    public void updateInterview(Interview i) { interviewDao.update(i); }

    /* ---------- Statistik ---------- */

    public Map<Role, Long> getRecruitmentCountsByRole() {
        Map<Role, Long> counts = new HashMap<>();
        for (Role role : roleDao.findAll()) {
            counts.put(role, (long) recruitmentDao.findByRole(role.getId()).size());
        }
        return counts;
    }
}
