import domain.Company;
import domain.Job;
import domain.Location;
import domain.User;
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
import java.util.*;
import java.util.stream.Collectors;

public class AvailableJobsController extends UnicastRemoteObject implements Initializable, IObserver, Serializable {

    private IService server;
    private Parent parentRoot;
    private Stage stage;

    private User loggedUser;

    private HomeController homeController;

    ObservableList<Job> jobModelTable = FXCollections.observableArrayList();


    // FXML Components

    @FXML
    Button backButton;

    @FXML
    TableView<Job> jobsTableView;

    @FXML
    Label selectedJobLabel;

    @FXML
    public TableColumn<Job, String> jobTitleTableColumn;

    @FXML
    public TableColumn<Job, Company> jobCompanyTableColumn;

    @FXML
    public TableColumn<Job, Location> jobLocationTableColumn;

    @FXML
    TextField searchBarTextField;

    @FXML
    ComboBox<String> orderByDate;

    @FXML
    Button applyButton;

    public AvailableJobsController() throws RemoteException {
    }


    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        jobTitleTableColumn.setCellValueFactory(new PropertyValueFactory<Job, String>("title"));
        jobCompanyTableColumn.setCellValueFactory(new PropertyValueFactory<Job, Company>("company"));
        jobLocationTableColumn.setCellValueFactory(new PropertyValueFactory<Job, Location>("location"));
        jobsTableView.setItems(jobModelTable);
        jobsTableView.getSelectionModel().selectedItemProperty().addListener(
                selectedItem -> setSelectedJob()
        );

        searchBarTextField.textProperty().addListener(o -> searchJob());
    }

    private void searchJob() {
        String info = searchBarTextField.getText();
        if (info.isEmpty())
            jobModelTable.setAll(server.findAllJobsAvailable());
        else
            jobModelTable.setAll(server.searchJob(info));

        if (!jobsTableView.getItems().isEmpty())
            jobsTableView.getSelectionModel().select(0);
    }

    private void setSelectedJob() {
        Job job = jobsTableView.getSelectionModel().getSelectedItem();
        if (job != null) {
            if (server.appliedForThis(loggedUser.getId(), job.getId())) {
                applyButton.setDisable(true);
                applyButton.setText("Applied");
            } else {
                applyButton.setDisable(false);
                applyButton.setText("Apply");
            }
            selectedJobLabel.setText(job.getTitle() + "   -   Applicants: " + server.getApplicantsNumber(job.getId()) + "\n\n" +
                    job.getCompany().getName() + "\n" +
                    job.getLocation().getCountry() + ", " + job.getLocation().getCity() + "\n" +
                    job.getEmploymentType() + " | " + job.getExperienceLevel() + " | " + job.getJobType() + "\n\n" +
                    job.getDescription() + "\n\n" +
                    job.getPostingDate().getDayOfMonth() + "-" + job.getPostingDate().getMonth().toString() + "-" + job.getPostingDate().getYear()
            );
        }
    }

    public void setServerAndStage(IService s, User user, Stage primaryStage, Parent root) {
        this.server = s;
        this.stage = primaryStage;
        this.parentRoot = root;
        this.loggedUser = user;

        initData();
    }

    private void initData() {
        try {
            jobModelTable.setAll(server.findAllJobsAvailable());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        if (!jobsTableView.getItems().isEmpty())
            jobsTableView.getSelectionModel().select(0);


        ObservableList<String> options = FXCollections.observableArrayList();
        options.setAll(Arrays.asList("Default", "Newest", "Oldest"));
        orderByDate.setItems(options);
        orderByDate.getSelectionModel().selectedItemProperty().addListener(o -> sortJobs());
    }

    @FXML
    protected void onActionBackView(ActionEvent event) {
        try {
            setHomeController();
            openHomeView(loggedUser, event);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void setHomeController() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("views/home-view.fxml"));
        parentRoot = fxmlLoader.load();
        homeController = fxmlLoader.<HomeController>getController();
    }

    private void openHomeView(User currentUser, ActionEvent actionEvent) {
        homeController.setServerAndStage(server, currentUser, stage, parentRoot);

        stage.setScene(new Scene(parentRoot));

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                try {
                    server.logout(currentUser);
                } catch (ServiceException exception) {
                    exception.printStackTrace();
                }
                System.exit(0);
            }
        });

        stage.setTitle("Quick Hiring - Home - " + currentUser.getMail());
    }


    protected void sortJobs() {
        if (orderByDate.getSelectionModel().getSelectedItem().equals("Default")) {
            jobModelTable.setAll(server.findAllJobsAvailable());
        }

        if (orderByDate.getSelectionModel().getSelectedItem().equals("Oldest")) {
            jobModelTable.setAll(server.findAllJobsAvailable().stream()
                    .sorted((j1, j2) -> j1.getPostingDate().compareTo(j2.getPostingDate()))
                    .collect(Collectors.toList())
            );
        }

        if (orderByDate.getSelectionModel().getSelectedItem().equals("Newest")) {
            jobModelTable.setAll(server.findAllJobsAvailable().stream()
                    .sorted((j1, j2) -> j2.getPostingDate().compareTo(j1.getPostingDate()))
                    .collect(Collectors.toList())
            );
        }

        if (!jobsTableView.getItems().isEmpty())
            jobsTableView.getSelectionModel().select(0);
    }

    @FXML
    protected void onActionApply(ActionEvent event) {
        Job selectedJob = jobsTableView.getSelectionModel().getSelectedItem();
        if (selectedJob != null && applyButton.getText().equals("Apply")) {
            try {
                server.applyForJob(selectedJob, loggedUser);

                applyButton.setDisable(true);
                applyButton.setText("Applied");
            } catch (ServiceException e) {
                e.printStackTrace();
            }
        }
    }

}