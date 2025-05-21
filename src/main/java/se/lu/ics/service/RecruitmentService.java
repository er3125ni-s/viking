package se.lu.ics.service;

import se.lu.ics.model.*;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class RecruitmentService {
    private DatabaseService dbService;
    private Connection connection;

    public RecruitmentService() {
        this.dbService = new DatabaseService();
        this.connection = dbService.getConnection();
        loadTestData(); // Load test data for demo purposes
    }
    
    /**
     * Get the database connection
     * @return the database connection
     */
    public Connection getConnection() {
        return connection;
    }
    
    /**
     * Load test data for demonstration purposes
     */
    private void loadTestData() {
        // Check if we already have data
        if (!Role.findAll(connection).isEmpty()) {
            return;
        }
        
        // Create roles
        Role developer = createRole("Software Developer", "Develops software applications", "IT");
        Role designer = createRole("UI/UX Designer", "Designs user interfaces", "Design");
        Role manager = createRole("Project Manager", "Manages projects", "Management");
        
        // Create recruitments
        Recruitment devRecruitment = createRecruitment(developer, LocalDate.now().plusMonths(1));
        Recruitment designRecruitment = createRecruitment(designer, LocalDate.now().plusMonths(2));
        
        // Create applicants
        Applicant applicant1 = createApplicant("John", "Doe", "john.doe@example.com", "123-456-7890");
        Applicant applicant2 = createApplicant("Jane", "Smith", "jane.smith@example.com", "098-765-4321");
        Applicant applicant3 = createApplicant("Bob", "Johnson", "bob.johnson@example.com", "555-123-4567");
        
        // Add applications
        addApplicantToRecruitment(applicant1, devRecruitment);
        addApplicantToRecruitment(applicant2, devRecruitment);
        addApplicantToRecruitment(applicant3, designRecruitment);
        
        // Schedule interviews
        scheduleInterview(devRecruitment, applicant1, LocalDateTime.now().plusDays(7), "Room 101", "Alice Manager");
        scheduleInterview(devRecruitment, applicant2, LocalDateTime.now().plusDays(8), "Room 102", "Bob Manager");
        scheduleInterview(designRecruitment, applicant3, LocalDateTime.now().plusDays(9), "Room 103", "Charlie Manager");
    }

    // Role management
    public Role createRole(String title, String description, String department) {
        Role role = new Role(title, description, department);
        role.save(connection);
        return role;
    }

    public List<Role> getAllRoles() {
        return Role.findAll(connection);
    }

    public Role getMostPopularRole() {
        Map<Role, Long> roleCounts = getRecruitmentCountsByRole();
        return roleCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    // Recruitment management
    public Recruitment createRecruitment(Role role, LocalDate applicationDeadline) {
        if (applicationDeadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Application deadline cannot be in the past");
        }
        Recruitment recruitment = new Recruitment(role, applicationDeadline);
        recruitment.save(connection);
        return recruitment;
    }

    public List<Recruitment> getRecruitmentsByDateRange(LocalDate start, LocalDate end) {
        return Recruitment.findByDateRange(connection, start, end);
    }

    public double getAverageDaysToAcceptance() {
        List<Recruitment> recruitments = Recruitment.findAll(connection);
        return recruitments.stream()
            .filter(r -> r.getOfferAcceptanceDate() != null)
            .mapToLong(Recruitment::getDaysToAcceptance)
            .average()
            .orElse(0.0);
    }

    public double getAverageInterviewsPerOffer() {
        List<Recruitment> recruitments = Recruitment.findAll(connection);
        return recruitments.stream()
            .filter(r -> r.getOfferCount() > 0)
            .mapToDouble(Recruitment::getInterviewsPerOffer)
            .average()
            .orElse(0.0);
    }

    // Applicant management
    public Applicant createApplicant(String firstName, String lastName, String email, String phone) {
        Applicant applicant = new Applicant(firstName, lastName, email, phone);
        applicant.save(connection);
        return applicant;
    }

    public void addApplicantToRecruitment(Applicant applicant, Recruitment recruitment) {
        applicant.applyForRecruitment(connection, recruitment);
    }

    public List<Recruitment> getApplicantHistory(Applicant applicant) {
        return applicant.loadApplicationHistory(connection);
    }

    // Interview management
    public Interview scheduleInterview(Recruitment recruitment, Applicant applicant,
                                     LocalDateTime dateTime, String location, String interviewer) {
        Interview interview = new Interview(recruitment, applicant, dateTime, location, interviewer);
        interview.save(connection);
        return interview;
    }

    public List<Interview> getInterviewSchedule() {
        return Interview.findAll(connection).stream()
            .sorted(Comparator.comparing(Interview::getDateTime))
            .collect(Collectors.toList());
    }

    public void cancelInterview(Interview interview) {
        interview.cancel();
        interview.update(connection);
    }

    public void rescheduleInterview(Interview interview, LocalDateTime newDateTime) {
        interview.reschedule(newDateTime);
        interview.update(connection);
    }

    // Statistics
    public Map<Role, Long> getRecruitmentCountsByRole() {
        List<Role> roles = Role.findAll(connection);
        Map<Role, Long> counts = new HashMap<>();
        
        for (Role role : roles) {
            long count = Recruitment.findByRole(connection, role).size();
            counts.put(role, count);
        }
        
        return counts;
    }

    public void rankApplicant(Applicant applicant, int rank) {
        applicant.setRank(rank);
        applicant.update(connection);
    }

    public List<Applicant> getRankedApplicants(Recruitment recruitment) {
        return Applicant.findByRecruitment(connection, recruitment).stream()
            .sorted(Comparator.comparingInt(Applicant::getRank))
            .collect(Collectors.toList());
    }
    
    public void close() {
        dbService.closeConnection();
    }
} 