package se.lu.ics;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.sql.DataSource;

import se.lu.ics.dao.*;
import se.lu.ics.model.*;          // om MainViewController ligger här
import se.lu.ics.service.DatabaseService;
import se.lu.ics.service.RecruitmentService;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        /* ───── 1. DataSource från vår singleton ───── */
        DataSource ds = DatabaseService.getDataSource();

        /* ───── 2. DAO-objekt ───── */
        RoleDao        roleDao = new RoleDaoJdbc(ds);
        RecruitmentDao recDao  = new RecruitmentDaoJdbc(ds);
        ApplicantDao   appDao  = new ApplicantDaoJdbc(ds);      // <─ bytte namn
        InterviewDao   intDao  = new InterviewDaoJdbc(ds);

        /* ───── 3. Service-lager ───── */
        RecruitmentService service =
            new RecruitmentService(roleDao, recDao, appDao, intDao);

        /* ───── 4. Ladda FXML & injicera service ───── */
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        loader.setControllerFactory(t -> new MainViewController(service));
        Parent root = loader.load();

        stage.setTitle("VikingExpress Recruitment System");
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(e -> Platform.exit());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
