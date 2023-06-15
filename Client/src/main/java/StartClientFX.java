import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.Properties;

public class StartClientFX extends Application {

    private static int defaultTravelPart = 55555;
    private static String defaultServer = "localhost";

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("In start");
        Properties clientProps = new Properties();
        try {
            clientProps.load(StartClientFX.class.getResourceAsStream("/quickhiringclient.properties"));
            System.out.println("Client properties set.");
            clientProps.list(System.out);
        } catch (IOException e) {
            System.err.println("Cannot find quickhiringclient.properties " + e);
            return;
        }


        String serverIP = clientProps.getProperty("quickhiring.server.host", defaultServer);
        int serverPort = defaultTravelPart;

        try {
            serverPort = Integer.parseInt(clientProps.getProperty("quickhiring.server.port"));
        } catch (NumberFormatException ex) {
            System.err.println("Wrong port number " + ex.getMessage());
            System.out.println("Using default port: " + defaultTravelPart);
        }

        System.out.println("Using server IP " + serverIP);
        System.out.println("Using server port " + serverPort);

        ApplicationContext factory = new ClassPathXmlApplicationContext("classpath:spring-client.xml");
        IService server = (IService) factory.getBean("service");

        System.out.println(getClass().getClassLoader().getResource("views/login-view.fxml"));

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("views/login-view.fxml"));
        Parent root = fxmlLoader.load();

        LoginController loginController = fxmlLoader.getController();
        loginController.setServerAndStage(server, primaryStage, root);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
            }
        });

        primaryStage.setScene(new Scene(root));
        primaryStage.setMinWidth(950);
        primaryStage.setMinHeight(550);
        primaryStage.setTitle("QuickHiring - Login");
        primaryStage.getIcons().add(new Image(StartClientFX.class.getResource("images/appLogo.jpg").toExternalForm()));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
