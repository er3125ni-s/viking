package se.lu.ics.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import se.lu.ics.App;
import se.lu.ics.service.RecruitmentService;

/**
 * Controller for recruitment management.
 */
public class RecruitmentController {
    private RecruitmentService recruitmentService;
    
    /**
     * Constructor
     */
    public RecruitmentController() {
        recruitmentService = App.getRecruitmentService();
    }
    
    /**
     * Display an information dialog
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 