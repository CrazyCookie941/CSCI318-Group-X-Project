package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Displays the user's profile details and account fields
public class AccountUI extends Application
{
    private static final String ACCOUNT_API =
            "http://localhost:8083/api/accounts/";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public void start(Stage stage)
    {
        /*
         * Account data belongs to an authenticated user.
         */
        if (!CurrentUserSession.isLoggedIn())
        {
            new Alert(
                    Alert.AlertType.WARNING,
                    "Please sign in before accessing your account."
            ).showAndWait();

            Navigation.goTo(
                    stage,
                    nextStage -> new LoginUI().start(nextStage)
            );

            return;
        }

        Long accountId = CurrentUserSession.getAccountId();

        // 1. Root Layout Container
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: #fafafa;");

        // 2. Styled Top Navigation Bar
        HBox navigationBar = Navigation.createNavigationBar(stage);
        layout.setTop(navigationBar);

        // 3. Central Content Split Frame
        HBox mainContent = new HBox(50);
        mainContent.setPadding(new Insets(35, 45, 35, 45));
        mainContent.setAlignment(Pos.TOP_CENTER);

        // --- LEFT COLUMN ---
        VBox leftColumn = new VBox(25);
        leftColumn.setMinWidth(420);

        Label title = new Label("Account profile");
        title.setStyle(
                "-fx-font-size: 26px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #222222;"
        );

        GridPane profileGrid = new GridPane();
        profileGrid.setHgap(20);
        profileGrid.setVgap(18);
        profileGrid.setAlignment(Pos.TOP_LEFT);

        ColumnConstraints labelColumn =
                new ColumnConstraints(120);

        ColumnConstraints fieldColumn =
                new ColumnConstraints(280);

        profileGrid.getColumnConstraints().addAll(
                labelColumn,
                fieldColumn
        );

        /*
         * Account fields.
         *
         * Username/email are initially populated from the
         * current session so the page does not appear empty
         * while the database request is running.
         *
         * They are then replaced with the authoritative
         * values returned by the backend.
         */
        TextField usernameField = new TextField();
        usernameField.setText(
                CurrentUserSession.getUsername() == null
                        ? ""
                        : CurrentUserSession.getUsername()
        );

        TextField locationField = new TextField();

        TextField emailField = new TextField();
        emailField.setText(
                CurrentUserSession.getEmail() == null
                        ? ""
                        : CurrentUserSession.getEmail()
        );

        addField(
                profileGrid,
                0,
                "Username:",
                usernameField,
                "Enter your username"
        );

        addField(
                profileGrid,
                1,
                "Location:",
                locationField,
                "e.g. Sydney, Australia"
        );

        addField(
                profileGrid,
                2,
                "Email:",
                emailField,
                "your.email@example.com"
        );

        // Password Field
        Label lblPassword = new Label("Password:");

        lblPassword.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-text-fill: #666666; " +
                        "-fx-font-weight: bold;"
        );

        HBox passwordFieldBox = new HBox(10);
        passwordFieldBox.setAlignment(Pos.CENTER_LEFT);

        PasswordField txtPassword =
                new PasswordField();

        /*
         * Never display a real password or password hash.
         */
        txtPassword.setText("••••••••••••");
        txtPassword.setEditable(false);
        txtPassword.setPrefHeight(32);

        txtPassword.setStyle(
                "-fx-background-color: #ededed; " +
                        "-fx-border-color: #cccccc; " +
                        "-fx-border-radius: 4; " +
                        "-fx-background-radius: 4; " +
                        "-fx-padding: 4 10; " +
                        "-fx-text-fill: #777777;"
        );

        HBox.setHgrow(
                txtPassword,
                Priority.ALWAYS
        );

        Button btnChangePassword =
                new Button("Change...");

        btnChangePassword.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #007bc4; " +
                        "-fx-border-color: #007bc4; " +
                        "-fx-border-radius: 4; " +
                        "-fx-height: 32; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;"
        );

        passwordFieldBox.getChildren().addAll(
                txtPassword,
                btnChangePassword
        );

        profileGrid.add(
                lblPassword,
                0,
                3
        );

        profileGrid.add(
                passwordFieldBox,
                1,
                3
        );

        // Two-factor authentication
        Label lblTwoFactor =
                new Label("2FA:");

        lblTwoFactor.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-text-fill: #666666; " +
                        "-fx-font-weight: bold;"
        );

        CheckBox chkTwoFactor =
                new CheckBox(
                        "Enable two-factor authentication"
                );

        chkTwoFactor.setSelected(true);

        chkTwoFactor.setStyle(
                "-fx-font-size: 13px; " +
                        "-fx-text-fill: #333333;"
        );

        profileGrid.add(
                lblTwoFactor,
                0,
                4
        );

        profileGrid.add(
                chkTwoFactor,
                1,
                4
        );

        // Delete Account section
        Label lblDeleteAccount =
                new Label("Delete account:");

        lblDeleteAccount.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-text-fill: #666666; " +
                        "-fx-font-weight: bold;"
        );

        Button btnDeleteAccount =
                new Button("Delete my account");

        btnDeleteAccount.setStyle(
                "-fx-background-color: #fff1f1; " +
                        "-fx-text-fill: #b00020; " +
                        "-fx-border-color: #d98d8d; " +
                        "-fx-border-radius: 4; " +
                        "-fx-padding: 8 14 8 14; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;"
        );

        btnDeleteAccount.setOnAction(event ->
                deleteCurrentAccount(stage, accountId, btnDeleteAccount)
        );

        profileGrid.add(
                lblDeleteAccount,
                0,
                5
        );

        profileGrid.add(
                btnDeleteAccount,
                1,
                5
        );

        // Footer Action Buttons
        HBox actionButtons = new HBox(15);

        Button btnSave =
                new Button("Save Changes");

        btnSave.setStyle(
                "-fx-background-color: #007bc4; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 24 10 24; " +
                        "-fx-background-radius: 4; " +
                        "-fx-cursor: hand;"
        );

        Button btnCancel =
                new Button("Cancel");

        btnCancel.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #555555; " +
                        "-fx-border-color: #cccccc; " +
                        "-fx-border-radius: 4; " +
                        "-fx-padding: 10 24 10 24; " +
                        "-fx-cursor: hand;"
        );

        /*
         * SAVE CHANGES
         *
         * Sends the edited profile to the real backend.
         */
        btnSave.setOnAction(event -> {

            String username =
                    usernameField.getText().trim();

            String email =
                    emailField.getText().trim();

            String location =
                    locationField.getText().trim();

            if (username.isBlank()
                    || email.isBlank())
            {
                showAlert(
                        Alert.AlertType.WARNING,
                        "Username and email are required."
                );

                return;
            }

            btnSave.setDisable(true);

            String requestBody =
                    "{"
                            + "\"username\":\""
                            + escapeJson(username)
                            + "\","
                            + "\"email\":\""
                            + escapeJson(email)
                            + "\","
                            + "\"location\":\""
                            + escapeJson(location)
                            + "\""
                            + "}";

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            ACCOUNT_API
                                                    + accountId
                                    )
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .PUT(
                                    HttpRequest.BodyPublishers
                                            .ofString(requestBody)
                            )
                            .build();

            httpClient.sendAsync(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    )
                    .whenComplete(
                            (response, error) ->
                                    Platform.runLater(() -> {

                                        btnSave.setDisable(false);

                                        if (error != null)
                                        {
                                            showAlert(
                                                    Alert.AlertType.ERROR,
                                                    "Cannot connect to the account service."
                                            );

                                            return;
                                        }

                                        if (response.statusCode() == 200)
                                        {
                                            String responseBody =
                                                    response.body();

                                            String updatedUsername =
                                                    extractJsonString(
                                                            responseBody,
                                                            "username"
                                                    );

                                            String updatedEmail =
                                                    extractJsonString(
                                                            responseBody,
                                                            "email"
                                                    );

                                            String updatedLocation =
                                                    extractNullableJsonString(
                                                            responseBody,
                                                            "location"
                                                    );

                                            /*
                                             * The server response is the
                                             * authoritative result.
                                             */
                                            if (updatedUsername != null)
                                            {
                                                usernameField.setText(
                                                        updatedUsername
                                                );
                                            }

                                            if (updatedEmail != null)
                                            {
                                                emailField.setText(
                                                        updatedEmail
                                                );
                                            }

                                            locationField.setText(
                                                    updatedLocation == null
                                                            ? ""
                                                            : updatedLocation
                                            );

                                            /*
                                             * Keep the in-memory logged-in
                                             * account consistent with H2.
                                             */
                                            CurrentUserSession.updateProfile(
                                                    updatedUsername == null
                                                            ? username
                                                            : updatedUsername,

                                                    updatedEmail == null
                                                            ? email
                                                            : updatedEmail
                                            );

                                            showAlert(
                                                    Alert.AlertType.INFORMATION,
                                                    "Account details saved successfully."
                                            );
                                        }
                                        else if (response.statusCode() == 400)
                                        {
                                            String responseBody =
                                                    response.body();

                                            if (responseBody.contains(
                                                    "Username already exists"))
                                            {
                                                showAlert(
                                                        Alert.AlertType.WARNING,
                                                        "Username already exists."
                                                );
                                            }
                                            else if (responseBody.contains(
                                                    "Email already exists"))
                                            {
                                                showAlert(
                                                        Alert.AlertType.WARNING,
                                                        "Email already exists."
                                                );
                                            }
                                            else
                                            {
                                                showAlert(
                                                        Alert.AlertType.WARNING,
                                                        "Unable to save account details."
                                                );
                                            }
                                        }
                                        else
                                        {
                                            showAlert(
                                                    Alert.AlertType.ERROR,
                                                    "Unable to save account details."
                                            );
                                        }
                                    })
                    );
        });

        /*
         * Cancel simply reloads the current profile,
         * discarding unsaved edits.
         */
        btnCancel.setOnAction(event ->
                loadAccountProfile(
                        accountId,
                        usernameField,
                        emailField,
                        locationField
                )
        );

        actionButtons.getChildren().addAll(
                btnSave,
                btnCancel
        );

        leftColumn.getChildren().addAll(
                title,
                profileGrid,
                actionButtons
        );

        // --- RIGHT COLUMN ---
        VBox rightColumn = new VBox(12);

        rightColumn.setAlignment(Pos.TOP_CENTER);

        rightColumn.setPadding(
                new Insets(
                        45,
                        0,
                        0,
                        0
                )
        );

        Label lblAvatarTitle =
                new Label("Profile picture:");

        lblAvatarTitle.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #666666;"
        );

        VBox avatarBox = new VBox();

        avatarBox.setPrefSize(
                140,
                140
        );

        avatarBox.setMaxSize(
                140,
                140
        );

        avatarBox.setStyle(
                "-fx-background-color: #e9e9e9; " +
                        "-fx-border-color: #cccccc; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 70; " +
                        "-fx-background-radius: 70;"
        );

        avatarBox.setAlignment(Pos.CENTER);

        Label lblAvatarPlaceholder =
                new Label("👤");

        lblAvatarPlaceholder.setStyle(
                "-fx-font-size: 48px; " +
                        "-fx-text-fill: #aaaaaa;"
        );

        avatarBox.getChildren().add(
                lblAvatarPlaceholder
        );

        Button btnUploadAvatar =
                new Button("Upload photo");

        btnUploadAvatar.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #555555; " +
                        "-fx-border-color: #cccccc; " +
                        "-fx-border-radius: 4; " +
                        "-fx-padding: 6 14 6 14; " +
                        "-fx-font-size: 12px; " +
                        "-fx-cursor: hand;"
        );

        rightColumn.getChildren().addAll(
                lblAvatarTitle,
                avatarBox,
                btnUploadAvatar
        );

        mainContent.getChildren().addAll(
                leftColumn,
                rightColumn
        );

        layout.setCenter(mainContent);

        stage.setTitle("EventRunner AI Event Management Platform");

        stage.setScene(new Scene(layout,1366, 768));

        stage.setResizable(false);
        stage.show();

        /*
         * Fetch the authoritative account profile from H2
         * as soon as this page opens.
         */
        loadAccountProfile(
                accountId,
                usernameField,
                emailField,
                locationField
        );
    }

    /**
     * Permanently deletes the signed-in account after password confirmation.
     * The backend re-verifies the password before deleting anything.
     */
    private void deleteCurrentAccount(
            Stage stage,
            Long accountId,
            Button deleteButton
    ) {
        if (!CurrentUserSession.isLoggedIn()
                || !accountId.equals(CurrentUserSession.getAccountId())) {
            showAlert(Alert.AlertType.WARNING, "Please sign in again.");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Delete account");
        dialog.setHeaderText("This permanently deletes your account.");

        Label warning = new Label(
                "Enter your current password to confirm. " +
                        "You cannot undo this action."
        );
        warning.setWrapText(true);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Current password");

        VBox content = new VBox(12, warning, passwordField);
        content.setPadding(new Insets(10));
        content.setPrefWidth(360);
        dialog.getDialogPane().setContent(content);

        ButtonType deleteAction = new ButtonType(
                "Delete account",
                ButtonBar.ButtonData.OK_DONE
        );
        dialog.getDialogPane().getButtonTypes().addAll(
                deleteAction,
                ButtonType.CANCEL
        );
        dialog.setResultConverter(buttonType ->
                buttonType == deleteAction ? passwordField.getText() : null
        );

        Optional<String> password = dialog.showAndWait();
        if (password.isEmpty()) {
            return;
        }
        if (password.get().isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Please enter your password.");
            return;
        }

        String body = "{\"password\":\"" + escapeJson(password.get()) + "\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ACCOUNT_API + accountId))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .method("DELETE", HttpRequest.BodyPublishers.ofString(body))
                .build();

        deleteButton.setDisable(true);
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .whenComplete((response, error) -> Platform.runLater(() -> {
                    deleteButton.setDisable(false);

                    if (error != null) {
                        showAlert(
                                Alert.AlertType.ERROR,
                                "Cannot connect to the account service. " +
                                        "Your account has not been confirmed as deleted."
                        );
                        return;
                    }

                    if (response.statusCode() == 200) {
                        CurrentUserSession.logout();
                        Session.logout();
                        showAlert(
                                Alert.AlertType.INFORMATION,
                                "Your account has been deleted."
                        );
                        new LandingPage().start(stage);
                        return;
                    }

                    String serverMessage = extractJsonString(
                            response.body(), "message"
                    );
                    if (serverMessage == null || serverMessage.isBlank()) {
                        serverMessage = "Account deletion failed. Please try again.";
                    }
                    showAlert(Alert.AlertType.WARNING, serverMessage);
                }));
    }

    /*
     * GET /api/accounts/{accountId}
     *
     * Loads the current account from the database.
     */
    private void loadAccountProfile(
            Long accountId,
            TextField usernameField,
            TextField emailField,
            TextField locationField
    )
    {
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        ACCOUNT_API
                                                + accountId
                                )
                        )
                        .GET()
                        .build();

        httpClient.sendAsync(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                )
                .whenComplete(
                        (response, error) ->
                                Platform.runLater(() -> {

                                    if (error != null)
                                    {
                                        showAlert(
                                                Alert.AlertType.ERROR,
                                                "Cannot load account details."
                                        );

                                        return;
                                    }

                                    if (response.statusCode() != 200)
                                    {
                                        showAlert(
                                                Alert.AlertType.ERROR,
                                                "Unable to load account details."
                                        );

                                        return;
                                    }

                                    String responseBody =
                                            response.body();

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

                                    String location =
                                            extractNullableJsonString(
                                                    responseBody,
                                                    "location"
                                            );

                                    if (username != null)
                                    {
                                        usernameField.setText(
                                                username
                                        );
                                    }

                                    if (email != null)
                                    {
                                        emailField.setText(
                                                email
                                        );
                                    }

                                    locationField.setText(
                                            location == null
                                                    ? ""
                                                    : location
                                    );

                                    /*
                                     * Refresh session data using the
                                     * authoritative backend values.
                                     */
                                    if (username != null
                                            && email != null)
                                    {
                                        CurrentUserSession.updateProfile(
                                                username,
                                                email
                                        );
                                    }
                                })
                );
    }

    private static void addField(
            GridPane profile,
            int row,
            String labelText,
            TextField field,
            String promptText
    )
    {
        Label label =
                new Label(labelText);

        label.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-text-fill: #666666; " +
                        "-fx-font-weight: bold;"
        );

        field.setPromptText(promptText);
        field.setMaxWidth(Double.MAX_VALUE);
        field.setPrefHeight(32);

        field.setStyle(
                "-fx-background-color: #ffffff; " +
                        "-fx-border-color: #cccccc; " +
                        "-fx-border-radius: 4; " +
                        "-fx-background-radius: 4; " +
                        "-fx-padding: 4 10;"
        );

        profile.add(
                label,
                0,
                row
        );

        profile.add(
                field,
                1,
                row
        );
    }

    private void showAlert(
            Alert.AlertType type,
            String message
    )
    {
        new Alert(
                type,
                message
        ).showAndWait();
    }

    private String escapeJson(String value)
    {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private String extractJsonString(
            String json,
            String fieldName
    )
    {
        Pattern pattern =
                Pattern.compile(
                        "\""
                                + Pattern.quote(fieldName)
                                + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\""
                );

        Matcher matcher =
                pattern.matcher(json);

        if (!matcher.find())
        {
            return null;
        }

        return unescapeJson(
                matcher.group(1)
        );
    }

    /*
     * Handles properties that can either contain
     * a JSON String or null.
     */
    private String extractNullableJsonString(
            String json,
            String fieldName
    )
    {
        Pattern nullPattern =
                Pattern.compile(
                        "\""
                                + Pattern.quote(fieldName)
                                + "\"\\s*:\\s*null"
                );

        if (nullPattern.matcher(json).find())
        {
            return null;
        }

        return extractJsonString(
                json,
                fieldName
        );
    }

    private String unescapeJson(String value)
    {
        return value
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}