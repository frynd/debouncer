package com.frynd.debouncer.examples.editor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Extremely simple HTML editor to demonstrate showing loading state while user is typing.
 *
 * @see SimpleEditorDemoController
 */
public class SimpleEditorDemo extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(SimpleEditorDemo.class.getResource("/editor/editor.fxml"));
        primaryStage.setTitle("Simple Editor");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
