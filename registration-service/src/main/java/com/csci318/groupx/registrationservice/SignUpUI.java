package com.csci318.groupx.registrationservice;

import javafx.application.Application;
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

// Displays the account registration form with a clean, unified aesthetic matching other workspace views
public class SignUpUI extends Application
{

    @Override
    public void start(Stage stage)
    {
        // 1. Root Layout Backdrop Container
        StackPane rootLayout = new StackPane();
        rootLayout.setStyle("-fx-background-color: #fafafa;"); // Modern ultra-light background matching other screens
        rootLayout.setPadding(new Insets(30));

        // 2. Centralized Registration Form Card Panel
        VBox cardContainer = new VBox(22);
        cardContainer.setAlignment(Pos.TOP_LEFT);
        cardContainer.setMaxSize(380, 480); // Keeps the signup capsule perfectly proportioned
        cardContainer.setPadding(new Insets(35, 40, 35, 40));
        // Clean white card structure featuring a subtle drop-shadow effect
        cardContainer.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 4);");

        // Typography Header Grouping Block
        VBox headersBlock = new VBox(4);
        headersBlock.setAlignment(Pos.TOP_LEFT);

        Label welcome = new Label("Welcome");
        welcome.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");

        Label title = new Label("Create an account to get started");
        title.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-padding: 0 0 5 0;");

        headersBlock.getChildren().addAll(welcome, title);

        // Input Fields (Unified look with generous click/touch targets)
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

        // Form Control Options
        CheckBox rememberMe = new CheckBox("Remember me");
        rememberMe.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555; -fx-cursor: hand;");

        // Premium Primary Blue Action Button
        Button signUpButton = new Button("Sign Up");
        signUpButton.setPrefHeight(40);
        signUpButton.setMaxWidth(Double.MAX_VALUE); // Stretches across the card container block
        signUpButton.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");

        // Dynamic hover animation color transformations
        signUpButton.setOnMouseEntered(e -> signUpButton.setStyle("-fx-background-color: #005fa3; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;"));
        signUpButton.setOnMouseExited(e -> signUpButton.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;"));

        // Interactive Validation Status Message
        Label message = new Label();
        message.setWrapText(true);
        message.setStyle("-fx-text-fill: #d00000; -fx-font-size: 13px; -fx-font-weight: bold;");

        // Form registration submission checking rules
        signUpButton.setOnAction(event -> {
            if (emailField.getText().isBlank() || passwordField.getText().isBlank()) {
                message.setText("Please enter both email and password.");
            } else {
                message.setText("Account created successfully.");
            }
        });

        // Navigation Footer Links Grouping
        Label accountLabel = new Label("Already have an account? ");
        accountLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 13px;");

        Hyperlink accountLink = new Hyperlink("Sign In");
        accountLink.setStyle("-fx-text-fill: #007bc4; -fx-font-size: 13px; -fx-underline: false; -fx-padding: 0; -fx-font-weight: bold;");

        HBox accountRow = new HBox(accountLabel, accountLink);
        accountRow.setAlignment(Pos.CENTER_LEFT);

        // Assembly of card and core inputs
        cardContainer.getChildren().addAll(
                headersBlock,
                emailField,
                passwordField,
                rememberMe,
                signUpButton,
                accountRow,
                message
        );

        rootLayout.getChildren().add(cardContainer);

        // 3. Stage Presentation Sizing
        stage.setTitle("Create Account");
        stage.setScene(new Scene(rootLayout, 640, 540)); // Dimensions tracking clean window layout metrics
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
