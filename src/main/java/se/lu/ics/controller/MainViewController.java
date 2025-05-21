package se.lu.ics.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.io.IOException;
import java.time.Period;
import java.util.Map;

import se.lu.ics.model.*;
import se.lu.ics.service.RecruitmentService;
import se.lu.ics.service.ReportService;

/**
 * Controller for the main view of the application.
 */
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
    private ReportService reportService;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    /**
     * Default constructor
     */
    public MainViewController() {
        // Default constructor required by FXML
    }
    
    /**
     * Constructor with dependency injection
     * @param recruitmentService The recruitment service
     */
    public MainViewController(RecruitmentService recruitmentService) {
        this.recruitmentService = recruitmentService;
    }

    /**
     * Set the recruitment service
     * @param recruitmentService The recruitment service to set
     */
    public void setRecruitmentService(RecruitmentService recruitmentService) {
        this.recruitmentService = recruitmentService;
    }
    
    /**
     * Set the report service
     * @param reportService The report service to set
     */
    public void setReportService(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Initialize method called by JavaFX after FXML fields are injected
     */
    @FXML
    public void initialize() {
        setupRecruitmentTable();
        setupInterviewTable();
        
        // Only load data if the service is available
        if (recruitmentService != null) {
            loadRecruitments();
            loadInterviews();
            updateStatistics();
        }
    }

    /**
     * Called after services are injected to initialize the UI with data
     */
    public void initializeData() {
        if (recruitmentService != null) {
            loadRecruitments();
            loadInterviews();
            updateStatistics();
        }
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
        interviewApplicantColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getApplicant().getFullName()));
        interviewRoleColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getRecruitment().getRole().getTitle()));
        interviewerColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getInterviewer()));
        interviewStatusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus().toString()));
            
        // Add context menu for interview table
        ContextMenu contextMenu = new ContextMenu();
        MenuItem completeInterviewItem = new MenuItem("Mark as Completed");
        completeInterviewItem.setOnAction(e -> handleCompleteInterview());
        MenuItem rescheduleInterviewItem = new MenuItem("Reschedule");
        rescheduleInterviewItem.setOnAction(e -> handleRescheduleInterview());
        MenuItem cancelInterviewItem = new MenuItem("Cancel Interview");
        cancelInterviewItem.setOnAction(e -> handleCancelInterview());
        contextMenu.getItems().addAll(completeInterviewItem, rescheduleInterviewItem, cancelInterviewItem);
        
        interviewTable.setContextMenu(contextMenu);
    }

    private void loadRecruitments() {
        List<Recruitment> recruitments = recruitmentService.getAllRecruitments();
        
        // Set the applicant count for each recruitment
        for (Recruitment recruitment : recruitments) {
            List<Applicant> applicants = recruitmentService.getApplicantsForRecruitment(recruitment);
            recruitment.setApplicantCount(applicants.size());
        }
        
        recruitmentTable.setItems(FXCollections.observableArrayList(recruitments));
    }

    private void loadInterviews() {
        List<Interview> interviews = recruitmentService.getAllInterviews();
        interviewTable.setItems(FXCollections.observableArrayList(interviews));
    }

    private void updateStatistics() {
        try {
            // Calculate average days to acceptance
            double avgDays = recruitmentService.getAverageDaysToAcceptance();
            avgDaysLabel.setText(String.format("Average: %.1f days", avgDays));
            
            // Calculate average interviews per position
            double avgInterviews = recruitmentService.getAverageInterviewsPerOffer();
            avgInterviewsLabel.setText(String.format("Average: %.1f interviews per position", avgInterviews));
            
            // Find most popular role
            Role popularRole = recruitmentService.getMostPopularRole();
            if (popularRole != null) {
                popularRoleLabel.setText("Most popular: " + popularRole.getTitle());
            } else {
                popularRoleLabel.setText("Most popular: No data available");
            }
        } catch (Exception e) {
            // Handle errors gracefully
            System.err.println("Error updating statistics: " + e.getMessage());
            
            // Set default values if calculation fails
            avgDaysLabel.setText("Average: N/A");
            avgInterviewsLabel.setText("Average: N/A");
            popularRoleLabel.setText("Most popular: N/A");
        }
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    @FXML
    private void handleNewRecruitment() {
        Dialog<Recruitment> dialog = new Dialog<>();
        dialog.setTitle("New Recruitment");
        dialog.setHeaderText("Create a new recruitment position");
        
        // Set the button types
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        // Create the grid for the dialog
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Create the form fields
        ComboBox<Role> roleComboBox = new ComboBox<>();
        roleComboBox.setItems(FXCollections.observableArrayList(recruitmentService.getAllRoles()));
        DatePicker deadlinePicker = new DatePicker(LocalDate.now().plusMonths(1));
        
        grid.add(new Label("Role:"), 0, 0);
        grid.add(roleComboBox, 1, 0);
        grid.add(new Label("Application Deadline:"), 0, 1);
        grid.add(deadlinePicker, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convert the result when the create button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                Role selectedRole = roleComboBox.getValue();
                LocalDate deadline = deadlinePicker.getValue();
                
                if (selectedRole != null && deadline != null) {
                    return new Recruitment(selectedRole, deadline);
                }
            }
            return null;
        });
        
        Optional<Recruitment> result = dialog.showAndWait();
        result.ifPresent(recruitment -> {
            recruitmentService.addRecruitment(recruitment);
            loadRecruitments();
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
            // Replace this with the actual filter logic
            loadRecruitments();
        });
    }

    @FXML
    private void handleManageRoles() {
        Dialog<Role> dialog = new Dialog<>();
        dialog.setTitle("Manage Roles");
        dialog.setHeaderText("Create or edit job roles");
        
        // Create a TableView to display existing roles
        TableView<Role> rolesTable = new TableView<>();
        rolesTable.setPrefHeight(200);
        
        TableColumn<Role, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        TableColumn<Role, String> departmentColumn = new TableColumn<>("Department");
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        
        rolesTable.getColumns().addAll(titleColumn, departmentColumn);
        rolesTable.setItems(FXCollections.observableArrayList(recruitmentService.getAllRoles()));
        
        // Create form fields for adding new roles
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));
        
        TextField titleField = new TextField();
        TextField departmentField = new TextField();
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(3);
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Department:"), 0, 1);
        grid.add(departmentField, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descriptionArea, 1, 2);
        
        // Create a VBox to hold both the table and the form
        javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10);
        vbox.getChildren().addAll(rolesTable, grid);
        
        dialog.getDialogPane().setContent(vbox);
        
        ButtonType addButtonType = new ButtonType("Add Role", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CLOSE);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (!titleField.getText().isEmpty()) {
                    Role role = new Role();
                    role.setTitle(titleField.getText());
                    role.setDepartment(departmentField.getText());
                    role.setDescription(descriptionArea.getText());
                    return role;
                }
            }
            return null;
        });
        
        Optional<Role> result = dialog.showAndWait();
        result.ifPresent(role -> {
            recruitmentService.addRole(role);
            // Refresh the table in the dialog
            rolesTable.setItems(FXCollections.observableArrayList(recruitmentService.getAllRoles()));
        });
    }
    
    @FXML
    private void handleViewApplicants() {
        Recruitment selectedRecruitment = recruitmentTable.getSelectionModel().getSelectedItem();
        if (selectedRecruitment == null) {
            showError("No Selection", "Please select a recruitment to view applicants.");
            return;
        }
        
        // Create a dialog that can be updated (so we keep a reference to the content)
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Applicants");
        dialog.setHeaderText("Applicants for " + selectedRecruitment.getRole().getTitle());
        
        // Create a container that will hold the content and buttons
        VBox contentVBox = new VBox(10);
        contentVBox.setPadding(new Insets(10, 10, 10, 10));
        
        // Create buttons for adding and removing applicants
        HBox buttonBox = new HBox(10);
        Button addButton = new Button("Add Applicant");
        Button deleteButton = new Button("Remove Applicant");
        Button scheduleInterviewButton = new Button("Schedule Interview");
        
        buttonBox.getChildren().addAll(addButton, deleteButton, scheduleInterviewButton);
        contentVBox.getChildren().add(buttonBox);
        
        // Create and configure the table
        TableView<Applicant> applicantTable = new TableView<>();
        applicantTable.setPrefHeight(300);
        
        TableColumn<Applicant, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFullName()));
        
        TableColumn<Applicant, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        TableColumn<Applicant, String> dateColumn = new TableColumn<>("Application Date");
        dateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getApplicationDate().format(dateFormatter)));
        
        TableColumn<Applicant, String> rankColumn = new TableColumn<>("Rank");
        rankColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(getRankDescription(cellData.getValue().getRank())));
        
        applicantTable.getColumns().addAll(nameColumn, emailColumn, dateColumn, rankColumn);
        contentVBox.getChildren().add(applicantTable);
        
        // A method to refresh the applicant table
        Runnable refreshApplicants = () -> {
            List<Applicant> applicants = null;
            try {
                applicants = recruitmentService.getApplicantsForRecruitment(selectedRecruitment);
                
                if (applicants.isEmpty()) {
                    applicantTable.setPlaceholder(new Label("No applicants for this recruitment yet."));
                }
                applicantTable.setItems(FXCollections.observableArrayList(applicants));
                
                // Update the main recruitment table to reflect the current applicant count
                loadRecruitments();
            } catch (Exception e) {
                showError("Error Loading Applicants", "Could not load applicants: " + e.getMessage());
                applicantTable.setPlaceholder(new Label("Error loading applicants."));
            }
        };
        
        // Initial loading of applicants
        refreshApplicants.run();
        
        // Add button handlers
        addButton.setOnAction(e -> {
            handleAddApplicant(selectedRecruitment, refreshApplicants);
        });
        
        deleteButton.setOnAction(e -> {
            Applicant selectedApplicant = applicantTable.getSelectionModel().getSelectedItem();
            if (selectedApplicant != null) {
                handleDeleteApplicant(selectedApplicant, selectedRecruitment, refreshApplicants);
            } else {
                showError("No Selection", "Please select an applicant to remove.");
            }
        });
        
        scheduleInterviewButton.setOnAction(e -> {
            Applicant selectedApplicant = applicantTable.getSelectionModel().getSelectedItem();
            if (selectedApplicant != null) {
                handleScheduleInterview(selectedApplicant, selectedRecruitment);
                refreshApplicants.run();
            } else {
                showError("No Selection", "Please select an applicant to schedule an interview with.");
            }
        });
        
        // Add context menu for ranking
        ContextMenu contextMenu = new ContextMenu();
        MenuItem rankItem = new MenuItem("Rank Applicant");
        rankItem.setOnAction(e -> {
            Applicant selected = applicantTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleRankApplicant(selected, selectedRecruitment);
                refreshApplicants.run();
            } else {
                showError("No Selection", "Please select an applicant to rank.");
            }
        });
        contextMenu.getItems().add(rankItem);
        applicantTable.setContextMenu(contextMenu);
        
        // Set dialog content and configuration
        dialog.getDialogPane().setContent(contentVBox);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.setResizable(true);
        dialog.getDialogPane().setPrefWidth(600);
        dialog.getDialogPane().setPrefHeight(500);
        
        dialog.showAndWait();
    }
    
    /**
     * Handle adding a new applicant to a recruitment
     * @param recruitment The recruitment to add the applicant to
     * @param refreshAction Action to run after adding the applicant
     */
    private void handleAddApplicant(Recruitment recruitment, Runnable refreshAction) {
        Dialog<Applicant> dialog = new Dialog<>();
        dialog.setTitle("Add Applicant");
        dialog.setHeaderText("Add a new applicant to " + recruitment.getRole().getTitle());
        
        // Set the button types
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Add form fields
        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        TextField emailField = new TextField();
        TextField phoneField = new TextField();
        
        grid.add(new Label("First Name:*"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:*"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Email:*"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        
        // Add note about required fields
        Label requiredNote = new Label("* Required fields");
        requiredNote.setStyle("-fx-font-size: 10pt; -fx-text-fill: gray;");
        grid.add(requiredNote, 0, 4, 2, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the first field
        Platform.runLater(() -> firstNameField.requestFocus());
        
        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                // Validate inputs
                if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() 
                        || emailField.getText().isEmpty()) {
                    showError("Missing Information", "Please fill in all required fields.");
                    return null;
                }
                
                return new Applicant(
                    firstNameField.getText(),
                    lastNameField.getText(),
                    emailField.getText(),
                    phoneField.getText()
                );
            }
            return null;
        });
        
        Optional<Applicant> result = dialog.showAndWait();
        result.ifPresent(applicant -> {
            // Add the applicant to the selected recruitment
            try {
                recruitmentService.addApplicantToRecruitment(applicant, recruitment);
                showInfo("Applicant Added", 
                         applicant.getFullName() + " has been added to " + recruitment.getRole().getTitle());
                
                // Refresh the applicant list
                if (refreshAction != null) {
                    refreshAction.run();
                }
            } catch (Exception e) {
                showError("Error", "Failed to add applicant: " + e.getMessage());
            }
        });
    }
    
    /**
     * Handle deleting an applicant
     * @param applicant The applicant to delete
     * @param recruitment The recruitment the applicant belongs to
     * @param refreshAction Action to run after deleting the applicant
     */
    private void handleDeleteApplicant(Applicant applicant, Recruitment recruitment, Runnable refreshAction) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete Applicant");
        confirmation.setContentText("Are you sure you want to remove " + applicant.getFullName() 
                                  + " from this recruitment?");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                recruitmentService.removeApplicantFromRecruitment(applicant, recruitment);
                showInfo("Applicant Removed", 
                         applicant.getFullName() + " has been removed from the recruitment.");
                
                // Refresh the applicant list
                if (refreshAction != null) {
                    refreshAction.run();
                }
            } catch (Exception e) {
                showError("Error", "Failed to remove applicant: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handle scheduling an interview with an applicant
     * @param applicant The applicant to interview
     * @param recruitment The recruitment the applicant applied to
     */
    private void handleScheduleInterview(Applicant applicant, Recruitment recruitment) {
        Dialog<Interview> dialog = new Dialog<>();
        dialog.setTitle("Schedule Interview");
        dialog.setHeaderText("Schedule an interview with " + applicant.getFullName());
        
        // Set button types
        ButtonType scheduleButtonType = new ButtonType("Schedule", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(scheduleButtonType, ButtonType.CANCEL);
        
        // Create form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Date and time picker
        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
        
        // Hour and minute spinners (8:00 - 18:00)
        Spinner<Integer> hourSpinner = new Spinner<>(8, 18, 9);
        hourSpinner.setEditable(true);
        hourSpinner.setPrefWidth(75);
        
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 55, 0, 5);
        minuteSpinner.setEditable(true);
        minuteSpinner.setPrefWidth(75);
        
        HBox timeBox = new HBox(5);
        timeBox.getChildren().addAll(hourSpinner, new Label(":"), minuteSpinner);
        
        // Location and interviewer
        TextField locationField = new TextField("VikingExpress Office");
        TextField interviewerField = new TextField();
        
        grid.add(new Label("Date:*"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("Time:*"), 0, 1);
        grid.add(timeBox, 1, 1);
        grid.add(new Label("Location:*"), 0, 2);
        grid.add(locationField, 1, 2);
        grid.add(new Label("Interviewer:*"), 0, 3);
        grid.add(interviewerField, 1, 3);
        
        // Add note about required fields
        Label requiredNote = new Label("* Required fields");
        requiredNote.setStyle("-fx-font-size: 10pt; -fx-text-fill: gray;");
        grid.add(requiredNote, 0, 4, 2, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the interviewer field
        Platform.runLater(() -> interviewerField.requestFocus());
        
        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == scheduleButtonType) {
                // Validate inputs
                if (datePicker.getValue() == null || locationField.getText().isEmpty() 
                        || interviewerField.getText().isEmpty()) {
                    showError("Missing Information", "Please fill in all required fields.");
                    return null;
                }
                
                // Create date time from components
                LocalDateTime dateTime = LocalDateTime.of(
                    datePicker.getValue(),
                    java.time.LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue())
                );
                
                // Create and return the interview
                return new Interview(
                    recruitment,
                    applicant,
                    dateTime,
                    locationField.getText(),
                    interviewerField.getText()
                );
            }
            return null;
        });
        
        Optional<Interview> result = dialog.showAndWait();
        result.ifPresent(interview -> {
            try {
                recruitmentService.scheduleInterview(
                    recruitment,
                    applicant,
                    interview.getDateTime(),
                    interview.getLocation(),
                    interview.getInterviewer()
                );
                
                showInfo("Interview Scheduled", 
                         "Interview with " + applicant.getFullName() + " has been scheduled for " 
                         + interview.getDateTime().format(dateTimeFormatter));
                
                // Refresh the interviews table
                loadInterviews();
            } catch (Exception e) {
                showError("Error", "Failed to schedule interview: " + e.getMessage());
            }
        });
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
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the grid for the dialog
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Create the form fields with pre-populated values
        ComboBox<Role> roleComboBox = new ComboBox<>();
        roleComboBox.setItems(FXCollections.observableArrayList(recruitmentService.getAllRoles()));
        roleComboBox.setValue(selectedRecruitment.getRole());
        
        DatePicker deadlinePicker = new DatePicker(selectedRecruitment.getApplicationDeadline());
        
        ComboBox<RecruitmentStatus> statusComboBox = new ComboBox<>();
        statusComboBox.setItems(FXCollections.observableArrayList(RecruitmentStatus.values()));
        statusComboBox.setValue(selectedRecruitment.getStatus());
        
        grid.add(new Label("Role:"), 0, 0);
        grid.add(roleComboBox, 1, 0);
        grid.add(new Label("Application Deadline:"), 0, 1);
        grid.add(deadlinePicker, 1, 1);
        grid.add(new Label("Status:"), 0, 2);
        grid.add(statusComboBox, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convert the result when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                selectedRecruitment.setRole(roleComboBox.getValue());
                selectedRecruitment.setApplicationDeadline(deadlinePicker.getValue());
                selectedRecruitment.setStatus(statusComboBox.getValue());
                return selectedRecruitment;
            }
            return null;
        });
        
        Optional<Recruitment> result = dialog.showAndWait();
        result.ifPresent(recruitment -> {
            // Update the recruitment in the service
            recruitmentService.updateRecruitment(recruitment);
            loadRecruitments();
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
            // Delete the recruitment using the service
            boolean deleted = recruitmentService.deleteRecruitment(selectedRecruitment.getId());
            if (deleted) {
                loadRecruitments();
                updateStatistics();
            } else {
                showError("Error", "Failed to delete recruitment.");
            }
        }
    }
    
    private void handleCompleteInterview() {
        Interview selectedInterview = interviewTable.getSelectionModel().getSelectedItem();
        if (selectedInterview == null) {
            showError("No Selection", "Please select an interview to mark as completed.");
            return;
        }
        
        if (selectedInterview.getStatus() == InterviewStatus.COMPLETED) {
            showError("Already Completed", "This interview is already marked as completed.");
            return;
        }
        
        // Mark interview as completed
        selectedInterview.complete();
        recruitmentService.updateInterview(selectedInterview);
        
        // Ask if the user wants to rank the applicant now
        Alert rankNowAlert = new Alert(Alert.AlertType.CONFIRMATION);
        rankNowAlert.setTitle("Rank Applicant");
        rankNowAlert.setHeaderText("Would you like to rank this applicant now?");
        rankNowAlert.setContentText("You can always rank applicants later from the Recruitments view.");
        
        Optional<ButtonType> result = rankNowAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            handleRankApplicant(selectedInterview.getApplicant(), selectedInterview.getRecruitment());
        }
        
        loadInterviews();
    }
    
    /**
     * Handle ranking an applicant
     * @param applicant The applicant to rank
     * @param recruitment The recruitment the applicant is for
     */
    private void handleRankApplicant(Applicant applicant, Recruitment recruitment) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Rank Applicant");
        dialog.setHeaderText("Rank " + applicant.getFullName() + " for " + recruitment.getRole().getTitle());
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the grid for the dialog
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Create a slider for ranking (0-5)
        Slider rankSlider = new Slider(0, 5, applicant.getRank());
        rankSlider.setShowTickLabels(true);
        rankSlider.setShowTickMarks(true);
        rankSlider.setMajorTickUnit(1);
        rankSlider.setMinorTickCount(0);
        rankSlider.setBlockIncrement(1);
        rankSlider.setSnapToTicks(true);
        
        Label rankValueLabel = new Label(getRankDescription((int)rankSlider.getValue()));
        rankSlider.valueProperty().addListener((obs, oldval, newval) -> {
            rankValueLabel.setText(getRankDescription(newval.intValue()));
        });
        
        grid.add(new Label("Applicant:"), 0, 0);
        grid.add(new Label(applicant.getFullName()), 1, 0);
        grid.add(new Label("Rank:"), 0, 1);
        grid.add(rankSlider, 1, 1);
        grid.add(rankValueLabel, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convert the result when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return (int)rankSlider.getValue();
            }
            return null;
        });
        
        Optional<Integer> result = dialog.showAndWait();
        result.ifPresent(rank -> {
            recruitmentService.updateApplicantRank(applicant, rank);
            showInfo("Applicant Ranked", "Applicant " + applicant.getFullName() 
                    + " has been assigned a rank of " + rank + " (" + getRankDescription(rank) + ")");
        });
    }
    
    /**
     * Get the description for a rank value
     * @param rank The rank value (0-5)
     * @return The string description
     */
    private String getRankDescription(int rank) {
        switch (rank) {
            case 0: return "Not ranked";
            case 1: return "Poor fit";
            case 2: return "Below average";
            case 3: return "Average";
            case 4: return "Good fit";
            case 5: return "Excellent fit";
            default: return "Unknown";
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
        dialog.setHeaderText("Select a new date and time");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        DatePicker datePicker = new DatePicker(selectedInterview.getDateTime().toLocalDate());
        
        Spinner<Integer> hourSpinner = new Spinner<>(8, 18, selectedInterview.getDateTime().getHour());
        hourSpinner.setEditable(true);
        
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 55, selectedInterview.getDateTime().getMinute(), 5);
        minuteSpinner.setEditable(true);
        
        grid.add(new Label("Date:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("Hour:"), 0, 1);
        grid.add(hourSpinner, 1, 1);
        grid.add(new Label("Minute:"), 0, 2);
        grid.add(minuteSpinner, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return LocalDateTime.of(
                    datePicker.getValue(),
                    java.time.LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue())
                );
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(newDateTime -> {
            selectedInterview.setDateTime(newDateTime);
            selectedInterview.update(null);
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
            try {
                recruitmentService.cancelInterview(selectedInterview);
                loadInterviews();
            } catch (IllegalStateException e) {
                showError("Cannot Cancel Interview", e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleRecruitmentStats() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Recruitment Statistics");
        dialog.setHeaderText("VikingExpress Recruitment Statistics");
        
        TabPane tabPane = new TabPane();
        
        // Tab for global statistics
        Tab globalStatsTab = new Tab("Global Statistics");
        GridPane globalGrid = new GridPane();
        globalGrid.setHgap(10);
        globalGrid.setVgap(10);
        globalGrid.setPadding(new Insets(20, 20, 20, 20));
        
        // Fetch and display statistics using ReportService for more detailed analytics
        try {
            // Get summary report
            Map<String, Object> summaryReport = reportService.generateSummaryReport();
            
            // Retrieve statistics
            double avgDays = (double) summaryReport.getOrDefault("avgDaysToFill", 0.0);
            double avgInterviews = recruitmentService.getAverageInterviewsPerOffer();
            Role popularRole = recruitmentService.getMostPopularRole();
            
            // Total recruitments
            globalGrid.add(new Label("Total Recruitments:"), 0, 0);
            globalGrid.add(new Label(summaryReport.get("totalRecruitments").toString()), 1, 0);
            
            // Total applicants
            globalGrid.add(new Label("Total Applicants:"), 0, 1);
            globalGrid.add(new Label(summaryReport.get("totalApplicants").toString()), 1, 1);
            
            // Average days to acceptance
            globalGrid.add(new Label("Average Days to Acceptance:"), 0, 2);
            globalGrid.add(new Label(String.format("%.1f days", avgDays)), 1, 2);
            
            // Average interviews per offer
            globalGrid.add(new Label("Average Interviews per Offer:"), 0, 3);
            globalGrid.add(new Label(String.format("%.1f interviews", avgInterviews)), 1, 3);
            
            // Most popular role
            globalGrid.add(new Label("Most Popular Role:"), 0, 4);
            if (popularRole != null) {
                globalGrid.add(new Label(popularRole.getTitle() + " (" + popularRole.getDepartment() + ")"), 1, 4);
            } else {
                globalGrid.add(new Label("No data available"), 1, 4);
            }
            
            // Most efficient recruitment
            Recruitment efficientRecruitment = reportService.getMostEfficientRecruitment();
            globalGrid.add(new Label("Most Efficient Recruitment:"), 0, 5);
            if (efficientRecruitment != null) {
                globalGrid.add(new Label(efficientRecruitment.getRole().getTitle() + " (" + 
                              Period.between(efficientRecruitment.getPostingDate(), 
                                            efficientRecruitment.getOfferAcceptanceDate()).getDays() + " days)"), 1, 5);
            } else {
                globalGrid.add(new Label("No data available"), 1, 5);
            }
            
            // Most popular recruitment
            Recruitment popularRecruitment = reportService.getMostPopularRecruitment();
            globalGrid.add(new Label("Most Popular Recruitment:"), 0, 6);
            if (popularRecruitment != null) {
                // Get applicant count through recruitmentService
                List<Applicant> applicants = recruitmentService.getApplicantsForRecruitment(popularRecruitment);
                globalGrid.add(new Label(popularRecruitment.getRole().getTitle() + " (" + 
                              applicants.size() + " applicants)"), 1, 6);
            } else {
                globalGrid.add(new Label("No data available"), 1, 6);
            }
        } catch (Exception e) {
            System.err.println("Error generating statistics: " + e.getMessage());
            globalGrid.add(new Label("Error loading statistics: " + e.getMessage()), 0, 0, 2, 1);
        }
        
        globalStatsTab.setContent(globalGrid);
        
        // Tab for rankings
        Tab rankingsTab = new Tab("Applicant Rankings");
        VBox rankingLayout = new VBox(10);
        rankingLayout.setPadding(new Insets(20, 20, 20, 20));
        
        // Recruitment selector
        HBox recruitmentBox = new HBox(10);
        Label recruitmentLabel = new Label("Select Recruitment:");
        ComboBox<Recruitment> recruitmentComboBox = new ComboBox<>();
        recruitmentComboBox.setItems(FXCollections.observableArrayList(recruitmentService.getAllRecruitments()));
        
        recruitmentBox.getChildren().addAll(recruitmentLabel, recruitmentComboBox);
        
        // Table for ranked applicants
        TableView<Applicant> rankingTable = new TableView<>();
        TableColumn<Applicant, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFullName()));
        
        TableColumn<Applicant, String> rankColumn = new TableColumn<>("Rank");
        rankColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(getRankDescription(cellData.getValue().getRank())));
        
        rankingTable.getColumns().addAll(nameColumn, rankColumn);
        rankingTable.setPrefHeight(300);
        
        // Statistics for the selected recruitment
        GridPane rankingStatsGrid = new GridPane();
        rankingStatsGrid.setHgap(10);
        rankingStatsGrid.setVgap(10);
        
        Label totalApplicantsLabel = new Label("Total Applicants: 0");
        Label rankedApplicantsLabel = new Label("Ranked Applicants: 0");
        Label avgRankLabel = new Label("Average Rank: 0.0");
        rankingStatsGrid.add(totalApplicantsLabel, 0, 0);
        rankingStatsGrid.add(rankedApplicantsLabel, 1, 0);
        rankingStatsGrid.add(avgRankLabel, 2, 0);
        
        // Update the UI when a recruitment is selected
        recruitmentComboBox.setOnAction(e -> {
            Recruitment selected = recruitmentComboBox.getValue();
            if (selected != null) {
                // Get all applicants
                List<Applicant> allApplicants = recruitmentService.getApplicantsForRecruitment(selected);
                
                // Get ranked applicants
                List<Applicant> rankedApplicants = recruitmentService.getRankedApplicantsForRecruitment(selected);
                
                // Update the table
                rankingTable.setItems(FXCollections.observableArrayList(rankedApplicants));
                
                // Update statistics
                totalApplicantsLabel.setText("Total Applicants: " + allApplicants.size());
                rankedApplicantsLabel.setText("Ranked Applicants: " + rankedApplicants.size());
                
                // Calculate average rank
                if (!rankedApplicants.isEmpty()) {
                    double avgRank = rankedApplicants.stream()
                        .mapToInt(Applicant::getRank)
                        .average()
                        .orElse(0.0);
                    avgRankLabel.setText(String.format("Average Rank: %.1f", avgRank));
                } else {
                    avgRankLabel.setText("Average Rank: N/A");
                }
            }
        });
        
        rankingLayout.getChildren().addAll(recruitmentBox, rankingTable, rankingStatsGrid);
        rankingsTab.setContent(rankingLayout);
        
        tabPane.getTabs().addAll(globalStatsTab, rankingsTab);
        
        dialog.getDialogPane().setContent(tabPane);
        dialog.getDialogPane().setPrefWidth(600);
        dialog.getDialogPane().setPrefHeight(400);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        dialog.showAndWait();
    }
    
    @FXML
    private void handleViewReports() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReportView.fxml"));
            
            // Create a ReportController and set it in the loader
            ReportController reportController = new ReportController(recruitmentService, reportService);
            loader.setController(reportController);
            
            Parent root = loader.load();
            
            Stage reportStage = new Stage();
            reportStage.setTitle("Recruitment Reports");
            reportStage.setScene(new Scene(root, 800, 600));
            reportStage.show();
            
        } catch (IOException e) {
            showError("Error", "Could not open report view: " + e.getMessage());
        }
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 