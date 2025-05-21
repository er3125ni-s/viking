package se.lu.ics;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.sql.DataSource;
import java.sql.Connection;

import se.lu.ics.dao.*;
import se.lu.ics.controller.MainViewController;
import se.lu.ics.model.Recruitment;
import se.lu.ics.service.DatabaseService;
import se.lu.ics.service.RecruitmentService;
import se.lu.ics.service.ReportService;

public class App extends Application {

    private static RecruitmentService recruitmentService;
    private static ReportService reportService;

    @Override
    public void start(Stage stage) throws Exception {

        /* ───── 1. DataSource från vår singleton ───── */
        DataSource ds = DatabaseService.getDataSource();

        /* ───── 2. Create TransactionManager ───── */
        TransactionManager transactionManager = new TransactionManager(ds);

        /* ───── 3. DAO-objekt ───── */
        RoleDao         roleDao = new RoleDaoJdbc(transactionManager);
        RecruitmentDao  recDao  = new RecruitmentDaoJdbc(transactionManager, roleDao);
        ApplicantDao    appDao  = new ApplicantDaoJdbc(transactionManager);
        InterviewDao    intDao  = new InterviewDaoJdbc(transactionManager, appDao, recDao);

        /* ───── 4. Initialize recruitment ID counters ───── */
        try (Connection conn = ds.getConnection()) {
            Recruitment.initializeYearCounters(conn);
        } catch (Exception e) {
            System.err.println("Failed to initialize recruitment counters: " + e.getMessage());
            // Set a default counter for the current year
            Recruitment.setDefaultCounters();
        }

        /* ───── 5. Service-lager ───── */
        recruitmentService = new RecruitmentService(roleDao, recDao, appDao, intDao, transactionManager);
        reportService = new ReportService(recDao, appDao, intDao, transactionManager);

        /* ───── 6. Ladda FXML ───── */
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent root = loader.load();
        
        /* ───── 7. Hämta controllern och injicera service ───── */
        MainViewController controller = loader.getController();
        controller.setReportService(reportService);
        controller.setRecruitmentService(recruitmentService);
        
        // Initialize data after services are injected
        controller.initializeData();

        stage.setTitle("VikingExpress Recruitment System");
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(e -> Platform.exit());
        stage.show();
    }

    /**
     * Get the recruitment service instance
     * @return The recruitment service
     */
    public static RecruitmentService getRecruitmentService() {
        return recruitmentService;
    }
    
    /**
     * Get the report service instance
     * @return The report service
     */
    public static ReportService getReportService() {
        return reportService;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
