
package com.team.campuslib;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/com/team/campuslib/view/LoginView.fxml")
        );

        Scene scene = new Scene(loader.load(), 1000, 650);
        scene.getStylesheets().add(
                App.class.getResource("/com/team/campuslib/css/style.css")
                        .toExternalForm()
        );

        stage.setTitle("CampusLib — Library Management System");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
