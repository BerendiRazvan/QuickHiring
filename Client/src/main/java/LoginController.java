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
import java.util.Locale;
import java.util.Objects;

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
    Label errorEmailLabel;

    @FXML
    Label errorPasswordLabel;


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

        clearErrors();
        setListeners();
    }


    @FXML
    protected void initialize() {
    }

    private void clearErrors() {
        errorEmailLabel.setText("");
        errorPasswordLabel.setText("");
        errorLabel.setText("");
    }

    private void setListeners() {
        mailTextField.textProperty().addListener(o -> {
            errorEmailLabel.setText("");
            errorLabel.setText("");
            mailTextField.getStylesheets().clear();
            mailTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-style.css")).toExternalForm());
        });

        passwordTextField.textProperty().addListener(o -> {
            errorPasswordLabel.setText("");
            errorLabel.setText("");
            passwordTextField.getStylesheets().clear();
            passwordTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-style.css")).toExternalForm());
        });

    }

    @FXML
    protected void onActionLogin(ActionEvent event) {
        String errors = "";
        clearErrors();

        if (mailTextField.getText().trim().isEmpty()) {
            errors = "Please enter your mail";
            errorEmailLabel.setText("Please enter your mail");
            mailTextField.getStylesheets().clear();
            mailTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-error-style.css")).toExternalForm());
        }
        if (passwordTextField.getText().trim().isEmpty()) {
            errors = "Please enter your password";
            errorPasswordLabel.setText("Please enter your password");
            passwordTextField.getStylesheets().clear();
            passwordTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-error-style.css")).toExternalForm());
        }
        if (mailTextField.getText().trim().isEmpty() && passwordTextField.getText().trim().isEmpty()) {
            errors = "Please enter your mail and password!";
            errorEmailLabel.setText("Please enter your mail");
            errorPasswordLabel.setText("Please enter your password");
            passwordTextField.getStylesheets().clear();
            passwordTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-error-style.css")).toExternalForm());
            mailTextField.getStylesheets().clear();
            mailTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-error-style.css")).toExternalForm());
        }
        if (errors.equals("")) {
            User userToLogin = User.builder()
                    .mail(mailTextField.getText().trim())
                    .password(passwordTextField.getText().trim())
                    .build();
            try {
                setHomeController();
                User currentUser = server.login(userToLogin, homeController);
                openHomeView(currentUser, event);
                clearErrors();
            } catch (ServiceException | IOException exception) {
                errorLabel.setText("Login failed!");
                if (exception.getMessage().toLowerCase(Locale.ROOT).contains("email")) {
                    errorEmailLabel.setText(exception.getMessage().replace("Login failed!", ""));
                    mailTextField.getStylesheets().clear();
                    mailTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                            .getResource("styles/text-field-error-style.css")).toExternalForm());
                }
                if (exception.getMessage().toLowerCase(Locale.ROOT).contains("password")) {
                    errorPasswordLabel.setText(exception.getMessage().replace("Login failed!", ""));
                    passwordTextField.getStylesheets().clear();
                    passwordTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                            .getResource("styles/text-field-error-style.css")).toExternalForm());
                }
                if(!exception.getMessage().toLowerCase(Locale.ROOT).contains("email") ||
                        !exception.getMessage().toLowerCase(Locale.ROOT).contains("password")){
                    errorLabel.setText(exception.getMessage().replace("Login failed!", ""));
                    passwordTextField.getStylesheets().clear();
                    passwordTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                            .getResource("styles/text-field-error-style.css")).toExternalForm());
                    mailTextField.getStylesheets().clear();
                    mailTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                            .getResource("styles/text-field-error-style.css")).toExternalForm());
                }
            }

        } else {
            errorLabel.setText("Login failed!");
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
