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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

// Displays the account registration form with a clean, unified aesthetic matching other workspace views
public class SignUpUI extends Application
{
    private static final String SIGNUP_URL =
            "http://localhost:8082/api/auth/signup";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

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
        cardContainer.setStyle(
                "-fx-background-color: #ffffff; " +
                        "-fx-border-color: #e0e0e0; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 4);"
        );

        // Typography Header Grouping Block
        VBox headersBlock = new VBox(4);
        headersBlock.setAlignment(Pos.TOP_LEFT);

        Label welcome = new Label("Welcome");
        welcome.setStyle(
                "-fx-font-size: 28px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #1a1a1a;"
        );

        Label title = new Label("Create an account to get started");
        title.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-text-fill: #666666; " +
                        "-fx-padding: 0 0 5 0;"
        );

        headersBlock.getChildren().addAll(welcome, title);

        // Input Fields (Unified look with generous click/touch targets)

        // Username is required by the account backend
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefHeight(38);
        usernameField.setMaxWidth(Double.MAX_VALUE);
        usernameField.setStyle(
                "-fx-background-color: #ffffff; " +
                        "-fx-border-color: #cccccc; " +
                        "-fx-border-radius: 4; " +
                        "-fx-background-radius: 4; " +
                        "-fx-padding: 4 12; " +
                        "-fx-font-size: 14px;"
        );

        TextField emailField = new TextField();
        emailField.setPromptText("Email address");
        emailField.setPrefHeight(38);
        emailField.setMaxWidth(Double.MAX_VALUE);
        emailField.setStyle(
                "-fx-background-color: #ffffff; " +
                        "-fx-border-color: #cccccc; " +
                        "-fx-border-radius: 4; " +
                        "-fx-background-radius: 4; " +
                        "-fx-padding: 4 12; " +
                        "-fx-font-size: 14px;"
        );

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(38);
        passwordField.setMaxWidth(Double.MAX_VALUE);
        passwordField.setStyle(
                "-fx-background-color: #ffffff; " +
                        "-fx-border-color: #cccccc; " +
                        "-fx-border-radius: 4; " +
                        "-fx-background-radius: 4; " +
                        "-fx-padding: 4 12; " +
                        "-fx-font-size: 14px;"
        );

        // Form Control Options
        CheckBox rememberMe = new CheckBox("Remember me");
        rememberMe.setStyle(
                "-fx-font-size: 13px; " +
                        "-fx-text-fill: #555555; " +
                        "-fx-cursor: hand;"
        );

        // Premium Primary Blue Action Button
        Button signUpButton = new Button("Sign Up");
        signUpButton.setPrefHeight(40);
        signUpButton.setMaxWidth(Double.MAX_VALUE);
        signUpButton.setStyle(
                "-fx-background-color: #007bc4; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 15px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 4; " +
                        "-fx-cursor: hand;"
        );

        // Dynamic hover animation color transformations
        signUpButton.setOnMouseEntered(e ->
                signUpButton.setStyle(
                        "-fx-background-color: #005fa3; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 15px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-radius: 4; " +
                                "-fx-cursor: hand;"
                )
        );

        signUpButton.setOnMouseExited(e ->
                signUpButton.setStyle(
                        "-fx-background-color: #007bc4; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 15px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-radius: 4; " +
                                "-fx-cursor: hand;"
                )
        );

        // Interactive Validation Status Message
        Label message = new Label();
        message.setWrapText(true);
        message.setStyle(
                "-fx-text-fill: #d00000; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold;"
        );

        // Real registration submission
        signUpButton.setOnAction(event -> {

            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();

            // Basic frontend validation
            if (username.isBlank()
                    || email.isBlank()
                    || password.isBlank())
            {
                message.setStyle(
                        "-fx-text-fill: #d00000; " +
                                "-fx-font-size: 13px; " +
                                "-fx-font-weight: bold;"
                );

                message.setText(
                        "Please enter username, email and password."
                );

                return;
            }

            // Prevent repeated clicks while the request is running
            signUpButton.setDisable(true);

            message.setStyle(
                    "-fx-text-fill: #555555; " +
                            "-fx-font-size: 13px; " +
                            "-fx-font-weight: bold;"
            );

            message.setText("Creating account...");

            // Build JSON body for the backend
            String requestBody =
                    "{"
                            + "\"username\":\""
                            + escapeJson(username)
                            + "\","
                            + "\"email\":\""
                            + escapeJson(email)
                            + "\","
                            + "\"password\":\""
                            + escapeJson(password)
                            + "\""
                            + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SIGNUP_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            /*
             * sendAsync prevents the JavaFX UI thread
             * from freezing while waiting for Spring Boot.
             */
            httpClient.sendAsync(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    )
                    .whenComplete((response, error) ->
                            Platform.runLater(() -> {

                                signUpButton.setDisable(false);

                                // Backend could not be reached
                                if (error != null)
                                {
                                    message.setStyle(
                                            "-fx-text-fill: #d00000; " +
                                                    "-fx-font-size: 13px; " +
                                                    "-fx-font-weight: bold;"
                                    );

                                    message.setText(
                                            "Cannot connect to the account service."
                                    );

                                    return;
                                }

                                // Registration successful
                                if (response.statusCode() == 200)
                                {
                                    message.setStyle(
                                            "-fx-text-fill: #008000; " +
                                                    "-fx-font-size: 13px; " +
                                                    "-fx-font-weight: bold;"
                                    );

                                    message.setText(
                                            "Account created successfully."
                                    );

                                    // After creating the account,
                                    // return to Sign In
                                    Navigation.goTo(
                                            stage,
                                            nextStage ->
                                                    new LoginUI()
                                                            .start(nextStage)
                                    );
                                }

                                // Backend validation / duplicate account
                                else if (response.statusCode() == 400)
                                {
                                    String responseBody = response.body();

                                    message.setStyle(
                                            "-fx-text-fill: #d00000; " +
                                                    "-fx-font-size: 13px; " +
                                                    "-fx-font-weight: bold;"
                                    );

                                    if (responseBody.contains(
                                            "Username already exists"))
                                    {
                                        message.setText(
                                                "Username already exists."
                                        );
                                    }
                                    else if (responseBody.contains(
                                            "Email already exists"))
                                    {
                                        message.setText(
                                                "Email already exists."
                                        );
                                    }
                                    else
                                    {
                                        message.setText(
                                                "Unable to create account. Please check your details."
                                        );
                                    }
                                }

                                // Unexpected server response
                                else
                                {
                                    message.setStyle(
                                            "-fx-text-fill: #d00000; " +
                                                    "-fx-font-size: 13px; " +
                                                    "-fx-font-weight: bold;"
                                    );

                                    message.setText(
                                            "Sign up failed. Please try again."
                                    );
                                }
                            })
                    );
        });

        // Navigation Footer Links Grouping
        Label accountLabel = new Label("Already have an account? ");
        accountLabel.setStyle(
                "-fx-text-fill: #555555; " +
                        "-fx-font-size: 13px;"
        );

        Hyperlink accountLink = new Hyperlink("Sign In");
        accountLink.setStyle(
                "-fx-text-fill: #007bc4; " +
                        "-fx-font-size: 13px; " +
                        "-fx-underline: false; " +
                        "-fx-padding: 0; " +
                        "-fx-font-weight: bold;"
        );

        accountLink.setOnAction(event ->
                Navigation.goTo(
                        stage,
                        nextStage ->
                                new LoginUI().start(nextStage)
                )
        );

        HBox accountRow =
                new HBox(accountLabel, accountLink);

        accountRow.setAlignment(Pos.CENTER_LEFT);

        // Assembly of card and core inputs
        cardContainer.getChildren().addAll(
                headersBlock,
                usernameField,
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
        stage.setScene(
                new Scene(
                        rootLayout,
                        640,
                        540
                )
        );

        stage.setResizable(false);
        stage.show();
    }

    /*
     * Escapes characters that could otherwise break
     * the JSON request body.
     */
    private String escapeJson(String value)
    {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}