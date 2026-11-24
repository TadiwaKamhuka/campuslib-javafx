package com.team.campuslib;

import java.util.Objects;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.team.campuslib.util.DatabaseManager;
import com.team.campuslib.util.SeedDataInitializer;

public class App extends Application {

    private DatabaseManager databaseManager;
	
    @Override
    public void start(Stage stage) throws Exception {
        
	databaseManager = DatabaseManager.getInstance();	
        SeedDataInitializer.seedIfEmpty();

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
	
    @Override
    public void stop() {
    	if (databaseManager != null) {
       	 	databaseManager.shutdown();
         }	
     }

    public static void main(String[] args) {
        launch(args);
    }
}
