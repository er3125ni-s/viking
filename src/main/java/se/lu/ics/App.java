package se.lu.ics;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import se.lu.ics.service.RecruitmentService;

public class App extends Application {
    private static RecruitmentService recruitmentService;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize the recruitment service
        recruitmentService = new RecruitmentService();
        
        // Load the main view
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent root = loader.load();
        
        // Set up the primary stage
        primaryStage.setTitle("VikingExpress Recruitment System");
        primaryStage.setScene(new Scene(root));
        primaryStage.setOnCloseRequest(e -> {
            // Close database connection on application exit
            if (recruitmentService != null) {
                recruitmentService.close();
            }
            Platform.exit();
        });
        
        primaryStage.show();
    }
    
    public static RecruitmentService getRecruitmentService() {
        return recruitmentService;
    }

    public static void main(String[] args) {
        launch(args);
    }
} 