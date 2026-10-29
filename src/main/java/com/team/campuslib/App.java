package com.team.campuslib;

import java.util.Objects;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(
                        App.class.getResource(
                                "/com/team/campuslib/view/LoginView.fxml"),
                        "LoginView.fxml not found"
                )
        );

        Parent root = loader.load();

        Scene scene = new Scene(root, 1180, 760);

        scene.getStylesheets().add(
                Objects.requireNonNull(
                        App.class.getResource(
                                "/com/team/campuslib/css/style.css"),
                        "style.css not found"
                ).toExternalForm()
        );

        stage.setTitle("CampusLib");
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}