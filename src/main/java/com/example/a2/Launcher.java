package com.example.a2;

import javafx.application.Application;

/**
 * Application entry point that delegates to {@link HelloApplication} for JavaFX lifecycle management.
 *
 * @author Abanoub
 * @version 1.0
 * @see HelloApplication
 */
public class Launcher {

    /**
     * Starts the JavaFX runtime and launches {@link HelloApplication}.
     *
     * @param args command-line arguments forwarded to JavaFX
     * @return nothing
     */
    public static void main(String[] args) {
        Application.launch(HelloApplication.class, args);
    }
}
