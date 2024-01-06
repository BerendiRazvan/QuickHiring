import domain.Company;
import domain.Job;
import domain.Location;
import domain.User;
import enums.*;
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
import javafx.util.Duration;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class AddOrModifyJobController extends UnicastRemoteObject implements Initializable, IObserver, Serializable {

    private IService server;
    private Parent parentRoot;
    private Stage stage;

    private User loggedUser;
    private Job selectedJob;

    private ManageJobsController manageJobsController;

    ObservableList<Company> companyModelList = FXCollections.observableArrayList();

    // FXML Components

    @FXML
    Button backViewButton;

    @FXML
    Button addModifyButton;

    @FXML
    Button infoButton;

    @FXML
    TextField titleTextField;

    @FXML
    CheckBox openForApplicationsCheckBox;

    @FXML
    TextArea descriptionTextArea;

    @FXML
    ListView<Company> companyList;

    @FXML
    ComboBox<Location> locationsComboBox;

    @FXML
    ComboBox<JobType> jobTypeComboBox;

    @FXML
    ComboBox<ExperienceLevel> experienceLevelComboBox;

    @FXML
    ComboBox<EmploymentType> employmentTypeComboBox;

    @FXML
    Label errorJTitleLabel;

    @FXML
    Label errorJDLabel;

    @FXML
    Label errorELLabel;

    @FXML
    Label errorETLabel;

    @FXML
    Label errorJTLabel;

    @FXML
    Label errorCLLabel;

    @FXML
    Label errorActionLabel;

    @FXML
    Label pageLabel;


    public AddOrModifyJobController() throws RemoteException {
    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        jobTypeComboBox.setItems(FXCollections.observableArrayList(JobType.values()));
        experienceLevelComboBox.setItems(FXCollections.observableArrayList(ExperienceLevel.values()));
        employmentTypeComboBox.setItems(FXCollections.observableArrayList(EmploymentType.values()));

        Tooltip tooltip = new Tooltip(
                "If you did not find your company in this list, please contact us at the \n" +
                        "email address: suport@quickhiring.com to present you the offers available\n" +
                        "for a collaboration and to register the company with the necessary\n" +
                        "information in our database.");
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setHideDelay(Duration.ZERO);
        infoButton.setTooltip(tooltip);
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

        pageLabel.setText("Add job");
        addModifyButton.setText("Add job");

        companyModelList.setAll(server.getAllCompanies());
        companyList.setItems(companyModelList);
        companyList.getSelectionModel().selectedItemProperty().addListener(o -> {
            locationsComboBox.setDisable(false);
            Company company = companyList.getSelectionModel().getSelectedItem();
            locationsComboBox.setItems(FXCollections.observableArrayList(company.getLocations()));
        });

        if (selectedJob != null) {
            addModifyButton.setText("Modify job");
            pageLabel.setText("Modify job");

            titleTextField.setText(selectedJob.getTitle());
            descriptionTextArea.setText(selectedJob.getDescription());
            jobTypeComboBox.setValue(selectedJob.getJobType());
            experienceLevelComboBox.setValue(selectedJob.getExperienceLevel());
            employmentTypeComboBox.setValue(selectedJob.getEmploymentType());

            if (selectedJob.getJobAvailability() == JobAvailability.OPEN_FOR_APPLICATIONS)
                openForApplicationsCheckBox.setSelected(true);
            else if (selectedJob.getJobAvailability() == JobAvailability.CLOSED_FOR_APPLICATIONS)
                openForApplicationsCheckBox.setSelected(false);

            if (!companyList.getItems().isEmpty()) {
                Optional<Company> company = companyList.getItems()
                        .stream()
                        .filter(c -> c.getId().equals(selectedJob.getCompany().getId()))
                        .findFirst();
                company.ifPresent(value -> companyList.getSelectionModel().select(value));
                locationsComboBox.setValue(selectedJob.getLocation());
            }
        }

        setListeners();
        clearErrors();
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

    private void clearErrors() {
        errorJTitleLabel.setText("");
        errorJDLabel.setText("");
        errorJTLabel.setText("");
        errorELLabel.setText("");
        errorETLabel.setText("");
        errorCLLabel.setText("");
        errorActionLabel.setText("");
    }

    private void setListeners() {
        titleTextField.textProperty().addListener(o -> {
            errorJTitleLabel.setText("");
            errorActionLabel.setText("");
            titleTextField.getStylesheets().clear();
            titleTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-style.css")).toExternalForm());
        });

        descriptionTextArea.textProperty().addListener(o -> {
            errorJDLabel.setText("");
            errorActionLabel.setText("");
            descriptionTextArea.getStylesheets().clear();
            descriptionTextArea.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-area-style.css")).toExternalForm());
        });

        experienceLevelComboBox.valueProperty().addListener(o -> {
            errorELLabel.setText("");
            errorActionLabel.setText("");
            experienceLevelComboBox.getStylesheets().clear();
            experienceLevelComboBox.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/combo-box-style.css")).toExternalForm());
        });

        employmentTypeComboBox.valueProperty().addListener(o -> {
            errorETLabel.setText("");
            errorActionLabel.setText("");
            employmentTypeComboBox.getStylesheets().clear();
            employmentTypeComboBox.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/combo-box-style.css")).toExternalForm());
        });

        jobTypeComboBox.valueProperty().addListener(o -> {
            errorJTLabel.setText("");
            errorActionLabel.setText("");
            jobTypeComboBox.getStylesheets().clear();
            jobTypeComboBox.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/combo-box-style.css")).toExternalForm());
        });

        locationsComboBox.valueProperty().addListener(o -> {
            errorCLLabel.setText("");
            errorActionLabel.setText("");
            locationsComboBox.getStylesheets().clear();
            locationsComboBox.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/combo-box-style.css")).toExternalForm());
        });

    }

    @FXML
    protected void anActionAddOrModifyJob(ActionEvent event) {
        boolean error = false;

        if (titleTextField.getText().isEmpty()) {
            error = true;
            errorJTitleLabel.setText("Title is empty");
            titleTextField.getStylesheets().clear();
            titleTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-error-style.css")).toExternalForm());
        }
        if (descriptionTextArea.getText().isEmpty()) {
            error = true;
            errorJDLabel.setText("Description is empty");
            descriptionTextArea.getStylesheets().clear();
            descriptionTextArea.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-area-error-style.css")).toExternalForm());
        }
        if (jobTypeComboBox.getValue() == null) {
            error = true;
            errorJTLabel.setText("Job type is not selected");
            jobTypeComboBox.getStylesheets().clear();
            jobTypeComboBox.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/combo-box-error-style.css")).toExternalForm());
        }
        if (experienceLevelComboBox.getValue() == null) {
            error = true;
            errorELLabel.setText("Experience level is not selected");
            experienceLevelComboBox.getStylesheets().clear();
            experienceLevelComboBox.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/combo-box-error-style.css")).toExternalForm());
        }
        if (employmentTypeComboBox.getValue() == null) {
            error = true;
            errorETLabel.setText("Employment type is not selected");
            employmentTypeComboBox.getStylesheets().clear();
            employmentTypeComboBox.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/combo-box-error-style.css")).toExternalForm());
        }
        if (locationsComboBox.getValue() == null) {
            error = true;
            errorCLLabel.setText("Company location is not selected");
            locationsComboBox.getStylesheets().clear();
            locationsComboBox.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/combo-box-error-style.css")).toExternalForm());
        }

        if (!error) {

            Job job = new Job();
            job.setTitle(titleTextField.getText());
            job.setDescription(descriptionTextArea.getText());
            job.setPostingDate(LocalDateTime.now());
            job.setJobType(jobTypeComboBox.getValue());
            job.setExperienceLevel(experienceLevelComboBox.getValue());
            job.setEmploymentType(employmentTypeComboBox.getValue());
            job.setCompany(companyList.getSelectionModel().getSelectedItem());
            job.setLocation(locationsComboBox.getValue());
            job.setJobAvailability(openForApplicationsCheckBox.isSelected() ?
                    JobAvailability.OPEN_FOR_APPLICATIONS : JobAvailability.CLOSED_FOR_APPLICATIONS);
            job.setRecruiter(loggedUser);

            if (selectedJob == null) {
                // add
                try {
                    server.addJob(job);
                    clearErrors();
                } catch (ServiceException e) {
                    errorActionLabel.setText("Invalid job!");
                    setErrors(e.getMessage());
                }

            } else {
                // modify
                try {
                    server.modifyJob(selectedJob.getId(), job);
                    clearErrors();
                } catch (ServiceException e) {
                    errorActionLabel.setText("Invalid job!");
                    setErrors(e.getMessage());
                }
            }

            // back to manage jobs
            try {
                setManageJobsController();
                openNextView(event);
                stage.setTitle("QuickHiring - Manage jobs");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void setErrors(String message) {
        boolean ok = true;
        if (message.toLowerCase().contains("title")) {
            errorJTitleLabel.setText(message);
            ok = false;
            titleTextField.getStylesheets().clear();
            titleTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-error-style.css")).toExternalForm());
        }
        if (message.toLowerCase().contains("description")) {
            errorJDLabel.setText(message);
            ok = false;
            descriptionTextArea.getStylesheets().clear();
            descriptionTextArea.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-area-error-style.css")).toExternalForm());
        }
        if (message.toLowerCase().contains("location") || message.toLowerCase().contains("company")) {
            errorCLLabel.setText(message);
            ok = false;
            locationsComboBox.getStylesheets().clear();
            locationsComboBox.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/combo-box-error-style.css")).toExternalForm());
        }
        if (message.toLowerCase().contains("experience level")) {
            errorELLabel.setText(message);
            ok = false;
            experienceLevelComboBox.getStylesheets().clear();
            experienceLevelComboBox.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/combo-box-error-style.css")).toExternalForm());
        }
        if (message.toLowerCase().contains("employment type")) {
            errorETLabel.setText(message);
            ok = false;
            employmentTypeComboBox.getStylesheets().clear();
            employmentTypeComboBox.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/combo-box-error-style.css")).toExternalForm());
        }
        if (message.toLowerCase().contains("job type")) {
            errorJTLabel.setText(message);
            ok = false;
            jobTypeComboBox.getStylesheets().clear();
            jobTypeComboBox.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/combo-box-error-style.css")).toExternalForm());
        }
        if (ok)
            errorActionLabel.setText(message);
    }

}
