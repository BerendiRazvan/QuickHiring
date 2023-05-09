
import domain.ApplicationForJob;
import domain.Job;
import domain.User;
import enums.ApplicationStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JobsAppliedController extends UnicastRemoteObject implements Initializable, IObserver, Serializable {

    private IService server;
    private Parent parentRoot;
    private Stage stage;

    private User loggedUser;
    private ApplicationForJob lastApplicationSelected;

    private HomeController homeController;

    ObservableList<ApplicationForJob> applicationAppliedModelList = FXCollections.observableArrayList();
    ObservableList<ApplicationForJob> applicationInReviewModelList = FXCollections.observableArrayList();
    ObservableList<ApplicationForJob> applicationInterviewModelList = FXCollections.observableArrayList();
    ObservableList<ApplicationForJob> applicationOfferModelList = FXCollections.observableArrayList();

    // FXML Components

    @FXML
    Button backViewButton;

    @FXML
    Button withdrawApplicationButton;

    @FXML
    ListView<ApplicationForJob> appliedStatusList;

    @FXML
    ListView<ApplicationForJob> inReviewStatusList;

    @FXML
    ListView<ApplicationForJob> interviewStatusList;

    @FXML
    ListView<ApplicationForJob> offerStatusList;

    @FXML
    Label jobDetailsLabel;

    @FXML
    Label applicationDetailsLabel;

    public JobsAppliedController() throws RemoteException {

    }


    @FXML
    public void initialize(URL location, ResourceBundle resources) {

    }


    public void setServerAndStage(IService s, User user, Stage primaryStage, Parent root) {
        this.server = s;
        this.stage = primaryStage;
        this.parentRoot = root;
        this.loggedUser = user;

        initData();
    }

    private void initData() {

        uploadData();

        offerStatusList.setCellFactory(r -> new ListCell<ApplicationForJob>() {
            @Override
            public void updateItem(ApplicationForJob item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setStyle("");
                } else if (item.getApplicationStatus() == ApplicationStatus.REJECTED) {
                    setText(item.toString());
                    setGraphic(null);
                    setStyle("-fx-background-color: #ffcfc2;");
                } else {
                    setText(item.toString());
                    setGraphic(null);
                    setStyle("-fx-background-color: #d7ffc8;");
                }
            }
        });

        appliedStatusList.getSelectionModel().selectedItemProperty().addListener(o -> {
            lastApplicationSelected = appliedStatusList.getSelectionModel().getSelectedItem();
            displayApplicationInfo(lastApplicationSelected);
        });

        inReviewStatusList.getSelectionModel().selectedItemProperty().addListener(o -> {
            lastApplicationSelected = inReviewStatusList.getSelectionModel().getSelectedItem();
            displayApplicationInfo(lastApplicationSelected);
        });

        interviewStatusList.getSelectionModel().selectedItemProperty().addListener(o -> {
            lastApplicationSelected = interviewStatusList.getSelectionModel().getSelectedItem();
            displayApplicationInfo(lastApplicationSelected);
        });

        offerStatusList.getSelectionModel().selectedItemProperty().addListener(o -> {
            lastApplicationSelected = offerStatusList.getSelectionModel().getSelectedItem();
            displayApplicationInfo(lastApplicationSelected);
        });

    }

    private void displayApplicationInfo(ApplicationForJob application) {
        if (application != null) {
            Job job = application.getJobApplied();
            jobDetailsLabel.setText(
                    job.getTitle() + "   -   Applicants: " + server.getApplicantsNumber(job.getId()) + "\n\n" +
                            job.getCompany().getName() + "\n" +
                            job.getLocation().getCountry() + ", " + job.getLocation().getCity() + "\n" +
                            job.getEmploymentType() + " | " + job.getExperienceLevel() + " | " + job.getJobType() + "\n\n" +
                            job.getDescription() + "\n\n" +
                            job.getPostingDate().getDayOfMonth() + "-" + job.getPostingDate().getMonth().toString() + "-" + job.getPostingDate().getYear());

            applicationDetailsLabel.setText(
                    "Applied for this job on " +
                            application.getApplicationDate().getDayOfMonth() + "-" +
                            application.getApplicationDate().getMonth().toString() + "-" +
                            application.getApplicationDate().getYear() + "\n\n" +
                            "Application details provided by recruiter:\n" + application.getApplicationDetails());
        }
    }


    @FXML
    protected void onActionBackView(ActionEvent event) {
        try {
            setHomeController();
            openNextView(event);
            stage.setTitle("Quick Hiring - Home - " + loggedUser.getMail());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void setHomeController() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("views/home-view.fxml"));
        parentRoot = fxmlLoader.load();
        homeController = fxmlLoader.<HomeController>getController();
        homeController.setServerAndStage(server, loggedUser, stage, parentRoot);
    }

    private void openNextView(ActionEvent event) {
        stage.setScene(new Scene(parentRoot));

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                try {
                    server.logout(loggedUser);
                } catch (ServiceException exception) {
                    exception.printStackTrace();
                }
                System.exit(0);
            }
        });
    }

    @FXML
    protected void onActionWithdrawApplication() {
        ApplicationForJob application = lastApplicationSelected;
        if (application != null) {
            try {
                server.modifyApplicationStatus(application, ApplicationStatus.WITHDRAW, "Application withdrawn by the candidate");
                uploadData();
            } catch (ServiceException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadData() {
        applicationAppliedModelList.setAll(server.findAllApplicationsForUser(loggedUser, ApplicationStatus.APPLIED));
        appliedStatusList.setItems(applicationAppliedModelList);

        applicationInReviewModelList.setAll(server.findAllApplicationsForUser(loggedUser, ApplicationStatus.IN_REVIEW));
        inReviewStatusList.setItems(applicationInReviewModelList);

        applicationInterviewModelList.setAll(server.findAllApplicationsForUser(loggedUser, ApplicationStatus.INTERVIEW));
        interviewStatusList.setItems(applicationInterviewModelList);

        applicationOfferModelList.setAll(
                Stream.concat(
                        server.findAllApplicationsForUser(loggedUser, ApplicationStatus.ACCEPTED).stream(),
                        server.findAllApplicationsForUser(loggedUser, ApplicationStatus.REJECTED).stream()
                ).collect(Collectors.toList()));
        offerStatusList.setItems(applicationOfferModelList);
    }

}