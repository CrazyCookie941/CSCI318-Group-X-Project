package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// Displays the account registration form with the team's existing styling.
public class SignUpUI extends Application {

    @Override
    public void start(Stage stage) {
        // 1. Root Layout Backdrop Container
        StackPane rootLayout = new StackPane();
        rootLayout.setStyle("-fx-background-color: #fafafa;");
        rootLayout.setPadding(new Insets(30));

        // 2. Centralized Registration Form Card Panel
        VBox cardContainer = new VBox(22);
        cardContainer.setAlignment(Pos.TOP_LEFT);
        cardContainer.setMaxSize(380, 520);
        cardContainer.setPadding(new Insets(35, 40, 0, 40));
        cardContainer.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 4);");

        VBox headersBlock = new VBox(4);
        headersBlock.setAlignment(Pos.TOP_LEFT);

        Label welcome = new Label("Welcome");
        welcome.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        Label title = new Label("Create an account to get started");
        title.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-padding: 0 0 5 0;");
        headersBlock.getChildren().addAll(welcome, title);

        TextField nameField = new TextField();
        nameField.setPromptText("Username");
        nameField.setPrefHeight(38);
        nameField.setMaxWidth(Double.MAX_VALUE);
        nameField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 12; -fx-font-size: 14px;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email address");
        emailField.setPrefHeight(38);
        emailField.setMaxWidth(Double.MAX_VALUE);
        emailField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 12; -fx-font-size: 14px;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(38);
        passwordField.setMaxWidth(Double.MAX_VALUE);
        passwordField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 12; -fx-font-size: 14px;");

        ComboBox<String> accountTypeBox = new ComboBox<>();
        accountTypeBox.getItems().addAll("Event goer", "Event manager");
        accountTypeBox.setValue("Event goer");
        accountTypeBox.setPrefHeight(38);
        accountTypeBox.setMaxWidth(Double.MAX_VALUE);

        Button signUpButton = new Button("Sign Up");
        signUpButton.setPrefHeight(40);
        signUpButton.setMaxWidth(Double.MAX_VALUE);
        signUpButton.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");
        signUpButton.setOnMouseEntered(e -> signUpButton.setStyle("-fx-background-color: #005fa3; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;"));
        signUpButton.setOnMouseExited(e -> signUpButton.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;"));

        Label message = new Label();
        message.setWrapText(true);
        message.setStyle("-fx-text-fill: #d00000; -fx-font-size: 13px; -fx-font-weight: bold;");

        signUpButton.setOnAction(event -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();

            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                message.setText("Please fill in all fields.");
                return;
            }

            String role = "Event manager".equals(accountTypeBox.getValue())
                    ? "MANAGER" : "GOER";

            signUpButton.setDisable(true);
            message.setText("Creating account...");

            AccountAuthApi.signup(name, email, password, role).whenComplete((result, error) ->
                    Platform.runLater(() -> {
                        signUpButton.setDisable(false);

                        if (error != null) {
                            message.setText("Cannot connect to the account service. Please ensure the backend is running on port 8083.");
                            return;
                        }

                        if (result == null) {
                            message.setText("The account service returned an empty response.");
                            return;
                        }

                        if (!result.success()) {
                            message.setText(result.message());
                            return;
                        }

                        if (result.accountId() == null || result.username() == null
                                || result.email() == null
                                || !("GOER".equals(result.role()) || "MANAGER".equals(result.role()))) {
                            message.setText("The account service returned incomplete signup data.");
                            return;
                        }

                        // Preserve the existing UX: go to Dashboard after signup.
                        Session.logout();
                        CurrentUserSession.login(
                                result.accountId(),
                                result.username(),
                                result.email(),
                                result.role()
                        );
                        message.setText("Account created. Opening dashboard...");
                        try {
                            Navigation.goTo(stage, nextStage -> new DashboardUI().start(nextStage));
                        } catch (RuntimeException exception) {
                            message.setText("Your account was created, but the dashboard could not be opened. "
                                    + "Please sign in to continue.");
                            System.err.println("Could not open dashboard after signup: "
                                    + exception.getMessage());
                        }
                    })
            );
        });

        Label accountLabel = new Label("Already have an account? ");
        accountLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 13px;");

        Hyperlink accountLink = new Hyperlink("Sign In");
        accountLink.setStyle("-fx-text-fill: #007bc4; -fx-font-size: 13px; -fx-underline: false; -fx-padding: 0; -fx-font-weight: bold;");
        accountLink.setOnAction(event -> Navigation.goTo(stage, nextStage -> new LoginUI().start(nextStage)));

        HBox accountRow = new HBox(accountLabel, accountLink);
        accountRow.setAlignment(Pos.CENTER_LEFT);

        cardContainer.getChildren().addAll(
                headersBlock,
                nameField,
                emailField,
                passwordField,
                accountTypeBox,
                signUpButton,
                accountRow,
                message
        );

        rootLayout.getChildren().add(cardContainer);
        stage.setTitle("Create Account");
        stage.setScene(new Scene(rootLayout, 1366, 768));
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
