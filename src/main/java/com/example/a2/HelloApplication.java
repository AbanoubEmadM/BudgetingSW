package com.example.a2;

import com.example.a2.util.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Primary JavaFX {@link Application} for the personal budgeting system.
 * Initializes SQLite via {@link DatabaseManager} and loads the login FXML scene.
 *
 * @author Abanoub
 * @version 1.0
 * @see com.example.a2.ui.LoginController
 * @see DatabaseManager
 */
public class HelloApplication extends Application {

    /**
     * Constructs the initial login scene.
     * Loads {@code login.fxml}, applies scene graph styling from FXML, and shows the primary stage.
     *
     * @param stage primary window provided by the JavaFX runtime
     * @return nothing
     * @throws Exception if FXML loading or database initialization fails
     */
    @Override
    public void start(Stage stage) throws Exception {
        DatabaseManager.getInstance();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/a2/fxml/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 500, 400);
        stage.setTitle("Personal Budgeting System - Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Releases the singleton database connection on JVM shutdown of the JavaFX application.
     *
     * @return nothing
     */
    @Override
    public void stop() {
        DatabaseManager.getInstance().close();
    }

    /**
     * Convenience launcher when this class is run directly (IDE or {@code java}).
     *
     * @param args JavaFX application arguments
     * @return nothing
     */
    public static void main(String[] args) {
        launch(args);
    }
}
