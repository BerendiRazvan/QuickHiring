import domain.ApplicationForJob;
import domain.ImageData;
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
import javafx.scene.image.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.io.Serializable;
import java.math.RoundingMode;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ManageSelectionProcessController extends UnicastRemoteObject implements Initializable, IObserver, Serializable {

    private IService server;
    private Parent parentRoot;
    private Stage stage;

    private User loggedUser;
    private Job selectedJob;
    private Image imageProfile;

    private ManageJobsController manageJobsController;

    private ApplicationForJob lastApplicationSelected;

    ObservableList<ApplicationForJob> applicationAppliedModelList = FXCollections.observableArrayList();
    ObservableList<ApplicationForJob> applicationInReviewModelList = FXCollections.observableArrayList();
    ObservableList<ApplicationForJob> applicationInterviewModelList = FXCollections.observableArrayList();
    ObservableList<ApplicationForJob> applicationOfferModelList = FXCollections.observableArrayList();

    // FXML Components

    @FXML
    Label jobDetailsLabel;

    @FXML
    Label candidateInfoLabel;

    @FXML
    Label errorLabel;

    @FXML
    ListView<ApplicationForJob> appliedStatusList;

    @FXML
    ListView<ApplicationForJob> inReviewStatusList;

    @FXML
    ListView<ApplicationForJob> interviewStatusList;

    @FXML
    ListView<ApplicationForJob> offerStatusList;

    @FXML
    TextArea stepDetailsTextArea;

    @FXML
    Button moveApplicationLButton;

    @FXML
    Button moveApplicationRButton;

    @FXML
    Button rejectButton;

    @FXML
    Button addDetailsButton;

    @FXML
    Button backViewButton;


    @FXML
    ImageView imageProfileView;

    public ManageSelectionProcessController() throws RemoteException {

    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        disableButtons();
    }

    private void disableButtons() {
        addDetailsButton.setDisable(true);
        moveApplicationLButton.setDisable(true);
        moveApplicationRButton.setDisable(true);
        rejectButton.setDisable(true);
    }

    private void enableButtons() {
        if (lastApplicationSelected != null) {
            addDetailsButton.setDisable(false);
            if (lastApplicationSelected.getApplicationStatus() != ApplicationStatus.ACCEPTED &&
                    lastApplicationSelected.getApplicationStatus() != ApplicationStatus.REJECTED) {
                if (lastApplicationSelected.getApplicationStatus() != ApplicationStatus.APPLIED)
                    moveApplicationLButton.setDisable(false);
                moveApplicationRButton.setDisable(false);
                rejectButton.setDisable(false);
            }
        } else {
            disableButtons();
        }
    }

    public void setServerAndStage(IService s, User user, Job job, Stage primaryStage, Parent root) {
        this.server = s;
        this.stage = primaryStage;
        this.parentRoot = root;
        this.loggedUser = user;
        this.selectedJob = job;

        initData();
    }

    private void initData() {
        jobDetailsLabel.setText(selectedJob.toString());

        uploadData();
        setListListeners();

    }

    private void setListListeners() {
        appliedStatusList.getSelectionModel().selectedItemProperty().addListener(o -> {
            lastApplicationSelected = appliedStatusList.getSelectionModel().getSelectedItem();
            displayApplicationInfo(lastApplicationSelected);
            inReviewStatusList.getSelectionModel().clearSelection();
            interviewStatusList.getSelectionModel().clearSelection();
            offerStatusList.getSelectionModel().clearSelection();
        });

        inReviewStatusList.getSelectionModel().selectedItemProperty().addListener(o -> {
            lastApplicationSelected = inReviewStatusList.getSelectionModel().getSelectedItem();
            displayApplicationInfo(lastApplicationSelected);
            appliedStatusList.getSelectionModel().clearSelection();
            interviewStatusList.getSelectionModel().clearSelection();
            offerStatusList.getSelectionModel().clearSelection();
        });

        interviewStatusList.getSelectionModel().selectedItemProperty().addListener(o -> {
            lastApplicationSelected = interviewStatusList.getSelectionModel().getSelectedItem();
            displayApplicationInfo(lastApplicationSelected);
            appliedStatusList.getSelectionModel().clearSelection();
            inReviewStatusList.getSelectionModel().clearSelection();
            offerStatusList.getSelectionModel().clearSelection();
        });

        offerStatusList.getSelectionModel().selectedItemProperty().addListener(o -> {
            lastApplicationSelected = offerStatusList.getSelectionModel().getSelectedItem();
            displayApplicationInfo(lastApplicationSelected);
            appliedStatusList.getSelectionModel().clearSelection();
            inReviewStatusList.getSelectionModel().clearSelection();
            interviewStatusList.getSelectionModel().clearSelection();
        });

    }

    private void displayApplicationInfo(ApplicationForJob application) {
        if (application != null) {
            enableButtons();

            User candidate = application.getCandidateResume().getOwner();
            candidateInfoLabel.setText(candidate.getFirstName() + " " + candidate.getLastName() + "\n\n" +
                    "Contact data\n" +
                    "Mail: " + candidate.getMail() + "\n" +
                    "Phone number: " + candidate.getPhoneNumber() + "\n\n\n" +
                    "Application details:\n\n" +
                    application.getApplicationDetails());

            // plus incarcare imagine profil mai incolo

            imageProfile = getProfileImage(candidate);
            imageProfileView.setImage(imageProfile);

        } else {
            candidateInfoLabel.setText("");
        }
    }

    private Image getProfileImage(User candidate) {
        ImageData dataImageBD = null;
        if (candidate != null) {
            dataImageBD = candidate.getProfileImage();
        }
        if (dataImageBD == null)
            dataImageBD = server.getProfileImage(1L);

        int width = dataImageBD.getWidth();
        int height = dataImageBD.getHeight();

        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        pixelWriter.setPixels(0, 0, width, height, PixelFormat.getByteBgraInstance(), dataImageBD.getImageData(), 0, height * 4);

        return writableImage;
    }

    @FXML
    protected void onActionBackView(ActionEvent event) {
        try {
            setManageJobsController();
            openNextView(event);
            stage.setTitle("QuickHiring - Manage jobs");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    protected void onActionMoveLeftApplication(ActionEvent event) {
        boolean errors = false;

        if (stepDetailsTextArea.getText().isEmpty()) {
            errors = true;
            errorLabel.setText("Next step details is empty\nPlease provide information for candidate about the next step");
        }

        if (lastApplicationSelected != null && !errors) {
            try {
                server.moveApplicationBackward(lastApplicationSelected, stepDetailsTextArea.getText());
                disableButtons();
                uploadData();
                displayApplicationInfo(lastApplicationSelected);
                errorLabel.setText("");
                stepDetailsTextArea.setText("");
            } catch (ServiceException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void onActionMoveRightApplication(ActionEvent event) {
        boolean errors = false;

        if (stepDetailsTextArea.getText().isEmpty()) {
            errors = true;
            errorLabel.setText("Next step details is empty\nPlease provide information for candidate about the next step");
        }

        if (lastApplicationSelected != null && !errors) {
            try {
                server.moveApplicationForward(lastApplicationSelected, stepDetailsTextArea.getText());
                disableButtons();
                uploadData();
                displayApplicationInfo(lastApplicationSelected);
                errorLabel.setText("");
                stepDetailsTextArea.setText("");
            } catch (ServiceException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void onActionRejectCandidate(ActionEvent event) {
        boolean errors = false;

        if (stepDetailsTextArea.getText().isEmpty()) {
            errors = true;
            errorLabel.setText("Next step details is empty\nPlease provide information for candidate about the next step");
        }

        if (lastApplicationSelected != null && !errors) {
            try {
                server.modifyApplicationStatus(lastApplicationSelected, ApplicationStatus.REJECTED, stepDetailsTextArea.getText());
                disableButtons();
                uploadData();
                displayApplicationInfo(lastApplicationSelected);
                errorLabel.setText("");
                stepDetailsTextArea.setText("");
            } catch (ServiceException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void onActionAddDetails(ActionEvent event) {
        boolean errors = false;
        if (stepDetailsTextArea.getText().isEmpty()) {
            errors = true;
            errorLabel.setText("Next step details is empty\nPlease provide information for candidate about the next step");
        }

        if (lastApplicationSelected != null && !errors) {
            try {
                server.modifyApplicationStatus(lastApplicationSelected, lastApplicationSelected.getApplicationStatus(), stepDetailsTextArea.getText());
                disableButtons();
                uploadData();
                displayApplicationInfo(lastApplicationSelected);
                errorLabel.setText("");
                stepDetailsTextArea.setText("");
            } catch (ServiceException e) {
                e.printStackTrace();
            }
        }
    }


    private void setManageJobsController() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("views/manage-jobs-view.fxml"));
        parentRoot = fxmlLoader.load();
        manageJobsController = fxmlLoader.<ManageJobsController>getController();
        manageJobsController.setServerAndStage(server, loggedUser, stage, parentRoot);
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

    private void uploadData() {
        applicationAppliedModelList.setAll(server.findAllApplicationsForJob(selectedJob, ApplicationStatus.APPLIED));
        appliedStatusList.setItems(applicationAppliedModelList);

        applicationInReviewModelList.setAll(server.findAllApplicationsForJob(selectedJob, ApplicationStatus.IN_REVIEW));
        inReviewStatusList.setItems(applicationInReviewModelList);

        applicationInterviewModelList.setAll(server.findAllApplicationsForJob(selectedJob, ApplicationStatus.INTERVIEW));
        interviewStatusList.setItems(applicationInterviewModelList);

        applicationOfferModelList.setAll(
                Stream.concat(
                        server.findAllApplicationsForJob(selectedJob, ApplicationStatus.ACCEPTED).stream(),
                        server.findAllApplicationsForJob(selectedJob, ApplicationStatus.REJECTED).stream()
                ).collect(Collectors.toList()));
        offerStatusList.setItems(applicationOfferModelList);

        setCellFactoryForList(appliedStatusList);
        setCellFactoryForList(inReviewStatusList);
        setCellFactoryForList(interviewStatusList);
        setCellFactoryForList(offerStatusList);
    }

    private void setCellFactoryForList(ListView<ApplicationForJob> listView) {
        listView.setCellFactory(r -> new ListCell<ApplicationForJob>() {
            @Override
            public void updateItem(ApplicationForJob item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText(null);
                } else {

                    User candidate = item.getCandidateResume().getOwner();
                    double matchingValue = server.getResumeJobMatchingScore(item.getCandidateResume(), item.getJobApplied()) * 100;
                    DecimalFormat df = new DecimalFormat("#.##");
                    df.setRoundingMode(RoundingMode.DOWN);
                    String formattedValue = df.format(matchingValue);
                    setText(candidate.getFirstName() + " " + candidate.getLastName() + " - " +
                            formattedValue + "%");

                    if (item == null) {
                        setStyle("");
                    } else if (item.getApplicationStatus() == ApplicationStatus.REJECTED) {
                        setText(candidate.getFirstName() + " " + candidate.getLastName() + " - " +
                                formattedValue + "%");
                        setGraphic(null);
                        setStyle("-fx-background-color: #ffcfc2;");
                    } else if (item.getApplicationStatus() == ApplicationStatus.ACCEPTED) {
                        setText(candidate.getFirstName() + " " + candidate.getLastName() + " - " +
                                formattedValue + "%");
                        setGraphic(null);
                        setStyle("-fx-background-color: #d7ffc8;");
                    } else {
                        setText(candidate.getFirstName() + " " + candidate.getLastName() + " - " +
                                formattedValue + "%");
                    }
                }
            }
        });
    }

}