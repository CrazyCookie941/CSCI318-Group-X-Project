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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Displays the sign-in form and authenticates against registration-service
public class LoginUI extends Application {

    private static final String LOGIN_URL =
            "http://localhost:8082/api/auth/login";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

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
        cardContainer.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 4);"
        );

        // Page Header
        Label title = new Label("Sign In");
        title.setStyle(
                "-fx-font-size: 26px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #1a1a1a;" +
                        "-fx-padding: 0 0 5 0;"
        );

        // Username / Email
        TextField identifierField = new TextField();
        identifierField.setPromptText("Email or username");
        identifierField.setPrefHeight(38);
        identifierField.setMaxWidth(Double.MAX_VALUE);
        identifierField.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-border-color: #cccccc;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;" +
                        "-fx-padding: 4 12;" +
                        "-fx-font-size: 14px;"
        );

        // Password
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(38);
        passwordField.setMaxWidth(Double.MAX_VALUE);
        passwordField.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-border-color: #cccccc;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;" +
                        "-fx-padding: 4 12;" +
                        "-fx-font-size: 14px;"
        );

        // Validation / authentication message
        Label message = new Label();
        message.setWrapText(true);
        message.setStyle(
                "-fx-text-fill: #d00000;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;"
        );

        CheckBox rememberMe = new CheckBox("Remember me");
        rememberMe.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #555555;" +
                        "-fx-cursor: hand;"
        );

        Button loginButton = new Button("Sign In");
        loginButton.setPrefHeight(40);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setStyle(
                "-fx-background-color: #007bc4;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;"
        );

        loginButton.setOnMouseEntered(e ->
                loginButton.setStyle(
                        "-fx-background-color: #005fa3;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 15px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 4;" +
                                "-fx-cursor: hand;"
                )
        );

        loginButton.setOnMouseExited(e ->
                loginButton.setStyle(
                        "-fx-background-color: #007bc4;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 15px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 4;" +
                                "-fx-cursor: hand;"
                )
        );

        /*
         * Real authentication.
         *
         * JavaFX UI thread is not blocked:
         * HttpClient.sendAsync performs the HTTP request asynchronously.
         */
        loginButton.setOnAction(event -> {

            String identifier = identifierField.getText().trim();
            String password = passwordField.getText();

            if (identifier.isBlank() || password.isBlank()) {
                message.setStyle(
                        "-fx-text-fill: #d00000;" +
                                "-fx-font-size: 13px;" +
                                "-fx-font-weight: bold;"
                );
                message.setText("Please enter both email/username and password.");
                return;
            }

            loginButton.setDisable(true);
            message.setStyle(
                    "-fx-text-fill: #555555;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: bold;"
            );
            message.setText("Signing in...");

            String requestBody =
                    "{"
                            + "\"identifier\":\""
                            + escapeJson(identifier)
                            + "\","
                            + "\"password\":\""
                            + escapeJson(password)
                            + "\""
                            + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(LOGIN_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            httpClient.sendAsync(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    )
                    .whenComplete((response, error) ->
                            Platform.runLater(() -> {

                                loginButton.setDisable(false);

                                if (error != null) {
                                    message.setStyle(
                                            "-fx-text-fill: #d00000;" +
                                                    "-fx-font-size: 13px;" +
                                                    "-fx-font-weight: bold;"
                                    );
                                    message.setText(
                                            "Cannot connect to the authentication service."
                                    );
                                    return;
                                }

                                if (response.statusCode() == 200) {

                                    String responseBody = response.body();

                                    Long accountId =
                                            extractJsonLong(
                                                    responseBody,
                                                    "accountId"
                                            );

                                    String username =
                                            extractJsonString(
                                                    responseBody,
                                                    "username"
                                            );

                                    String email =
                                            extractJsonString(
                                                    responseBody,
                                                    "email"
                                            );

                                    if (accountId == null
                                            || username == null
                                            || email == null) {

                                        message.setStyle(
                                                "-fx-text-fill: #d00000;" +
                                                        "-fx-font-size: 13px;" +
                                                        "-fx-font-weight: bold;"
                                        );

                                        message.setText(
                                                "Login response was invalid."
                                        );
                                        return;
                                    }

                                    CurrentUserSession.login(
                                            accountId,
                                            username,
                                            email
                                    );

                                    Navigation.goTo(
                                            stage,
                                            nextStage ->
                                                    new DashboardUI()
                                                            .start(nextStage)
                                    );

                                } else if (response.statusCode() == 401) {

                                    message.setStyle(
                                            "-fx-text-fill: #d00000;" +
                                                    "-fx-font-size: 13px;" +
                                                    "-fx-font-weight: bold;"
                                    );

                                    message.setText(
                                            "Invalid username/email or password."
                                    );

                                } else {

                                    message.setStyle(
                                            "-fx-text-fill: #d00000;" +
                                                    "-fx-font-size: 13px;" +
                                                    "-fx-font-weight: bold;"
                                    );

                                    message.setText(
                                            "Login failed. Please try again."
                                    );
                                }
                            })
                    );
        });

        // Navigation Hyperlink Options
        Hyperlink forgotPassword =
                new Hyperlink("Forgot password?");

        forgotPassword.setStyle(
                "-fx-text-fill: #007bc4;" +
                        "-fx-font-size: 13px;" +
                        "-fx-underline: false;" +
                        "-fx-padding: 0;"
        );

        Hyperlink createAccount =
                new Hyperlink("Create an account");

        createAccount.setStyle(
                "-fx-text-fill: #007bc4;" +
                        "-fx-font-size: 13px;" +
                        "-fx-underline: false;" +
                        "-fx-padding: 0;"
        );

        createAccount.setOnAction(event ->
                Navigation.goTo(
                        stage,
                        nextStage ->
                                new SignUpUI().start(nextStage)
                )
        );

        HBox links =
                new HBox(
                        15,
                        forgotPassword,
                        createAccount
                );

        links.setAlignment(Pos.CENTER_LEFT);

        cardContainer.getChildren().addAll(
                title,
                identifierField,
                passwordField,
                rememberMe,
                loginButton,
                links,
                message
        );

        rootLayout.getChildren().add(cardContainer);

        stage.setTitle("Sign In");
        stage.setScene(
                new Scene(
                        rootLayout,
                        640,
                        480
                )
        );
        stage.setResizable(false);
        stage.show();
    }

    /*
     * Escapes the characters required for our small JSON request.
     * No extra JSON dependency is required.
     */
    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    /*
     * Reads a numeric property from the authentication response.
     *
     * The response format is controlled by our own backend.
     */
    private Long extractJsonLong(
            String json,
            String fieldName
    ) {

        Pattern pattern = Pattern.compile(
                "\""
                        + Pattern.quote(fieldName)
                        + "\"\\s*:\\s*(\\d+)"
        );

        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            return null;
        }

        return Long.parseLong(
                matcher.group(1)
        );
    }

    /*
     * Reads a String property from the authentication response.
     */
    private String extractJsonString(
            String json,
            String fieldName
    ) {

        Pattern pattern = Pattern.compile(
                "\""
                        + Pattern.quote(fieldName)
                        + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\""
        );

        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            return null;
        }

        return unescapeJson(
                matcher.group(1)
        );
    }

    private String unescapeJson(String value) {
        return value
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    public static void main(String[] args) {
        launch(args);
    }
}