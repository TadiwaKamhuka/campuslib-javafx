package com.team.campuslib.controller;

import java.io.IOException;
import java.util.Objects;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void initialize() {
        usernameField.textProperty().addListener(
                (observable, oldValue, newValue) -> hideError()
        );

        passwordField.textProperty().addListener(
                (observable, oldValue, newValue) -> hideError()
        );
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            showError("Please enter your username and password.");
            return;
        }

        // TODO: Replace temporary demo checks with AuthService.login()
        // after backend integration.
        String role;

        if ("admin".equals(username) && "admin123".equals(password)) {
            role = "ADMIN";
        } else if ("student".equals(username)
                && "student123".equals(password)) {
            role = "STUDENT";
        } else {
            passwordField.clear();
            showError("Invalid username or password.");
            passwordField.requestFocus();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(
                            getClass().getResource(
                                    "/com/team/campuslib/view/MainView.fxml"),
                            "MainView.fxml not found"
                    )
            );

            Parent mainRoot = loader.load();

            MainController controller = loader.getController();
            controller.configureDemoUser(username, role);

            Scene scene = usernameField.getScene();

            passwordField.clear();
            scene.setRoot(mainRoot);

        } catch (IOException | RuntimeException exception) {
            exception.printStackTrace();
            showError("Unable to open CampusLib. Please try again.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}