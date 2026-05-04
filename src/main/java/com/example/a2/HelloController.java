package com.example.a2;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for the sample {@code hello-view.fxml} screen (template JavaFX project).
 * Updates a label when the demo button is pressed.
 *
 * @author Abanoub
 * @version 1.0
 * @see javafx.scene.control.Label
 */
public class HelloController {

    /** Label bound in FXML; displays welcome text (no dedicated CSS style class in template). */
    @FXML
    private Label welcomeText;

    /**
     * [FXML] Handles the hello button action and sets the welcome label text.
     *
     * @return nothing
     */
    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
