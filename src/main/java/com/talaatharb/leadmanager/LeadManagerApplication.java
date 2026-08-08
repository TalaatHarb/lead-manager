package com.talaatharb.leadmanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Main JavaFX application entry point for Lead Manager.
 */
public class LeadManagerApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(LeadManagerApplication.class);

    @Override
    public void start(Stage primaryStage) throws IOException {
        log.info("Starting Lead Manager application");

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/talaatharb/leadmanager/fxml/main.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 800);
        scene.getStylesheets().add(
                getClass().getResource("/com/talaatharb/leadmanager/css/style.css").toExternalForm());

        primaryStage.setTitle("Lead Manager");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
