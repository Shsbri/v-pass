package com.mycompany.tes;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;
    private static String currentRoot = "Register";

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("Register"), 1280, 720);
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        currentRoot = fxml;
        scene.setRoot(loadFXML(fxml));
    }

    public static String getCurrentRoot() {
        return currentRoot;
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}