import domain.Company;
import domain.Job;
import domain.Location;
import domain.User;
import enums.JobAvailability;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ManageJobsController extends UnicastRemoteObject implements Initializable, IObserver, Serializable {

    private IService server;
    private Parent parentRoot;
    private Stage stage;

    private User loggedUser;

    private HomeController homeController;
    private ManageSelectionProcessController manageSelectionProcessViewController;
    private AddOrModifyJobController addOrModifyJobController;

    ObservableList<Job> jobModelTable = FXCollections.observableArrayList();

    // FXML Components
    @FXML
    Button backViewButton;

    @FXML
    TextField searchJobTextField;

    @FXML
    ComboBox<String> orderByDate;

    @FXML
    TableView<Job> postedJobsTableView;

    @FXML
    public TableColumn<Job, String> jobTitleTableColumn;

    @FXML
    public TableColumn<Job, Company> jobCompanyTableColumn;

    @FXML
    public TableColumn<Job, Location> jobLocationTableColumn;

    @FXML
    TextArea jobInfoTextArea;

    @FXML
    Button manageSelectionButton;

    @FXML
    Button addNewJobButton;

    @FXML
    Button modifyJobButton;

    @FXML
    Button changeApplicationStatusButton;

    @FXML
    Button removeJobButton;


    public ManageJobsController() throws RemoteException {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        jobTitleTableColumn.setCellValueFactory(new PropertyValueFactory<Job, String>("title"));
        jobCompanyTableColumn.setCellValueFactory(new PropertyValueFactory<Job, Company>("company"));
        jobLocationTableColumn.setCellValueFactory(new PropertyValueFactory<Job, Location>("location"));
        postedJobsTableView.setItems(jobModelTable);
        postedJobsTableView.getSelectionModel().selectedItemProperty().addListener(
                selectedItem -> setSelectedJob()
        );
        searchJobTextField.textProperty().addListener(o -> searchJob());

        ObservableList<String> options = FXCollections.observableArrayList();
        options.setAll(Arrays.asList("Default", "Newest", "Oldest"));
        orderByDate.setItems(options);
        orderByDate.getSelectionModel().selectedItemProperty().addListener(o -> sortJobs());

        postedJobsTableView.setRowFactory(r -> new TableRow<Job>() {
            @Override
            public void updateItem(Job item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setStyle("");
                } else if (item.getJobAvailability() == JobAvailability.CLOSED_FOR_APPLICATIONS) {
                    setGraphic(null);
                    setStyle("-fx-background-color: #b6b6b6;");
                }
            }
        });
    }

    private void searchJob() {
        String info = searchJobTextField.getText();
        if (info.isEmpty())
            jobModelTable.setAll(server.findAllJobsPosted(loggedUser));
        else
            jobModelTable.setAll(server.searchJobPosted(info, loggedUser));

        if (!postedJobsTableView.getItems().isEmpty())
            postedJobsTableView.getSelectionModel().select(0);
    }

    private void setSelectedJob() {
        Job job = postedJobsTableView.getSelectionModel().getSelectedItem();
        if (job != null) {
            jobInfoTextArea.setText(job.getTitle() + "   -   Applicants: " + server.getApplicantsNumber(job.getId()) + "\n\n" +
                    job.getCompany().getName() + "\n" +
                    job.getLocation().getCountry() + ", " + job.getLocation().getCity() + "\n" +
                    job.getEmploymentType() + " | " + job.getExperienceLevel() + " | " + job.getJobType() + "\n\n" +
                    job.getDescription() + "\n\n" +
                    job.getPostingDate().getDayOfMonth() + "-" + job.getPostingDate().getMonth().toString() + "-" + job.getPostingDate().getYear()
            );
        }
    }

    protected void sortJobs() {
        if (orderByDate.getSelectionModel().getSelectedItem().equals("Default")) {
            jobModelTable.setAll(server.findAllJobsPosted(loggedUser));
        }

        if (orderByDate.getSelectionModel().getSelectedItem().equals("Oldest")) {
            jobModelTable.setAll(server.findAllJobsPosted(loggedUser).stream()
                    .sorted((j1, j2) -> j1.getPostingDate().compareTo(j2.getPostingDate()))
                    .collect(Collectors.toList())
            );
        }

        if (orderByDate.getSelectionModel().getSelectedItem().equals("Newest")) {
            jobModelTable.setAll(server.findAllJobsPosted(loggedUser).stream()
                    .sorted((j1, j2) -> j2.getPostingDate().compareTo(j1.getPostingDate()))
                    .collect(Collectors.toList())
            );
        }

        if (!postedJobsTableView.getItems().isEmpty())
            postedJobsTableView.getSelectionModel().select(0);
    }

    public void setServerAndStage(IService s, User user, Stage primaryStage, Parent root) {
        this.server = s;
        this.stage = primaryStage;
        this.parentRoot = root;
        this.loggedUser = user;

        initData();
    }

    private void initData() {
        jobModelTable.setAll(server.findAllJobsPosted(loggedUser));

        if (!postedJobsTableView.getItems().isEmpty())
            postedJobsTableView.getSelectionModel().select(0);

    }


    @FXML
    protected void onActionBackView(ActionEvent event) {
        try {
            setHomeController();
            openNextView(event);
            stage.setTitle("QuickHiring - Home - " + loggedUser.getMail());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    protected void onActionManageSelection(ActionEvent event) {
        Job job = postedJobsTableView.getSelectionModel().getSelectedItem();
        if (job != null) {
            try {
                setManageSelectionProcessViewController(job);
                openNextView(event);
                stage.setTitle("QuickHiring - Manage job selection process");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }


    @FXML
    protected void onActionAddJob(ActionEvent event) {
        try {
            setAddOrModifyJobController(null);
            openNextView(event);
            stage.setTitle("QuickHiring - Add job");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    protected void onActionModifyJob(ActionEvent event) {
        Job job = postedJobsTableView.getSelectionModel().getSelectedItem();
        if (job != null) {
            try {
                setAddOrModifyJobController(job);
                openNextView(event);
                stage.setTitle("QuickHiring - Modify job");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }


    @FXML
    protected void onActionChangeApplicationStatus(ActionEvent event) {
        Job job = postedJobsTableView.getSelectionModel().getSelectedItem();
        if (job != null) {
            try {
                server.changeJobStatus(job);
                initData();
            } catch (ServiceException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void onActionRemoveJob(ActionEvent event) {
        Job job = postedJobsTableView.getSelectionModel().getSelectedItem();
        if (job != null) {
            try {
                server.removeJob(job);
                initData();
            } catch (ServiceException e) {
                e.printStackTrace();
            }
        }
    }


    private void setHomeController() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("views/home-view.fxml"));
        parentRoot = fxmlLoader.load();
        homeController = fxmlLoader.<HomeController>getController();
        homeController.setServerAndStage(server, loggedUser, stage, parentRoot);
    }

    private void setManageSelectionProcessViewController(Job selectedJob) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("views/manage-selection-process-view.fxml"));
        parentRoot = fxmlLoader.load();
        manageSelectionProcessViewController = fxmlLoader.<ManageSelectionProcessController>getController();
        manageSelectionProcessViewController.setServerAndStage(server, loggedUser, selectedJob, stage, parentRoot);
    }

    private void setAddOrModifyJobController(Job selectedJob) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("views/add-or-modify-job-view.fxml"));
        parentRoot = fxmlLoader.load();
        addOrModifyJobController = fxmlLoader.<AddOrModifyJobController>getController();
        addOrModifyJobController.setServerAndStage(server, loggedUser, selectedJob, stage, parentRoot);
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

}