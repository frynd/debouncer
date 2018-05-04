package com.frynd.debouncer.examples.monitor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Simple demonstration of a system that monitors many event producers.
 *
 * @see MonitorDemoController
 * @see EventProducer
 */
public class MonitorDemo extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(MonitorDemo.class.getResource("/monitor/monitor.fxml"));
        primaryStage.setTitle("Monitor Demo");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
