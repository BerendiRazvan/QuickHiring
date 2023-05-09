
import domain.ImageData;
import domain.Job;
import domain.User;
import enums.AccountType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ResourceBundle;

public class HomeController extends UnicastRemoteObject implements Initializable, IObserver, Serializable {

    private IService server;
    private Parent parentRoot;
    private Stage stage;

    private User loggedUser;
    private Image imageProfile;

    private AddOrModifyAccountController addOrModifyAccountController;
    private AvailableJobsController availableJobsController;
    private JobsAppliedController jobsAppliedController;
    private ManageJobsController manageJobsController;

    ObservableList<Job> jobModelList = FXCollections.observableArrayList();

    // FXML Components

    @FXML
    Button jobsAvailableButton;

    @FXML
    Button quickApplyButton;

    @FXML
    Button applicationButton;

    @FXML
    Button manageJobsButton;

    @FXML
    Button editAccountButton;

    @FXML
    Label userInfoLabel;

    @FXML
    Label jobsLabel;

    @FXML
    ListView<Job> jobsList;

    @FXML
    ImageView imageProfileView;

    public HomeController() throws RemoteException {
    }

    public void setServerAndStage(IService s, User user, Stage primaryStage, Parent root) {
        this.server = s;
        this.stage = primaryStage;
        this.parentRoot = root;
        this.loggedUser = user;

        initData();
    }

    private void initData() {
        jobsList.setCellFactory(r -> new ListCell<Job>() {
            @Override
            public void updateItem(Job item, boolean empty) {
                super.updateItem(item, empty);

                if (item != null) {
                    if (loggedUser.getAccountType() == AccountType.RECRUITER)
                        setText(item.toString() + server.getApplicantsNumber(item.getId()));
                    else
                        setText(item.toString());
                }
            }
        });

        userInfoLabel.setText(loggedUser.getFirstName() + "\n" + loggedUser.getLastName() + "\n" + loggedUser.getMail());

        // set profile image
        imageProfile = getProfileImage();
        imageProfileView.setImage(imageProfile);

        if (loggedUser.getAccountType() == AccountType.RECRUITER)
            initRecruiterPage();
        else
            initCandidatePage();
    }

    private Image getProfileImage() {
        ImageData dataImageBD = null;
        if (loggedUser != null) {
            dataImageBD = loggedUser.getProfileImage();
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

    private void initCandidatePage() {
        quickApplyButton.setVisible(true);
        quickApplyButton.setDisable(true);
        jobsAvailableButton.setVisible(true);
        applicationButton.setVisible(true);
        manageJobsButton.setVisible(false);
        jobsLabel.setText("Best jobs recommendations for you");
        try {
            jobModelList.setAll(server.findBestJobs(5, server.getUserResume(loggedUser.getId())));
            jobsList.setItems(jobModelList);
            jobsList.getSelectionModel().selectedItemProperty().addListener(o -> {
                Job selectedJob = jobsList.getSelectionModel().getSelectedItem();
                if (selectedJob != null) {
                    if (server.appliedForThis(loggedUser.getId(), selectedJob.getId())) {
                        quickApplyButton.setDisable(true);
                        quickApplyButton.setText("Applied");
                    } else {
                        quickApplyButton.setText("Quick apply");
                        quickApplyButton.setDisable(false);
                    }
                } else {
                    quickApplyButton.setDisable(true);
                }
            });
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }

    private void initRecruiterPage() {
        quickApplyButton.setVisible(false);
        jobsAvailableButton.setVisible(false);
        applicationButton.setVisible(false);
        manageJobsButton.setVisible(true);
        jobsLabel.setText("Jobs posted");
        jobModelList.setAll(server.findAllJobsPosted(loggedUser));
        jobsList.setItems(jobModelList);
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    protected void onActionOpenAddOrModifyAccountView(ActionEvent event) {
        try {
            setAddOrModifyAccountController();
            openNextView(event);
            stage.setTitle("Quick Hiring - Edit account");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void setAddOrModifyAccountController() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("views/add-or-modify-account-view.fxml"));
        parentRoot = fxmlLoader.load();
        addOrModifyAccountController = fxmlLoader.<AddOrModifyAccountController>getController();
        addOrModifyAccountController.setServerAndStage(server, loggedUser, stage, parentRoot);
    }

    @FXML
    protected void onActionJobsAvailable(ActionEvent event) {
        try {
            setAvailableJobsController();
            openNextView(event);
            stage.setTitle("Quick Hiring - Jobs available");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
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

    private void setAvailableJobsController() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("views/available-jobs-view.fxml"));
        parentRoot = fxmlLoader.load();
        availableJobsController = fxmlLoader.<AvailableJobsController>getController();
        availableJobsController.setServerAndStage(server, loggedUser, stage, parentRoot);
    }


    @FXML
    protected void onActionMyApplications(ActionEvent event) {
        try {
            setJobsAppliedController();
            openNextView(event);
            stage.setTitle("Quick Hiring - My applications");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void setJobsAppliedController() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("views/jobs-applied-view.fxml"));
        parentRoot = fxmlLoader.load();
        jobsAppliedController = fxmlLoader.<JobsAppliedController>getController();
        jobsAppliedController.setServerAndStage(server, loggedUser, stage, parentRoot);
    }

    @FXML
    protected void onActionManageJobs(ActionEvent event) {
        try {
            setManageJobsController();
            openNextView(event);
            stage.setTitle("Quick Hiring - Manage jobs");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void setManageJobsController() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("views/manage-jobs-view.fxml"));
        parentRoot = fxmlLoader.load();
        manageJobsController = fxmlLoader.<ManageJobsController>getController();
        manageJobsController.setServerAndStage(server, loggedUser, stage, parentRoot);
    }

    @FXML
    protected void onActionQuickApply(ActionEvent event) {
        Job selectedJob = jobsList.getSelectionModel().getSelectedItem();
        if (selectedJob != null && quickApplyButton.getText().equals("Quick apply")) {
            try {
                server.applyForJob(selectedJob, loggedUser);

                quickApplyButton.setDisable(true);
                quickApplyButton.setText("Applied");
            } catch (ServiceException e) {
                e.printStackTrace();
            }
        }
    }
}