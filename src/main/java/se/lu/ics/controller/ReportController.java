package se.lu.ics.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.print.PrinterJob;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import se.lu.ics.model.*;
import se.lu.ics.service.RecruitmentService;
import se.lu.ics.service.ReportService;

/**
 * Controller for generating and displaying recruitment reports.
 */
public class ReportController {
    @FXML private TabPane reportTabPane;
    @FXML private Tab summaryTab;
    @FXML private Tab recruitmentTab;
    @FXML private Tab departmentTab;
    
    // Summary tab elements
    @FXML private Label totalRecruitmentsLabel;
    @FXML private Label openRecruitmentsLabel;
    @FXML private Label filledRecruitmentsLabel;
    @FXML private Label avgTimeToFillLabel;
    @FXML private Label totalApplicantsLabel;
    @FXML private Label avgApplicantsLabel;
    @FXML private PieChart statusPieChart;
    
    // Recruitment tab elements
    @FXML private ComboBox<Recruitment> recruitmentSelector;
    @FXML private GridPane recruitmentDetailsGrid;
    @FXML private BarChart<String, Number> applicantRankChart;
    @FXML private PieChart interviewStatusChart;
    
    // Department tab elements
    @FXML private TableView<DepartmentStatistics> departmentTable;
    @FXML private TableColumn<DepartmentStatistics, String> departmentNameColumn;
    @FXML private TableColumn<DepartmentStatistics, Integer> totalRecruitmentsColumn;
    @FXML private TableColumn<DepartmentStatistics, Integer> openRecruitmentsColumn;
    @FXML private TableColumn<DepartmentStatistics, Double> avgTimeToFillColumn;
    
    private final RecruitmentService recruitmentService;
    private final ReportService reportService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Constructor with dependency injection
     * @param recruitmentService The recruitment service
     * @param reportService The report service
     */
    public ReportController(RecruitmentService recruitmentService, ReportService reportService) {
        this.recruitmentService = recruitmentService;
        this.reportService = reportService;
    }
    
    /**
     * Initialize method called by JavaFX after FXML fields are injected
     */
    @FXML
    public void initialize() {
        setupSummaryTab();
        setupRecruitmentTab();
        setupDepartmentTab();
        
        // Load initial data
        refreshSummaryReport();
    }
    
    /**
     * Set up the summary tab UI elements
     */
    private void setupSummaryTab() {
        // Set up action buttons
        Button printButton = new Button("Print Report");
        printButton.setOnAction(e -> printSummaryReport());
        
        Button exportButton = new Button("Export to CSV");
        exportButton.setOnAction(e -> exportSummaryToCsv());
        
        HBox buttonBox = new HBox(10, printButton, exportButton);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        // Find the VBox or other container in the tab and add the buttons
        if (summaryTab.getContent() instanceof VBox) {
            ((VBox) summaryTab.getContent()).getChildren().add(buttonBox);
        }
    }
    
    /**
     * Set up the recruitment tab UI elements
     */
    private void setupRecruitmentTab() {
        // Populate recruitment selector
        recruitmentSelector.setItems(FXCollections.observableArrayList(recruitmentService.getAllRecruitments()));
        recruitmentSelector.setPromptText("Select a recruitment");
        
        // Add event handler for recruitment selection
        recruitmentSelector.setOnAction(e -> {
            Recruitment selectedRecruitment = recruitmentSelector.getValue();
            if (selectedRecruitment != null) {
                loadRecruitmentReport(selectedRecruitment);
            }
        });
        
        // Initialize chart labels
        if (applicantRankChart != null) {
            applicantRankChart.setTitle("Applicant Rank Distribution");
            CategoryAxis xAxis = (CategoryAxis) applicantRankChart.getXAxis();
            NumberAxis yAxis = (NumberAxis) applicantRankChart.getYAxis();
            xAxis.setLabel("Rank");
            yAxis.setLabel("Number of Applicants");
        }
        
        if (interviewStatusChart != null) {
            interviewStatusChart.setTitle("Interview Status");
        }
    }
    
    /**
     * Set up the department tab UI elements
     */
    private void setupDepartmentTab() {
        // Configure department table columns
        departmentNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDepartmentName()));
        
        totalRecruitmentsColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(
                cellData.getValue().getTotalRecruitments()).asObject());
        
        openRecruitmentsColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(
                (int)cellData.getValue().getOpenRecruitments()).asObject());
        
        avgTimeToFillColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(
                cellData.getValue().getAvgDaysToFill()).asObject());
    }
    
    /**
     * Load the summary report data and update the UI
     */
    private void refreshSummaryReport() {
        try {
            Map<String, Object> summaryReport = reportService.generateSummaryReport();
            
            // Update labels
            totalRecruitmentsLabel.setText(summaryReport.get("totalRecruitments").toString());
            totalApplicantsLabel.setText(summaryReport.get("totalApplicants").toString());
            
            // Format average values
            double avgDaysToFill = (double) summaryReport.get("avgDaysToFill");
            avgTimeToFillLabel.setText(String.format("%.1f days", avgDaysToFill));
            
            double avgApplicants = (double) summaryReport.get("avgApplicantsPerRecruitment");
            avgApplicantsLabel.setText(String.format("%.1f", avgApplicants));
            
            // Get recruitment status distribution for pie chart
            @SuppressWarnings("unchecked")
            Map<RecruitmentStatus, Long> statusMap = 
                (Map<RecruitmentStatus, Long>) summaryReport.get("recruitmentsByStatus");
            
            // Count open recruitments
            long openCount = statusMap.getOrDefault(RecruitmentStatus.OPEN, 0L);
            openRecruitmentsLabel.setText(String.valueOf(openCount));
            
            // Count filled recruitments
            long filledCount = statusMap.getOrDefault(RecruitmentStatus.FILLED, 0L);
            filledRecruitmentsLabel.setText(String.valueOf(filledCount));
            
            // Update pie chart
            statusPieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Open", openCount),
                new PieChart.Data("In Progress", statusMap.getOrDefault(RecruitmentStatus.IN_PROGRESS, 0L)),
                new PieChart.Data("Filled", filledCount),
                new PieChart.Data("Cancelled", statusMap.getOrDefault(RecruitmentStatus.CANCELLED, 0L))
            ));
            
        } catch (Exception e) {
            showError("Error", "Failed to generate summary report: " + e.getMessage());
        }
    }
    
    /**
     * Load the recruitment report data for a specific recruitment
     * @param recruitment The recruitment to load the report for
     */
    private void loadRecruitmentReport(Recruitment recruitment) {
        try {
            Map<String, Object> report = reportService.generateRecruitmentReport(recruitment);
            
            // Clear existing details
            recruitmentDetailsGrid.getChildren().clear();
            
            // Add details to the grid
            int row = 0;
            
            recruitmentDetailsGrid.add(new Label("Recruitment ID:"), 0, row);
            recruitmentDetailsGrid.add(new Label(report.get("recruitmentId").toString()), 1, row++);
            
            recruitmentDetailsGrid.add(new Label("Role:"), 0, row);
            recruitmentDetailsGrid.add(new Label(report.get("role").toString()), 1, row++);
            
            recruitmentDetailsGrid.add(new Label("Department:"), 0, row);
            recruitmentDetailsGrid.add(new Label(report.get("department").toString()), 1, row++);
            
            recruitmentDetailsGrid.add(new Label("Status:"), 0, row);
            recruitmentDetailsGrid.add(new Label(report.get("status").toString()), 1, row++);
            
            recruitmentDetailsGrid.add(new Label("Posting Date:"), 0, row);
            recruitmentDetailsGrid.add(new Label(((LocalDate)report.get("postingDate")).format(dateFormatter)), 1, row++);
            
            recruitmentDetailsGrid.add(new Label("Application Deadline:"), 0, row);
            recruitmentDetailsGrid.add(new Label(((LocalDate)report.get("applicationDeadline")).format(dateFormatter)), 1, row++);
            
            recruitmentDetailsGrid.add(new Label("Total Applicants:"), 0, row);
            recruitmentDetailsGrid.add(new Label(report.get("totalApplicants").toString()), 1, row++);
            
            recruitmentDetailsGrid.add(new Label("Total Interviews:"), 0, row);
            recruitmentDetailsGrid.add(new Label(report.get("totalInterviews").toString()), 1, row++);
            
            recruitmentDetailsGrid.add(new Label("Days Active:"), 0, row);
            recruitmentDetailsGrid.add(new Label(report.get("daysActive").toString()), 1, row++);
            
            // Update applicant rank chart
            @SuppressWarnings("unchecked")
            Map<Integer, Long> rankDistribution = (Map<Integer, Long>) report.get("rankDistribution");
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Number of Applicants");
            
            // Add data points for ranks 0-5
            for (int i = 0; i <= 5; i++) {
                String rankLabel = i == 0 ? "Not Ranked" : "Rank " + i;
                long count = rankDistribution.getOrDefault(i, 0L);
                series.getData().add(new XYChart.Data<>(rankLabel, count));
            }
            
            applicantRankChart.getData().clear();
            applicantRankChart.getData().add(series);
            
            // Update interview status chart
            @SuppressWarnings("unchecked")
            Map<InterviewStatus, Long> interviewsByStatus = 
                (Map<InterviewStatus, Long>) report.get("interviewsByStatus");
            
            interviewStatusChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Scheduled", interviewsByStatus.getOrDefault(InterviewStatus.SCHEDULED, 0L)),
                new PieChart.Data("Completed", interviewsByStatus.getOrDefault(InterviewStatus.COMPLETED, 0L)),
                new PieChart.Data("Cancelled", interviewsByStatus.getOrDefault(InterviewStatus.CANCELLED, 0L)),
                new PieChart.Data("Rescheduled", interviewsByStatus.getOrDefault(InterviewStatus.RESCHEDULED, 0L))
            ));
            
        } catch (Exception e) {
            showError("Error", "Failed to generate recruitment report: " + e.getMessage());
        }
    }
    
    /**
     * Refresh the department report data and update the UI
     */
    @FXML
    private void refreshDepartmentReport() {
        try {
            Map<String, Map<String, Object>> departmentReport = reportService.generateDepartmentReport();
            
            // Convert the report data into a list of DepartmentStatistics for the table
            List<DepartmentStatistics> departmentStats = new java.util.ArrayList<>();
            
            for (Map.Entry<String, Map<String, Object>> entry : departmentReport.entrySet()) {
                String departmentName = entry.getKey();
                Map<String, Object> stats = entry.getValue();
                
                DepartmentStatistics deptStats = new DepartmentStatistics(
                    departmentName,
                    (int) stats.get("recruitmentCount"),
                    (long) stats.get("openRecruitments"),
                    (double) stats.get("avgDaysToFill")
                );
                
                departmentStats.add(deptStats);
            }
            
            // Update the table
            departmentTable.setItems(FXCollections.observableArrayList(departmentStats));
            
        } catch (Exception e) {
            showError("Error", "Failed to generate department report: " + e.getMessage());
        }
    }
    
    /**
     * Print the summary report
     */
    private void printSummaryReport() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(null)) {
            boolean success = job.printPage(summaryTab.getContent());
            if (success) {
                job.endJob();
                showInfo("Print Success", "Report printed successfully");
            } else {
                showError("Print Error", "Failed to print report");
            }
        }
    }
    
    /**
     * Export the summary report to a CSV file
     */
    private void exportSummaryToCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("recruitment_summary_report.csv");
        
        Stage stage = (Stage) reportTabPane.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Write header
                writer.println("Metric,Value");
                
                // Write data
                writer.println("Total Recruitments," + totalRecruitmentsLabel.getText());
                writer.println("Open Recruitments," + openRecruitmentsLabel.getText());
                writer.println("Filled Recruitments," + filledRecruitmentsLabel.getText());
                writer.println("Average Time to Fill," + avgTimeToFillLabel.getText());
                writer.println("Total Applicants," + totalApplicantsLabel.getText());
                writer.println("Average Applicants per Recruitment," + avgApplicantsLabel.getText());
                
                showInfo("Export Success", "Report exported successfully to " + file.getAbsolutePath());
            } catch (IOException e) {
                showError("Export Error", "Failed to export report: " + e.getMessage());
            }
        }
    }
    
    /**
     * Static inner class to hold department statistics for the table view
     */
    private static class DepartmentStatistics {
        private final String departmentName;
        private final int totalRecruitments;
        private final long openRecruitments;
        private final double avgDaysToFill;
        
        public DepartmentStatistics(String departmentName, int totalRecruitments, 
                                  long openRecruitments, double avgDaysToFill) {
            this.departmentName = departmentName;
            this.totalRecruitments = totalRecruitments;
            this.openRecruitments = openRecruitments;
            this.avgDaysToFill = avgDaysToFill;
        }
        
        public String getDepartmentName() {
            return departmentName;
        }
        
        public int getTotalRecruitments() {
            return totalRecruitments;
        }
        
        public long getOpenRecruitments() {
            return openRecruitments;
        }
        
        public double getAvgDaysToFill() {
            return avgDaysToFill;
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