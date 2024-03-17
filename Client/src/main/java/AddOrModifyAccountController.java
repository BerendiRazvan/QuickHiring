import domain.ImageData;
import domain.Resume;
import domain.User;
import enums.AccountType;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import validators.ResumeValidator;

import java.io.*;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.ResourceBundle;

public class AddOrModifyAccountController extends UnicastRemoteObject implements Initializable, IObserver, Serializable {

    private IService server;
    private Parent parentRoot;
    private Stage stage;

    private HomeController homeController;
    private LoginController loginController;

    private User loggedUser;
    private Resume resumeForUser;

    private Image imageProfile;
    private ImageData imageDataProfile;


    // FXML Components

    @FXML
    Button backButton;

    @FXML
    Button uploadResumeButton;

    @FXML
    Button uploadImageButton;

    @FXML
    Button createModifyAccountButton;

    @FXML
    Label titlePageLabel;

    @FXML
    Label firstNameLabel;

    @FXML
    Label lastNameLabel;

    @FXML
    Label birthDateLabel;

    @FXML
    Label mailLabel;

    @FXML
    Label mailConfirmationLabel;

    @FXML
    Label passwordLabel;

    @FXML
    Label passwordConfirmationLabel;

    @FXML
    Label phoneNumberLabel;

    @FXML
    Label errorFNLabel;

    @FXML
    Label errorLNLabel;

    @FXML
    Label errorBDLabel;

    @FXML
    Label errorMLabel;

    @FXML
    Label errorMCLabel;

    @FXML
    Label errorPLabel;

    @FXML
    Label errorPCLabel;

    @FXML
    Label errorPNoLabel;

    @FXML
    Label errorATLabel;

    @FXML
    Label errorRLabel;

    @FXML
    Label errorImgLabel;

    @FXML
    Label errorActionLabel;

    @FXML
    TextField firstNameTextField;

    @FXML
    TextField lastNameTextField;

    @FXML
    TextField mailTextField;

    @FXML
    TextField mailCTextField;

    @FXML
    PasswordField passwordTextField;

    @FXML
    PasswordField passwordCTextField;

    @FXML
    TextField phoneNumberTextField;

    @FXML
    ComboBox<AccountType> accountTypeBox;

    @FXML
    DatePicker birthDatePicker;

    @FXML
    ImageView imageProfileView;


    public AddOrModifyAccountController() throws RemoteException {
    }

    public void setServerAndStage(IService s, User user, Stage primaryStage, Parent root) {
        this.server = s;
        this.stage = primaryStage;
        this.parentRoot = root;
        this.loggedUser = user;

        initPageData();
    }


    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        phoneNumberTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                phoneNumberTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void initPageData() {
        accountTypeBox.setItems(FXCollections.observableArrayList(AccountType.values()));

        accountTypeBox.valueProperty().addListener(observable -> {
            if (accountTypeBox.getValue() == AccountType.CANDIDATE)
                uploadResumeButton.setVisible(true);
            else
                uploadResumeButton.setVisible(false);
        });

        clearErrorMsg();

        if (loggedUser == null)
            initPageDataForCreateAcc();
        else
            initPageDataForModifyAcc();
    }

    private void clearErrorMsg() {
        errorFNLabel.setText("");
        errorLNLabel.setText("");
        errorBDLabel.setText("");
        errorMLabel.setText("");
        errorMCLabel.setText("");
        errorPLabel.setText("");
        errorPCLabel.setText("");
        errorPNoLabel.setText("");
        errorATLabel.setText("");
        errorRLabel.setText("");
        errorImgLabel.setText("");
        errorActionLabel.setText("");
    }

    private void initPageDataForCreateAcc() {
        titlePageLabel.setText("Create account");

        createModifyAccountButton.setText("Create account");

        accountTypeBox.setVisible(true);

        birthDatePicker.setVisible(true);
        birthDateLabel.setVisible(true);


        uploadResumeButton.setVisible(false);

        mailTextField.setDisable(false);
        mailConfirmationLabel.setVisible(true);
        mailCTextField.setVisible(true);
        errorMCLabel.setVisible(true);

        // set textFields to empty string
        clearFields();
        birthDatePicker.setValue(LocalDate.now());

        // add photo default
        imageProfile = getProfileImage();
        imageProfileView.setImage(imageProfile);


        firstNameTextField.textProperty().addListener(o -> {
            errorFNLabel.setText("");
            errorActionLabel.setText("");
            firstNameTextField.getStylesheets().clear();
            firstNameTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-style.css")).toExternalForm());
        });

        lastNameTextField.textProperty().addListener(o -> {
            errorLNLabel.setText("");
            errorActionLabel.setText("");
            lastNameTextField.getStylesheets().clear();
            lastNameTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-style.css")).toExternalForm());
        });

        mailTextField.textProperty().addListener(o -> {
            errorMLabel.setText("");
            errorActionLabel.setText("");
            mailTextField.getStylesheets().clear();
            mailTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-style.css")).toExternalForm());
        });

        mailCTextField.textProperty().addListener(o -> {
            errorMCLabel.setText("");
            errorActionLabel.setText("");
            mailCTextField.getStylesheets().clear();
            mailCTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-style.css")).toExternalForm());
        });

        phoneNumberTextField.textProperty().addListener(o -> {
            errorPNoLabel.setText("");
            errorActionLabel.setText("");
            phoneNumberTextField.getStylesheets().clear();
            phoneNumberTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-style.css")).toExternalForm());
        });

        birthDatePicker.valueProperty().addListener(o -> {
            errorBDLabel.setText("");
            errorActionLabel.setText("");
            birthDatePicker.getStylesheets().clear();
            birthDatePicker.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/date-picker-style.css")).toExternalForm());
        });

        passwordTextField.textProperty().addListener(o -> {
            errorPLabel.setText("");
            errorActionLabel.setText("");
            passwordTextField.getStylesheets().clear();
            passwordTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-style.css")).toExternalForm());
        });

        passwordCTextField.textProperty().addListener(o -> {
            errorPCLabel.setText("");
            errorActionLabel.setText("");
            passwordCTextField.getStylesheets().clear();
            passwordCTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-style.css")).toExternalForm());
        });

        accountTypeBox.valueProperty().addListener(o -> {
            errorATLabel.setText("");
            errorActionLabel.setText("");
            accountTypeBox.getStylesheets().clear();
            accountTypeBox.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/combo-box-style.css")).toExternalForm());
        });

    }

    private void initPageDataForModifyAcc() {
        titlePageLabel.setText("Manage account");

        createModifyAccountButton.setText("Modify account");

        accountTypeBox.setVisible(false);

        birthDatePicker.setVisible(false);
        birthDateLabel.setVisible(false);

        uploadResumeButton.setVisible(false);
        if (loggedUser.getAccountType() == AccountType.CANDIDATE) uploadResumeButton.setVisible(true);

        mailTextField.setDisable(true);
        mailConfirmationLabel.setVisible(false);
        mailCTextField.setVisible(false);
        errorMCLabel.setVisible(false);

        accountTypeBox.setValue(loggedUser.getAccountType());

        // upload user data in text filed
        setupFields();
        birthDatePicker.setValue(loggedUser.getBirthDate().toLocalDate());

        // add user photo
        imageProfile = getProfileImage();
        imageProfileView.setImage(imageProfile);

        firstNameTextField.textProperty().addListener(o -> {
            errorFNLabel.setText("");
            errorActionLabel.setText("");
            firstNameTextField.getStylesheets().clear();
            firstNameTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-style.css")).toExternalForm());
        });

        lastNameTextField.textProperty().addListener(o -> {
            errorLNLabel.setText("");
            errorActionLabel.setText("");
            lastNameTextField.getStylesheets().clear();
            lastNameTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-style.css")).toExternalForm());
        });

        mailTextField.textProperty().addListener(o -> {
            errorMLabel.setText("");
            errorActionLabel.setText("");
            mailTextField.getStylesheets().clear();
            mailTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-style.css")).toExternalForm());
        });


        phoneNumberTextField.textProperty().addListener(o -> {
            errorPNoLabel.setText("");
            errorActionLabel.setText("");
            phoneNumberTextField.getStylesheets().clear();
            phoneNumberTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-style.css")).toExternalForm());
        });

        passwordTextField.textProperty().addListener(o -> {
            errorPLabel.setText("");
            errorActionLabel.setText("");
            passwordTextField.getStylesheets().clear();
            passwordTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-style.css")).toExternalForm());
        });

        passwordCTextField.textProperty().addListener(o -> {
            errorPCLabel.setText("");
            errorActionLabel.setText("");
            passwordCTextField.getStylesheets().clear();
            passwordCTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-style.css")).toExternalForm());
        });

    }

    private Image getProfileImage() {
        ImageData dataImageBD = null;
        if (loggedUser != null) {
            dataImageBD = loggedUser.getProfileImage();
        }
        if (dataImageBD == null)
            dataImageBD = server.getProfileImage(1L);

        imageDataProfile = dataImageBD;

        int width = dataImageBD.getWidth();
        int height = dataImageBD.getHeight();

        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        pixelWriter.setPixels(0, 0, width, height, PixelFormat.getByteBgraInstance(), dataImageBD.getImageData(), 0, height * 4);

        return writableImage;
    }


    private void clearFields() {
        firstNameTextField.setText("");
        lastNameTextField.setText("");
        mailTextField.setText("");
        mailCTextField.setText("");
        passwordTextField.setText("");
        passwordCTextField.setText("");
        phoneNumberTextField.setText("");
    }

    private void setupFields() {
        firstNameTextField.setText(loggedUser.getFirstName());
        lastNameTextField.setText(loggedUser.getLastName());
        mailTextField.setText(loggedUser.getMail());
        mailCTextField.setText(loggedUser.getMail());
        passwordTextField.setText(loggedUser.getPassword());
        passwordCTextField.setText(loggedUser.getPassword());
        phoneNumberTextField.setText(loggedUser.getPhoneNumber());
    }

    @FXML
    protected void onActionBack(ActionEvent event) {
        if (loggedUser != null) {
            try {
                setHomeController();
                openHomeView(loggedUser, event);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else {
            try {
                setLoginController();
                openLoginView(event);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }


    @FXML
    protected void onActionCreateModifyAccount(ActionEvent event) {
        boolean errors = false;
        if (firstNameTextField.getText().isEmpty()) {
            errorFNLabel.setText("First name is empty");
            errors = true;
            firstNameTextField.getStylesheets().clear();
            firstNameTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-error-style.css")).toExternalForm());
        }
        if (lastNameTextField.getText().isEmpty()) {
            errorLNLabel.setText("Last name is empty");
            errors = true;
            lastNameTextField.getStylesheets().clear();
            lastNameTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-error-style.css")).toExternalForm());
        }
        if (mailTextField.getText().isEmpty()) {
            errorMLabel.setText("Email is empty");
            errors = true;
            mailTextField.getStylesheets().clear();
            mailTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-error-style.css")).toExternalForm());
        }
        if (mailCTextField.getText().isEmpty() && loggedUser == null) {
            errorMCLabel.setText("Email confirmation is empty");
            errors = true;
            mailCTextField.getStylesheets().clear();
            mailCTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-error-style.css")).toExternalForm());
        }
        if (!mailTextField.getText().equals(mailCTextField.getText()) && loggedUser == null) {
            errorMCLabel.setText("Different email");
            errors = true;
            mailCTextField.getStylesheets().clear();
            mailCTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-error-style.css")).toExternalForm());
        }
        if (passwordTextField.getText().isEmpty()) {
            errorPLabel.setText("Password is empty");
            errors = true;
            passwordTextField.getStylesheets().clear();
            passwordTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-error-style.css")).toExternalForm());
        }
        if (passwordCTextField.getText().isEmpty()) {
            errorPCLabel.setText("Password confirmation is empty");
            errors = true;
            passwordCTextField.getStylesheets().clear();
            passwordCTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-error-style.css")).toExternalForm());
        }
        if (!passwordTextField.getText().equals(passwordCTextField.getText())) {
            errorPCLabel.setText("Different password");
            errors = true;
            passwordCTextField.getStylesheets().clear();
            passwordCTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-error-style.css")).toExternalForm());
        }
        if (phoneNumberTextField.getText().isEmpty()) {
            errorPNoLabel.setText("Phone number is empty");
            errors = true;
            phoneNumberTextField.getStylesheets().clear();
            phoneNumberTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/text-field-error-style.css")).toExternalForm());
        }
        if (accountTypeBox.getValue() == null) {
            errorATLabel.setText("Account type is not selected");
            errors = true;
            accountTypeBox.getStylesheets().clear();
            accountTypeBox.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("styles/combo-box-error-style.css")).toExternalForm());
        } else {
            if (loggedUser == null) {
                if (accountTypeBox.getValue() == AccountType.CANDIDATE && resumeForUser == null) {
                    errorRLabel.setText("Resume is not uploaded");
                    errors = true;
                    uploadResumeButton.getStylesheets().clear();
                    uploadResumeButton.getStylesheets().add(Objects.requireNonNull(getClass()
                            .getResource("styles/button-error-style.css")).toExternalForm());
                }
            }
        }

        if (!errors) {
            User user = new User();
            user.setFirstName(firstNameTextField.getText());
            user.setLastName(lastNameTextField.getText());
            user.setMail(mailTextField.getText());
            user.setPassword(passwordTextField.getText());
            user.setAccountType(accountTypeBox.getValue());
            user.setPhoneNumber(phoneNumberTextField.getText());
            user.setBirthDate(LocalDateTime.of(
                    birthDatePicker.getValue().getYear(),
                    birthDatePicker.getValue().getMonthValue(),
                    birthDatePicker.getValue().getDayOfMonth(), 0, 0));

            // set img for user
            if (imageDataProfile != null)
                user.setProfileImage(imageDataProfile);
            else
                user.setProfileImage(server.getProfileImage(1L));

            try {
                if (loggedUser == null) {
                    //create
                    loggedUser = server.createAccount(user);
                    if (user.getAccountType() == AccountType.CANDIDATE) {
                        resumeForUser.setOwner(loggedUser);
                        server.addResume(resumeForUser);
                    }
                    //login
                    clearErrorMsg();
                    setHomeController();
                    User currentUser = server.login(loggedUser, homeController);
                    openHomeView(currentUser, event);
                } else {
                    //modify
                    loggedUser = server.modifyAccount(loggedUser.getId(), user);
                    //update resume
                    if (user.getAccountType() == AccountType.CANDIDATE && resumeForUser != null) {
                        resumeForUser.setOwner(loggedUser);
                        server.addResume(resumeForUser);
                    }
                    clearErrorMsg();
                    setupFields();

                    //go back to home page
                    try {
                        setHomeController();
                        openHomeView(loggedUser, event);
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
            } catch (ServiceException | IOException e) {
                errorActionLabel.setText("Invalid account data!");
                setErrors(e.getMessage());
            }
        }
    }

    private void setErrors(String m) {
        boolean ok = true;

        String[] errors = m.split("\n");
        for (String message : errors) {
            message = message.replace("!", "!\n");
            message = message.replace(";", "\n");

            if (message.toLowerCase().contains("email")) {
                ok = false;
                errorMLabel.setText(message);
                mailTextField.getStylesheets().clear();
                mailTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                        .getResource("styles/text-field-error-style.css")).toExternalForm());
            }
            if (message.toLowerCase().contains("password")) {
                ok = false;
                errorPLabel.setText(message);
                passwordTextField.getStylesheets().clear();
                passwordTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                        .getResource("styles/text-field-error-style.css")).toExternalForm());
            }
            if (message.toLowerCase().contains("first name")) {
                ok = false;
                errorFNLabel.setText(message);
                firstNameTextField.getStylesheets().clear();
                firstNameTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                        .getResource("styles/text-field-error-style.css")).toExternalForm());
            }
            if (message.toLowerCase().contains("last name")) {
                ok = false;
                errorLNLabel.setText(message);
                lastNameTextField.getStylesheets().clear();
                lastNameTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                        .getResource("styles/text-field-error-style.css")).toExternalForm());
            }
            if (message.toLowerCase().contains("birth date")) {
                ok = false;
                errorBDLabel.setText(message);
                birthDatePicker.getStylesheets().clear();
                birthDatePicker.getStylesheets().add(Objects.requireNonNull(getClass()
                        .getResource("styles/date-picker-error-style.css")).toExternalForm());
            }
            if (message.toLowerCase().contains("phone number")) {
                ok = false;
                errorPNoLabel.setText(message);
                phoneNumberTextField.getStylesheets().clear();
                phoneNumberTextField.getStylesheets().add(Objects.requireNonNull(getClass()
                        .getResource("styles/text-field-error-style.css")).toExternalForm());
            }
            if (message.toLowerCase().contains("resume")) {
                ok = false;
                errorRLabel.setText(message);
                uploadResumeButton.getStylesheets().clear();
                uploadResumeButton.getStylesheets().add(Objects.requireNonNull(getClass()
                        .getResource("styles/button-error-style.css")).toExternalForm());
            }

            if (ok)
                errorActionLabel.setText(message);
        }
    }

    @FXML
    protected void onActionSelectResume(ActionEvent event) {
        errorRLabel.setText("");
        errorActionLabel.setText("");
        uploadResumeButton.getStylesheets().clear();
        uploadResumeButton.getStylesheets().add(Objects.requireNonNull(getClass()
                .getResource("styles/button-style.css")).toExternalForm());

        FileChooser fileChooser = new FileChooser();

        // Set extension filter
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("Resume types accepted: ", "*.txt", "*.pdf", "*.doc", "*.docx");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show open file dialog
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            System.out.println(file.getName() + "\n" + file.getAbsolutePath());
            uploadResumeButton.setText(file.getName() + " - uploaded");
            // set to green
            try {
                resumeForUser = server.analyzeResume(file);
                ResumeValidator resumeValidator = new ResumeValidator();
                resumeValidator.validateInfoExtracted(resumeForUser);
            } catch (Exception e) {
                errorRLabel.setText("Invalid resume!\n");
                uploadResumeButton.getStylesheets().clear();
                uploadResumeButton.getStylesheets().add(Objects.requireNonNull(getClass()
                        .getResource("styles/button-error-style.css")).toExternalForm());

            }
            // autofill fields from extracted data if you create an account (create)
        }
    }

    @FXML
    protected void onActionSelectPhoto(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();

        // Set extension filter
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("Image types accepted: ", "*.png", "*.jpg", "*.jpeg", "*.gif");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show open file dialog
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            imageProfile = new Image(file.toURI().toString());
            imageProfileView.setImage(imageProfile);

            int width = (int) imageProfile.getWidth();
            int height = (int) imageProfile.getHeight();
            WritableImage writableImage = new WritableImage(width, height);
            PixelReader pixelReader = imageProfile.getPixelReader();
            PixelWriter pixelWriter = writableImage.getPixelWriter();
            pixelWriter.setPixels(0, 0, width, height, pixelReader, 0, 0);

            byte[] imageData = new byte[width * height * 4];
            pixelReader.getPixels(0, 0, width, height, PixelFormat.getByteBgraInstance(), imageData, 0, width * 4);


            imageDataProfile = new ImageData();
            imageDataProfile.setImageData(imageData);
            imageDataProfile.setHeight(height);
            imageDataProfile.setWidth(width);
            imageDataProfile.setType(file.getPath().substring(file.getPath().indexOf(".")));

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

        stage.setTitle("QuickHiring - Home - " + currentUser.getMail());
    }

    private void setLoginController() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("views/login-view.fxml"));
        parentRoot = fxmlLoader.load();
        loginController = fxmlLoader.<LoginController>getController();
    }

    private void openLoginView(ActionEvent actionEvent) {
        loginController.setServerAndStage(server, stage, parentRoot);

        stage.setScene(new Scene(parentRoot));

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
            }
        });

        stage.setTitle("QuickHiring - Login");
    }
}