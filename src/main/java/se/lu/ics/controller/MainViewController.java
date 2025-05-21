package se.lu.ics.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import se.lu.ics.model.*;
import se.lu.ics.service.RecruitmentService;
import se.lu.ics.App;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainViewController {
    @FXML private TableView<Recruitment> recruitmentTable;
    @FXML private TableColumn<Recruitment, String> idColumn;
    @FXML private TableColumn<Recruitment, String> roleColumn;
    @FXML private TableColumn<Recruitment, String> deadlineColumn;
    @FXML private TableColumn<Recruitment, Integer> applicantsColumn;
    @FXML private TableColumn<Recruitment, String> statusColumn;

    @FXML private TableView<Interview> interviewTable;
    @FXML private TableColumn<Interview, String> interviewDateColumn;
    @FXML private TableColumn<Interview, String> interviewApplicantColumn;
    @FXML private TableColumn<Interview, String> interviewRoleColumn;
    @FXML private TableColumn<Interview, String> interviewerColumn;
    @FXML private TableColumn<Interview, String> interviewStatusColumn;

    @FXML private Label avgDaysLabel;
    @FXML private Label avgInterviewsLabel;
    @FXML private Label popularRoleLabel;

    private RecruitmentService recruitmentService;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        recruitmentService = App.getRecruitmentService();
        setupRecruitmentTable();
        setupInterviewTable();
        loadRecruitments();
        loadInterviews();
        updateStatistics();
    }

    private void setupRecruitmentTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        roleColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getRole().getTitle()));
        deadlineColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getApplicationDeadline().format(dateFormatter)));
        applicantsColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getApplicantCount()).asObject());
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus().toString()));
            
        // Add context menu for recruitment table
        ContextMenu contextMenu = new ContextMenu();
        MenuItem viewApplicantsItem = new MenuItem("View Applicants");
        viewApplicantsItem.setOnAction(e -> handleViewApplicants());
        MenuItem editRecruitmentItem = new MenuItem("Edit Recruitment");
        editRecruitmentItem.setOnAction(e -> handleEditRecruitment());
        MenuItem deleteRecruitmentItem = new MenuItem("Delete Recruitment");
        deleteRecruitmentItem.setOnAction(e -> handleDeleteRecruitment());
        contextMenu.getItems().addAll(viewApplicantsItem, editRecruitmentItem, deleteRecruitmentItem);
        
        recruitmentTable.setContextMenu(contextMenu);
    }

    private void setupInterviewTable() {
        interviewDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDateTime().format(dateTimeFormatter)));
        interviewApplicantColumn.setCellValueFactory(cellData -> {
            Interview interview = cellData.getValue();
            if (interview.getApplicant() == null) {
                return new SimpleStringProperty("N/A");
            }
            return new SimpleStringProperty(interview.getApplicant().getFullName());
        });
        interviewRoleColumn.setCellValueFactory(cellData -> {
            Interview interview = cellData.getValue();
            if (interview.getRecruitment() == null || interview.getRecruitment().getRole() == null) {
                return new SimpleStringProperty("N/A");
            }
            return new SimpleStringProperty(interview.getRecruitment().getRole().getTitle());
        });
        interviewerColumn.setCellValueFactory(new PropertyValueFactory<>("interviewer"));
        interviewStatusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus().toString()));
            
        // Add context menu for interview table
        ContextMenu contextMenu = new ContextMenu();
        MenuItem rescheduleItem = new MenuItem("Reschedule Interview");
        rescheduleItem.setOnAction(e -> handleRescheduleInterview());
        MenuItem cancelItem = new MenuItem("Cancel Interview");
        cancelItem.setOnAction(e -> handleCancelInterview());
        MenuItem completeItem = new MenuItem("Complete Interview");
        completeItem.setOnAction(e -> handleCompleteInterview());
        contextMenu.getItems().addAll(rescheduleItem, cancelItem, completeItem);
        
        interviewTable.setContextMenu(contextMenu);
    }
    
    private void loadRecruitments() {
        List<Recruitment> recruitments = recruitmentService.getRecruitmentsByDateRange(
            LocalDate.now().minusYears(1), LocalDate.now().plusYears(1));
        recruitmentTable.setItems(FXCollections.observableArrayList(recruitments));
    }
    
    private void loadInterviews() {
        List<Interview> interviews = recruitmentService.getInterviewSchedule();
        interviewTable.setItems(FXCollections.observableArrayList(interviews));
    }

    @FXML
    private void handleExit() {
        Stage stage = (Stage) recruitmentTable.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleNewRecruitment() {
        Dialog<Recruitment> dialog = new Dialog<>();
        dialog.setTitle("New Recruitment");
        dialog.setHeaderText("Create a new recruitment");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Role> roleCombo = new ComboBox<>();
        roleCombo.setItems(FXCollections.observableArrayList(recruitmentService.getAllRoles()));
        DatePicker deadlinePicker = new DatePicker(LocalDate.now().plusDays(30));

        grid.add(new Label("Role:"), 0, 0);
        grid.add(roleCombo, 1, 0);
        grid.add(new Label("Application Deadline:"), 0, 1);
        grid.add(deadlinePicker, 1, 1);

        dialog.getDialogPane().setContent(grid);

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    // Check if deadline is in the future
                    if (deadlinePicker.getValue().isBefore(LocalDate.now())) {
                        showError("Invalid Date", "Application deadline must be in the future.");
                        return null;
                    }
                    
                    return recruitmentService.createRecruitment(
                        roleCombo.getValue(),
                        deadlinePicker.getValue()
                    );
                } catch (IllegalArgumentException e) {
                    showError("Invalid Input", e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(recruitment -> {
            loadRecruitments();
            updateStatistics();
        });
    }

    @FXML
    private void handleViewRecruitments() {
        Dialog<LocalDate[]> dialog = new Dialog<>();
        dialog.setTitle("View Recruitments");
        dialog.setHeaderText("Select date range");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        DatePicker startDate = new DatePicker(LocalDate.now().minusMonths(1));
        DatePicker endDate = new DatePicker(LocalDate.now());

        grid.add(new Label("Start Date:"), 0, 0);
        grid.add(startDate, 1, 0);
        grid.add(new Label("End Date:"), 0, 1);
        grid.add(endDate, 1, 1);

        dialog.getDialogPane().setContent(grid);

        ButtonType viewButtonType = new ButtonType("View", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(viewButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == viewButtonType) {
                return new LocalDate[]{startDate.getValue(), endDate.getValue()};
            }
            return null;
        });

        dialog.showAndWait().ifPresent(dates -> {
            List<Recruitment> filteredRecruitments = recruitmentService.getRecruitmentsByDateRange(dates[0], dates[1]);
            recruitmentTable.setItems(FXCollections.observableArrayList(filteredRecruitments));
        });
    }

    @FXML
    private void handleManageRoles() {
        Dialog<Role> dialog = new Dialog<>();
        dialog.setTitle("Manage Roles");
        dialog.setHeaderText("Add a new role");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(3);
        TextField departmentField = new TextField();

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Department:"), 0, 2);
        grid.add(departmentField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (titleField.getText().isEmpty()) {
                    showError("Invalid Input", "Role title cannot be empty.");
                    return null;
                }
                
                return recruitmentService.createRole(
                    titleField.getText(),
                    descriptionArea.getText(),
                    departmentField.getText()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(role -> {
            updateStatistics();
        });
    }
    
    @FXML
    private void handleViewApplicants() {
        Recruitment selectedRecruitment = recruitmentTable.getSelectionModel().getSelectedItem();
        if (selectedRecruitment == null) {
            showError("No Selection", "Please select a recruitment to view applicants.");
            return;
        }
        
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Applicants");
        dialog.setHeaderText("Applicants for " + selectedRecruitment.getRole().getTitle());
        
        TableView<Applicant> applicantTable = new TableView<>();
        TableColumn<Applicant, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFullName()));
        
        TableColumn<Applicant, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        TableColumn<Applicant, Integer> rankColumn = new TableColumn<>("Rank");
        rankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
        
        applicantTable.getColumns().addAll(nameColumn, emailColumn, rankColumn);
        applicantTable.setPrefWidth(400);
        applicantTable.setPrefHeight(300);
        
        List<Applicant> applicants = Applicant.findByRecruitment(
            recruitmentService.getConnection(), selectedRecruitment);
        applicantTable.setItems(FXCollections.observableArrayList(applicants));
        
        dialog.getDialogPane().setContent(applicantTable);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        dialog.showAndWait();
    }
    
    private void handleEditRecruitment() {
        Recruitment selectedRecruitment = recruitmentTable.getSelectionModel().getSelectedItem();
        if (selectedRecruitment == null) {
            showError("No Selection", "Please select a recruitment to edit.");
            return;
        }
        
        Dialog<Recruitment> dialog = new Dialog<>();
        dialog.setTitle("Edit Recruitment");
        dialog.setHeaderText("Edit recruitment details");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        ComboBox<Role> roleCombo = new ComboBox<>();
        roleCombo.setItems(FXCollections.observableArrayList(recruitmentService.getAllRoles()));
        roleCombo.setValue(selectedRecruitment.getRole());
        
        DatePicker deadlinePicker = new DatePicker(selectedRecruitment.getApplicationDeadline());
        ComboBox<RecruitmentStatus> statusCombo = new ComboBox<>();
        statusCombo.setItems(FXCollections.observableArrayList(RecruitmentStatus.values()));
        statusCombo.setValue(selectedRecruitment.getStatus());
        
        grid.add(new Label("Role:"), 0, 0);
        grid.add(roleCombo, 1, 0);
        grid.add(new Label("Application Deadline:"), 0, 1);
        grid.add(deadlinePicker, 1, 1);
        grid.add(new Label("Status:"), 0, 2);
        grid.add(statusCombo, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Check if deadline is in the future for active recruitments
                    if (statusCombo.getValue() == RecruitmentStatus.ACTIVE && 
                        deadlinePicker.getValue().isBefore(LocalDate.now())) {
                        showError("Invalid Date", "Application deadline must be in the future for active recruitments.");
                        return null;
                    }
                    
                    selectedRecruitment.setApplicationDeadline(deadlinePicker.getValue());
                    selectedRecruitment.setStatus(statusCombo.getValue());
                    selectedRecruitment.update(recruitmentService.getConnection());
                    return selectedRecruitment;
                } catch (Exception e) {
                    showError("Error", "Failed to update recruitment: " + e.getMessage());
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(recruitment -> {
            loadRecruitments();
            updateStatistics();
        });
    }
    
    private void handleDeleteRecruitment() {
        Recruitment selectedRecruitment = recruitmentTable.getSelectionModel().getSelectedItem();
        if (selectedRecruitment == null) {
            showError("No Selection", "Please select a recruitment to delete.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Recruitment");
        alert.setHeaderText("Delete Recruitment");
        alert.setContentText("Are you sure you want to delete this recruitment?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = selectedRecruitment.delete(recruitmentService.getConnection());
            if (success) {
                loadRecruitments();
                updateStatistics();
            } else {
                showError("Error", "Failed to delete recruitment.");
            }
        }
    }
    
    private void handleRescheduleInterview() {
        Interview selectedInterview = interviewTable.getSelectionModel().getSelectedItem();
        if (selectedInterview == null) {
            showError("No Selection", "Please select an interview to reschedule.");
            return;
        }
        
        Dialog<LocalDateTime> dialog = new Dialog<>();
        dialog.setTitle("Reschedule Interview");
        dialog.setHeaderText("Select new date and time");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        DatePicker datePicker = new DatePicker(selectedInterview.getDateTime().toLocalDate());
        TextField timeField = new TextField(
            selectedInterview.getDateTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        
        grid.add(new Label("Date:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("Time (HH:MM):"), 0, 1);
        grid.add(timeField, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String[] timeParts = timeField.getText().split(":");
                    int hour = Integer.parseInt(timeParts[0]);
                    int minute = Integer.parseInt(timeParts[1]);
                    
                    return datePicker.getValue().atTime(hour, minute);
                } catch (Exception e) {
                    showError("Invalid Time", "Please enter a valid time in HH:MM format.");
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(newDateTime -> {
            recruitmentService.rescheduleInterview(selectedInterview, newDateTime);
            loadInterviews();
        });
    }
    
    private void handleCancelInterview() {
        Interview selectedInterview = interviewTable.getSelectionModel().getSelectedItem();
        if (selectedInterview == null) {
            showError("No Selection", "Please select an interview to cancel.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Interview");
        alert.setHeaderText("Cancel Interview");
        alert.setContentText("Are you sure you want to cancel this interview?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            recruitmentService.cancelInterview(selectedInterview);
            loadInterviews();
        }
    }
    
    private void handleCompleteInterview() {
        Interview selectedInterview = interviewTable.getSelectionModel().getSelectedItem();
        if (selectedInterview == null) {
            showError("No Selection", "Please select an interview to mark as completed.");
            return;
        }
        
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Complete Interview");
        dialog.setHeaderText("Add notes and complete the interview");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextArea notesArea = new TextArea(selectedInterview.getNotes());
        notesArea.setPrefRowCount(5);
        notesArea.setPrefColumnCount(30);
        
        grid.add(new Label("Notes:"), 0, 0);
        grid.add(notesArea, 0, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType completeButtonType = new ButtonType("Complete", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(completeButtonType, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == completeButtonType) {
                return notesArea.getText();
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(notes -> {
            selectedInterview.setNotes(notes);
            selectedInterview.complete();
            selectedInterview.update(recruitmentService.getConnection());
            loadInterviews();
        });
    }

    @FXML
    private void handleRecruitmentStats() {
        updateStatistics();
    }

    private void updateStatistics() {
        double avgDays = recruitmentService.getAverageDaysToAcceptance();
        double avgInterviews = recruitmentService.getAverageInterviewsPerOffer();
        Role popularRole = recruitmentService.getMostPopularRole();

        avgDaysLabel.setText(String.format("%.1f days", avgDays));
        avgInterviewsLabel.setText(String.format("%.1f", avgInterviews));
        popularRoleLabel.setText(popularRole != null ? popularRole.getTitle() : "N/A");
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 