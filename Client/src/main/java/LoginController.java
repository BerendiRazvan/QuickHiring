import domain.User;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class LoginController extends UnicastRemoteObject implements Serializable {

    private IService server;
    private Parent parentRoot;
    private Stage stage;

    private HomeController homeController;
    private AddOrModifyAccountController addOrModifyAccountController;


    // FXML Components

    @FXML
    Label mailLabel;

    @FXML
    Label passwordLabel;

    @FXML
    Label errorLabel;

    @FXML
    Label pageTitleLabel;

    @FXML
    TextField mailTextField;

    @FXML
    TextField passwordTextField;

    @FXML
    Button loginButton;

    @FXML
    Button createAccountButton;


    public LoginController() throws RemoteException {
    }

    public void setServerAndStage(IService s, Stage primaryStage, Parent root) {
        this.server = s;
        this.stage = primaryStage;
        this.parentRoot = root;
    }


    @FXML
    protected void initialize() {
    }

    @FXML
    protected void onActionLogin(ActionEvent event) {
        String errors = "";

        if (mailTextField.getText().trim().isEmpty())
            errors = "Please enter your mail!";
        if (passwordTextField.getText().trim().isEmpty())
            errors = "Please enter your password!";
        if (mailTextField.getText().trim().isEmpty() && passwordTextField.getText().trim().isEmpty())
            errors = "Please enter your mail and password!";

        if (errors.equals("")) {
            User userToLogin = User.builder()
                    .mail(mailTextField.getText().trim())
                    .password(passwordTextField.getText().trim())
                    .build();
            try {
                setHomeController();
                User currentUser = server.login(userToLogin, homeController);
                openHomeView(currentUser, event);
            } catch (ServiceException | IOException exception) {
                errorLabel.setText(exception.getMessage());
            }

        } else {
            errorLabel.setText(errors);
        }
    }


    @FXML
    protected void onActionOpenAddOrModifyAccountView(ActionEvent event) {
        try {
            setAddOrModifyAccountController();
            openAddOrModifyAccountView(event);
        } catch (IOException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    private void setHomeController() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("views/home-view.fxml"));
        parentRoot = fxmlLoader.load();
        homeController = fxmlLoader.<HomeController>getController();
    }

    private void setAddOrModifyAccountController() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("views/add-or-modify-account-view.fxml"));
        parentRoot = fxmlLoader.load();
        addOrModifyAccountController = fxmlLoader.<AddOrModifyAccountController>getController();
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

        stage.setTitle("QuickHiring - Home - " + currentUser.getMail());
    }

    private void openAddOrModifyAccountView(ActionEvent actionEvent) {
        addOrModifyAccountController.setServerAndStage(server, null, stage, parentRoot);

        stage.setScene(new Scene(parentRoot));

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
            }
        });

        stage.setTitle("QuickHiring - Create account");
    }

}
