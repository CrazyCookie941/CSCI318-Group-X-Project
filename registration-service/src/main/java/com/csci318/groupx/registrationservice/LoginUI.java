package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// Displays the sign-in form using the account authentication API.
public class LoginUI extends Application {

    @Override
    public void start(Stage stage) {
        // 1. Root Layout Backdrop Container
        StackPane rootLayout = new StackPane();
        rootLayout.setStyle("-fx-background-color: #fafafa;");
        rootLayout.setPadding(new Insets(30));

        // 2. Centralized Form Presentation Card Panel
        VBox cardContainer = new VBox(22);
        cardContainer.setAlignment(Pos.TOP_LEFT);
        cardContainer.setMaxSize(360, 410);
        cardContainer.setPadding(new Insets(35, 40, 35, 40));
        cardContainer.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 4);");

        Label title = new Label("Sign In");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a; -fx-padding: 0 0 5 0;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email or username");
        emailField.setPrefHeight(38);
        emailField.setMaxWidth(Double.MAX_VALUE);
        emailField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 12; -fx-font-size: 14px;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(38);
        passwordField.setMaxWidth(Double.MAX_VALUE);
        passwordField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 12; -fx-font-size: 14px;");

        Label message = new Label();
        message.setWrapText(true);
        message.setStyle("-fx-text-fill: #d00000; -fx-font-size: 13px; -fx-font-weight: bold;");

        // Kept for visual consistency. Persistent 'remember me' is not implemented.
        CheckBox rememberMe = new CheckBox("Remember me");
        rememberMe.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555; -fx-cursor: hand;");

        Button loginButton = new Button("Sign In");
        loginButton.setPrefHeight(40);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");
        loginButton.setOnMouseEntered(e -> loginButton.setStyle("-fx-background-color: #005fa3; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;"));

        loginButton.setOnAction(event -> {
            String identifier = emailField.getText().trim();
            String password = passwordField.getText();

            if (identifier.isBlank() || password.isBlank()) {
                message.setText("Please enter both email/username and password.");
                return;
            }

            loginButton.setDisable(true);
            message.setText("Signing in...");

            AccountAuthApi.login(identifier, password).whenComplete((result, error) ->
                    Platform.runLater(() -> {
                        loginButton.setDisable(false);

                        if (error != null) {
                            message.setText("Cannot connect to the account service. Please ensure the backend is running on port 8083.");
                            return;
                        }

                        if (!result.success()) {
                            message.setText(result.message());
                            return;
                        }

                        if (result.accountId() == null || result.username() == null
                                || result.email() == null
                                || !("GOER".equals(result.role()) || "MANAGER".equals(result.role()))) {
                            message.setText("The account service returned incomplete login data.");
                            return;
                        }

                        Session.logout();
                        CurrentUserSession.login(
                                result.accountId(),
                                result.username(),
                                result.email(),
                                result.role()
                        );
                        Navigation.goTo(stage, nextStage -> new DashboardUI().start(nextStage));
                    })
            );
        });

        Hyperlink createAccount = new Hyperlink("Create an account");
        createAccount.setStyle("-fx-text-fill: #007bc4; -fx-font-size: 13px; -fx-underline: false; -fx-padding: 0;");
        createAccount.setOnAction(event -> Navigation.goTo(stage, nextStage -> new SignUpUI().start(nextStage)));

        HBox links = new HBox(15, createAccount);
        links.setAlignment(Pos.CENTER_LEFT);

        cardContainer.getChildren().addAll(
                title,
                emailField,
                passwordField,
                rememberMe,
                loginButton,
                links,
                message
        );

        rootLayout.getChildren().add(cardContainer);
        stage.setTitle("EventRunner AI Event Management Platform");
        stage.setScene(new Scene(rootLayout, 1366, 768));
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
