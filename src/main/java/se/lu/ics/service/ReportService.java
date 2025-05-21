package se.lu.ics.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import se.lu.ics.dao.ApplicantDao;
import se.lu.ics.dao.InterviewDao;
import se.lu.ics.dao.RecruitmentDao;
import se.lu.ics.dao.TransactionManager;
import se.lu.ics.model.Applicant;
import se.lu.ics.model.Interview;
import se.lu.ics.model.InterviewStatus;
import se.lu.ics.model.Recruitment;
import se.lu.ics.model.RecruitmentStatus;
import se.lu.ics.model.Role;

/**
 * Service for generating recruitment reports and analytics.
 */
public class ReportService {
    private final RecruitmentDao recruitmentDao;
    private final ApplicantDao applicantDao;
    private final InterviewDao interviewDao;
    private final TransactionManager transactionManager;
    
    /**
     * Constructor with dependency injection
     * @param recruitmentDao The recruitment DAO
     * @param applicantDao The applicant DAO
     * @param interviewDao The interview DAO
     * @param transactionManager The transaction manager
     */
    public ReportService(RecruitmentDao recruitmentDao, ApplicantDao applicantDao, 
                         InterviewDao interviewDao, TransactionManager transactionManager) {
        this.recruitmentDao = recruitmentDao;
        this.applicantDao = applicantDao;
        this.interviewDao = interviewDao;
        this.transactionManager = transactionManager;
    }
    
    /**
     * Generate a summary report for all recruitments
     * @return Map containing summary statistics
     */
    public Map<String, Object> generateSummaryReport() {
        Map<String, Object> report = new HashMap<>();
        
        List<Recruitment> allRecruitments = recruitmentDao.findAll();
        report.put("totalRecruitments", allRecruitments.size());
        
        // Count by status
        Map<RecruitmentStatus, Long> recruitmentsByStatus = allRecruitments.stream()
            .collect(Collectors.groupingBy(Recruitment::getStatus, Collectors.counting()));
        report.put("recruitmentsByStatus", recruitmentsByStatus);
        
        // Average time to fill (for completed recruitments)
        List<Recruitment> completedRecruitments = allRecruitments.stream()
            .filter(r -> r.getStatus() == RecruitmentStatus.FILLED && r.getOfferAcceptanceDate() != null)
            .collect(Collectors.toList());
        
        if (!completedRecruitments.isEmpty()) {
            double avgDaysToFill = completedRecruitments.stream()
                .mapToLong(r -> Period.between(r.getPostingDate(), r.getOfferAcceptanceDate()).getDays())
                .average()
                .orElse(0);
            report.put("avgDaysToFill", avgDaysToFill);
        } else {
            report.put("avgDaysToFill", 0);
        }
        
        // Count applicants
        int totalApplicants = applicantDao.countAll();
        report.put("totalApplicants", totalApplicants);
        
        if (!allRecruitments.isEmpty() && totalApplicants > 0) {
            report.put("avgApplicantsPerRecruitment", (double) totalApplicants / allRecruitments.size());
        } else {
            report.put("avgApplicantsPerRecruitment", 0);
        }
        
        return report;
    }
    
    /**
     * Generate a detailed report for a specific recruitment
     * @param recruitment The recruitment to generate a report for
     * @return Map containing detailed statistics
     */
    public Map<String, Object> generateRecruitmentReport(Recruitment recruitment) {
        Map<String, Object> report = new HashMap<>();
        
        report.put("recruitmentId", recruitment.getId());
        report.put("role", recruitment.getRole().getTitle());
        report.put("department", recruitment.getRole().getDepartment());
        report.put("status", recruitment.getStatus().toString());
        report.put("postingDate", recruitment.getPostingDate());
        report.put("applicationDeadline", recruitment.getApplicationDeadline());
        
        // Applicant statistics
        List<Applicant> applicants = applicantDao.findByRecruitment(recruitment.getId());
        report.put("totalApplicants", applicants.size());
        
        // Rank distribution
        Map<Integer, Long> rankDistribution = applicants.stream()
            .collect(Collectors.groupingBy(Applicant::getRank, Collectors.counting()));
        report.put("rankDistribution", rankDistribution);
        
        // Interview statistics
        List<Interview> interviews = interviewDao.findByRecruitment(recruitment.getId());
        report.put("totalInterviews", interviews.size());
        
        Map<InterviewStatus, Long> interviewsByStatus = interviews.stream()
            .collect(Collectors.groupingBy(Interview::getStatus, Collectors.counting()));
        report.put("interviewsByStatus", interviewsByStatus);
        
        // Calculate days active
        LocalDate today = LocalDate.now();
        int daysActive = Period.between(recruitment.getPostingDate(), 
                                      recruitment.getOfferAcceptanceDate() != null ? 
                                      recruitment.getOfferAcceptanceDate() : today).getDays();
        report.put("daysActive", daysActive);
        
        return report;
    }
    
    /**
     * Generate a department-based report
     * @return Map of department names to their statistics
     */
    public Map<String, Map<String, Object>> generateDepartmentReport() {
        Map<String, Map<String, Object>> report = new HashMap<>();
        
        // Group recruitments by department
        List<Recruitment> allRecruitments = recruitmentDao.findAll();
        
        Map<String, List<Recruitment>> recruitmentsByDept = allRecruitments.stream()
            .collect(Collectors.groupingBy(r -> r.getRole().getDepartment()));
        
        // For each department, calculate statistics
        for (Map.Entry<String, List<Recruitment>> entry : recruitmentsByDept.entrySet()) {
            String department = entry.getKey();
            List<Recruitment> deptRecruitments = entry.getValue();
            
            Map<String, Object> deptStats = new HashMap<>();
            deptStats.put("recruitmentCount", deptRecruitments.size());
            
            // Count open recruitments
            long openCount = deptRecruitments.stream()
                .filter(r -> r.getStatus() == RecruitmentStatus.OPEN)
                .count();
            deptStats.put("openRecruitments", openCount);
            
            // Calculate average fill time for completed recruitments
            List<Recruitment> completedRecruitments = deptRecruitments.stream()
                .filter(r -> r.getStatus() == RecruitmentStatus.FILLED && r.getOfferAcceptanceDate() != null)
                .collect(Collectors.toList());
            
            if (!completedRecruitments.isEmpty()) {
                double avgDaysToFill = completedRecruitments.stream()
                    .mapToLong(r -> Period.between(r.getPostingDate(), r.getOfferAcceptanceDate()).getDays())
                    .average()
                    .orElse(0);
                deptStats.put("avgDaysToFill", avgDaysToFill);
            } else {
                deptStats.put("avgDaysToFill", 0);
            }
            
            report.put(department, deptStats);
        }
        
        return report;
    }
    
    /**
     * Get the most efficient recruitment process (shortest time to fill)
     * @return The recruitment with the shortest time to fill, or null if none found
     */
    public Recruitment getMostEfficientRecruitment() {
        List<Recruitment> completedRecruitments = recruitmentDao.findAll().stream()
            .filter(r -> r.getStatus() == RecruitmentStatus.FILLED && r.getOfferAcceptanceDate() != null)
            .collect(Collectors.toList());
        
        if (completedRecruitments.isEmpty()) {
            return null;
        }
        
        return completedRecruitments.stream()
            .reduce((r1, r2) -> {
                int days1 = Period.between(r1.getPostingDate(), r1.getOfferAcceptanceDate()).getDays();
                int days2 = Period.between(r2.getPostingDate(), r2.getOfferAcceptanceDate()).getDays();
                return days1 < days2 ? r1 : r2;
            })
            .orElse(null);
    }
    
    /**
     * Get the recruitment with the most applicants
     * @return The recruitment with the most applicants, or null if none found
     */
    public Recruitment getMostPopularRecruitment() {
        List<Recruitment> allRecruitments = recruitmentDao.findAll();
        
        if (allRecruitments.isEmpty()) {
            return null;
        }
        
        return allRecruitments.stream()
            .reduce((r1, r2) -> {
                int count1 = applicantDao.findByRecruitment(r1.getId()).size();
                int count2 = applicantDao.findByRecruitment(r2.getId()).size();
                return count1 > count2 ? r1 : r2;
            })
            .orElse(null);
    }
    
    /**
     * Get the most popular role (with most applicants across all recruitments)
     * @return The most popular role
     */
    public Role getMostPopularRole() {
        // Get all recruitments
        List<Recruitment> allRecruitments = recruitmentDao.findAll();
        
        if (allRecruitments.isEmpty()) {
            return null;
        }
        
        // Count applicants by role
        Map<Role, Integer> applicantsByRole = new HashMap<>();
        
        for (Recruitment recruitment : allRecruitments) {
            Role role = recruitment.getRole();
            int count = applicantDao.findByRecruitment(recruitment.getId()).size();
            
            applicantsByRole.merge(role, count, Integer::sum);
        }
        
        // Find the role with the most applicants
        if (applicantsByRole.isEmpty()) {
            return null;
        }
        
        return applicantsByRole.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    /**
     * Calculate the average days from posting to acceptance across all filled positions
     * @return The average days to acceptance, or 0 if no data available
     */
    public double getAverageDaysToAcceptance() {
        // Get completed recruitments
        List<Recruitment> completedRecruitments = recruitmentDao.findAll().stream()
            .filter(r -> r.getStatus() == RecruitmentStatus.FILLED && r.getOfferAcceptanceDate() != null)
            .collect(Collectors.toList());
        
        // Calculate average time to fill
        if (!completedRecruitments.isEmpty()) {
            return completedRecruitments.stream()
                .mapToLong(r -> Period.between(r.getPostingDate(), r.getOfferAcceptanceDate()).getDays())
                .average()
                .orElse(0);
        }
        
        return 0.0;
    }
    
    /**
     * Calculate the average number of interviews conducted per filled position
     * @return The average number of interviews, or 0 if no data available
     */
    public double getAverageInterviewsPerOffer() {
        // Get completed recruitments
        List<Recruitment> completedRecruitments = recruitmentDao.findAll().stream()
            .filter(r -> r.getStatus() == RecruitmentStatus.FILLED)
            .collect(Collectors.toList());
        
        if (completedRecruitments.isEmpty()) {
            return 0.0;
        }
        
        // Count total interviews for filled positions
        int totalInterviews = 0;
        for (Recruitment recruitment : completedRecruitments) {
            totalInterviews += interviewDao.findByRecruitment(recruitment.getId()).size();
        }
        
        // Calculate average
        return (double) totalInterviews / completedRecruitments.size();
    }
} 