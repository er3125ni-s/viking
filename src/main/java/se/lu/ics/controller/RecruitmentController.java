package se.lu.ics.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import se.lu.ics.model.*;
import se.lu.ics.service.RecruitmentService;
import se.lu.ics.App;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for recruitment-specific operations
 */
public class RecruitmentController {
    private RecruitmentService recruitmentService;
    private Recruitment currentRecruitment;
    
    public RecruitmentController() {
        this.recruitmentService = App.getRecruitmentService();
    }
    
    /**
     * Create a new applicant dialog
     */
    public Applicant showNewApplicantDialog() {
        Dialog<Applicant> dialog = new Dialog<>();
        dialog.setTitle("New Applicant");
        dialog.setHeaderText("Create a new applicant");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        TextField emailField = new TextField();
        TextField phoneField = new TextField();
        
        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() || 
                    emailField.getText().isEmpty()) {
                    showError("Invalid Input", "First name, last name, and email are required.");
                    return null;
                }
                
                return recruitmentService.createApplicant(
                    firstNameField.getText(),
                    lastNameField.getText(),
                    emailField.getText(),
                    phoneField.getText()
                );
            }
            return null;
        });
        
        return dialog.showAndWait().orElse(null);
    }
    
    /**
     * Show applicant history dialog
     */
    public void showApplicantHistoryDialog(Applicant applicant) {
        if (applicant == null) {
            showError("No Applicant", "No applicant selected.");
            return;
        }
        
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Application History");
        dialog.setHeaderText("Application History for " + applicant.getFullName());
        
        TableView<Recruitment> historyTable = new TableView<>();
        TableColumn<Recruitment, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Recruitment, String> roleColumn = new TableColumn<>("Role");
        roleColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRole().getTitle()));
        
        TableColumn<Recruitment, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus().toString()));
        
        historyTable.getColumns().addAll(idColumn, roleColumn, statusColumn);
        historyTable.setPrefWidth(400);
        historyTable.setPrefHeight(300);
        
        List<Recruitment> history = recruitmentService.getApplicantHistory(applicant);
        historyTable.setItems(FXCollections.observableArrayList(history));
        
        dialog.getDialogPane().setContent(historyTable);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        dialog.showAndWait();
    }
    
    /**
     * Show interview booking dialog
     */
    public Interview showInterviewBookingDialog(Recruitment recruitment, Applicant applicant) {
        if (recruitment == null || applicant == null) {
            showError("Missing Information", "Recruitment and applicant must be selected.");
            return null;
        }
        
        // Check if application deadline has passed
        if (recruitment.getApplicationDeadline().isBefore(LocalDate.now())) {
            showError("Deadline Passed", "Cannot schedule interviews for recruitments with passed deadlines.");
            return null;
        }
        
        Dialog<Interview> dialog = new Dialog<>();
        dialog.setTitle("Book Interview");
        dialog.setHeaderText("Book interview for " + applicant.getFullName());
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
        TextField timeField = new TextField("10:00");
        TextField locationField = new TextField("Meeting Room 1");
        TextField interviewerField = new TextField();
        
        grid.add(new Label("Date:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("Time (HH:MM):"), 0, 1);
        grid.add(timeField, 1, 1);
        grid.add(new Label("Location:"), 0, 2);
        grid.add(locationField, 1, 2);
        grid.add(new Label("Interviewer:"), 0, 3);
        grid.add(interviewerField, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType bookButtonType = new ButtonType("Book", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(bookButtonType, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == bookButtonType) {
                try {
                    if (interviewerField.getText().isEmpty()) {
                        showError("Missing Information", "Interviewer name is required.");
                        return null;
                    }
                    
                    String[] timeParts = timeField.getText().split(":");
                    int hour = Integer.parseInt(timeParts[0]);
                    int minute = Integer.parseInt(timeParts[1]);
                    
                    LocalDateTime interviewDateTime = datePicker.getValue().atTime(hour, minute);
                    
                    // Ensure interview date is not in the past
                    if (interviewDateTime.isBefore(LocalDateTime.now())) {
                        showError("Invalid Date", "Interview date cannot be in the past.");
                        return null;
                    }
                    
                    return recruitmentService.scheduleInterview(
                        recruitment,
                        applicant,
                        interviewDateTime,
                        locationField.getText(),
                        interviewerField.getText()
                    );
                } catch (Exception e) {
                    showError("Invalid Input", "Please check your input: " + e.getMessage());
                }
            }
            return null;
        });
        
        return dialog.showAndWait().orElse(null);
    }
    
    /**
     * Show applicant ranking dialog
     */
    public void showApplicantRankingDialog(Recruitment recruitment) {
        if (recruitment == null) {
            showError("No Recruitment", "No recruitment selected.");
            return;
        }
        
        List<Applicant> applicants = Applicant.findByRecruitment(
            recruitmentService.getConnection(), recruitment);
        
        if (applicants.isEmpty()) {
            showError("No Applicants", "This recruitment has no applicants.");
            return;
        }
        
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Rank Applicants");
        dialog.setHeaderText("Rank applicants for " + recruitment.getRole().getTitle());
        
        TableView<Applicant> applicantTable = new TableView<>();
        TableColumn<Applicant, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFullName()));
        
        TableColumn<Applicant, Integer> rankColumn = new TableColumn<>("Rank");
        rankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
        
        // Add a column with ranking controls
        TableColumn<Applicant, Void> actionColumn = new TableColumn<>("Actions");
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final ComboBox<Integer> rankCombo = new ComboBox<>(
                FXCollections.observableArrayList(1, 2, 3, 4, 5));
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                
                Applicant applicant = getTableView().getItems().get(getIndex());
                rankCombo.setValue(applicant.getRank());
                rankCombo.setOnAction(event -> {
                    applicant.setRank(rankCombo.getValue());
                    applicant.update(recruitmentService.getConnection());
                });
                
                setGraphic(rankCombo);
            }
        });
        
        applicantTable.getColumns().addAll(nameColumn, rankColumn, actionColumn);
        applicantTable.setPrefWidth(400);
        applicantTable.setPrefHeight(300);
        applicantTable.setItems(FXCollections.observableArrayList(applicants));
        
        dialog.getDialogPane().setContent(applicantTable);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        dialog.showAndWait();
    }
    
    /**
     * Show recruitment statistics dialog
     */
    public void showRecruitmentStatisticsDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Recruitment Statistics");
        dialog.setHeaderText("Detailed Recruitment Statistics");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        
        // Calculate statistics
        double avgDays = recruitmentService.getAverageDaysToAcceptance();
        double avgInterviews = recruitmentService.getAverageInterviewsPerOffer();
        Role popularRole = recruitmentService.getMostPopularRole();
        
        int activeRecruitments = countActiveRecruitments();
        double avgApplicants = calculateAverageApplicants();
        
        grid.add(new Label("Average Days to Acceptance:"), 0, 0);
        grid.add(new Label(String.format("%.1f days", avgDays)), 1, 0);
        
        grid.add(new Label("Average Interviews per Offer:"), 0, 1);
        grid.add(new Label(String.format("%.1f", avgInterviews)), 1, 1);
        
        grid.add(new Label("Most Popular Role:"), 0, 2);
        grid.add(new Label(popularRole != null ? popularRole.getTitle() : "N/A"), 1, 2);
        
        grid.add(new Label("Active Recruitments:"), 0, 3);
        grid.add(new Label(String.valueOf(activeRecruitments)), 1, 3);
        
        grid.add(new Label("Average Applicants per Recruitment:"), 0, 4);
        grid.add(new Label(String.format("%.1f", avgApplicants)), 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        dialog.showAndWait();
    }
    
    /**
     * Count active recruitments
     */
    private int countActiveRecruitments() {
        List<Recruitment> recruitments = Recruitment.findAll(recruitmentService.getConnection());
        return (int) recruitments.stream()
            .filter(r -> r.getStatus() == RecruitmentStatus.ACTIVE)
            .count();
    }
    
    /**
     * Calculate average applicants per recruitment
     */
    private double calculateAverageApplicants() {
        List<Recruitment> recruitments = Recruitment.findAll(recruitmentService.getConnection());
        if (recruitments.isEmpty()) {
            return 0.0;
        }
        
        int totalApplicants = 0;
        for (Recruitment recruitment : recruitments) {
            totalApplicants += recruitment.countApplicants(recruitmentService.getConnection());
        }
        
        return (double) totalApplicants / recruitments.size();
    }
    
    /**
     * Show error dialog
     */
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 