package com.team.campuslib.controller;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    private Label accountLabel;

    @FXML
    private Label navigationErrorLabel;

    @FXML
    private Button dashboardButton;

    @FXML
    private Button booksButton;

    @FXML
    private Button membersButton;

    @FXML
    private Button loansButton;

    private String demoUsername;
    private String demoRole;

    public void configureDemoUser(String username, String role)
            throws IOException {

        if (!"ADMIN".equals(role) && !"STUDENT".equals(role)) {
            throw new IllegalArgumentException("Unsupported demo role");
        }

        // TODO: Replace temporary demo identity with the approved
        // current-user contract after backend integration.
        demoUsername = username;
        demoRole = role;

        boolean isAdmin = "ADMIN".equals(demoRole);

        accountLabel.setText(demoUsername + " · " + demoRole);

        membersButton.setVisible(isAdmin);
        membersButton.setManaged(isAdmin);

        loansButton.setText(isAdmin ? "Loans" : "My Loans");

        loadPage("DashboardView.fxml", dashboardButton);
    }

    @FXML
    private void handleDashboard() {
        navigate("DashboardView.fxml", dashboardButton);
    }

    @FXML
    private void handleBooks() {
        navigate("BookView.fxml", booksButton);
    }

    @FXML
    private void handleMembers() {
        if ("ADMIN".equals(demoRole)) {
            navigate("MemberView.fxml", membersButton);
        }
    }

    @FXML
    private void handleLoans() {
        navigate("LoanView.fxml", loansButton);
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(
                            getClass().getResource(
                                    "/com/team/campuslib/view/LoginView.fxml"),
                            "LoginView.fxml not found"
                    )
            );

            Parent loginRoot = loader.load();

            contentArea.getScene().setRoot(loginRoot);

            // TODO: Replace temporary demo logout with
            // AuthService.logout() after backend integration.
            demoUsername = null;
            demoRole = null;

        } catch (IOException | RuntimeException exception) {
            showNavigationError(exception);
        }
    }

    private void navigate(String filename, Button selectedButton) {
        try {
            loadPage(filename, selectedButton);
        } catch (IOException | RuntimeException exception) {
            showNavigationError(exception);
        }
    }

    private void loadPage(String filename, Button selectedButton)
            throws IOException {

        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(
                        getClass().getResource(
                                "/com/team/campuslib/view/" + filename),
                        filename + " not found"
                )
        );

        Parent page = loader.load();

        if ("DashboardView.fxml".equals(filename)) {
            Label dashboardTitle =
                    (Label) loader.getNamespace().get("dashboardTitle");

            Label welcomeLabel =
                    (Label) loader.getNamespace().get("welcomeLabel");

            dashboardTitle.setText(
                    "ADMIN".equals(demoRole)
                            ? "Admin Dashboard"
                            : "Student Dashboard"
            );

            welcomeLabel.setText("Welcome back, " + demoUsername);
        }

        contentArea.getChildren().setAll(page);

        for (Button button : List.of(
                dashboardButton,
                booksButton,
                membersButton,
                loansButton)) {

            button.getStyleClass().remove("nav-button-selected");
        }

        selectedButton.getStyleClass().add("nav-button-selected");

        navigationErrorLabel.setText("");
        navigationErrorLabel.setVisible(false);
        navigationErrorLabel.setManaged(false);
    }

    private void showNavigationError(Exception exception) {
        exception.printStackTrace();

        navigationErrorLabel.setText(
                "Unable to open this page. Please try again."
        );

        navigationErrorLabel.setVisible(true);
        navigationErrorLabel.setManaged(true);
    }
}