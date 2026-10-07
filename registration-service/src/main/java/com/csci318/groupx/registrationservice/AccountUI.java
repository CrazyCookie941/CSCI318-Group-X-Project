package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

//displays the user's profile details and account fields
public class AccountUI extends Application
{

    @Override
    public void start(Stage stage)
    {
        // 1. Root Layout Container
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: #fafafa;");

        // 2. Styled Top Navigation Bar
        HBox navigationBar = Navigation.createNavigationBar(stage);
        layout.setTop(navigationBar);

        // 3. Central Content Split Frame (Form Left, Image Right)
        HBox mainContent = new HBox(50);
        mainContent.setPadding(new Insets(35, 45, 35, 45));
        mainContent.setAlignment(Pos.TOP_CENTER);

        // --- LEFT COLUMN: Profile Account Information ---
        VBox leftColumn = new VBox(25);
        leftColumn.setMinWidth(420);

        Label title = new Label("Account profile");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        GridPane profileGrid = new GridPane();
        profileGrid.setHgap(20);
        profileGrid.setVgap(18);
        profileGrid.setAlignment(Pos.TOP_LEFT);

        ColumnConstraints labelColumn = new ColumnConstraints(120);
        ColumnConstraints fieldColumn = new ColumnConstraints(280);
        profileGrid.getColumnConstraints().addAll(labelColumn, fieldColumn);

        // Add core informational fields
        addField(profileGrid, 0, "Username:", new TextField(), "Enter your username");
        addField(profileGrid, 1, "Location:", new TextField(), "e.g. Sydney, Australia");
        addField(profileGrid, 2, "Email:", new TextField(), "your.email@example.com");

        // Integrated Password Field with Change Option
        Label lblPassword = new Label("Password:");
        lblPassword.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-font-weight: bold;");

        HBox passwordFieldBox = new HBox(10);
        passwordFieldBox.setAlignment(Pos.CENTER_LEFT);

        PasswordField txtPassword = new PasswordField();
        txtPassword.setText("••••••••••••");
        txtPassword.setEditable(false);
        txtPassword.setPrefHeight(32);
        txtPassword.setStyle("-fx-background-color: #ededed; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 10; -fx-text-fill: #777777;");
        HBox.setHgrow(txtPassword, Priority.ALWAYS);

        Button btnChangePassword = new Button("Change...");
        btnChangePassword.setStyle("-fx-background-color: transparent; -fx-text-fill: #007bc4; -fx-border-color: #007bc4; -fx-border-radius: 4; -fx-height: 32; -fx-font-weight: bold; -fx-cursor: hand;");

        passwordFieldBox.getChildren().addAll(txtPassword, btnChangePassword);

        profileGrid.add(lblPassword, 0, 3);
        profileGrid.add(passwordFieldBox, 1, 3);

        Label lblTwoFactor = new Label("2FA:");
        lblTwoFactor.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-font-weight: bold;");

        CheckBox chkTwoFactor = new CheckBox("Enable two-factor authentication");
        chkTwoFactor.setSelected(true);
        chkTwoFactor.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        profileGrid.add(lblTwoFactor, 0, 4);
        profileGrid.add(chkTwoFactor, 1, 4);

        Label lblDeleteAccount = new Label("Delete account:");
        lblDeleteAccount.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-font-weight: bold;");

        Button btnDeleteAccount = new Button("Delete my account");
        btnDeleteAccount.setStyle("-fx-background-color: #fff1f1; -fx-text-fill: #b00020; -fx-border-color: #d98d8d; -fx-border-radius: 4; -fx-padding: 8 14 8 14; -fx-font-weight: bold; -fx-cursor: hand;");
        btnDeleteAccount.setOnAction(event ->
                new Alert(Alert.AlertType.CONFIRMATION, "This action is permanent. Please confirm you want to delete your account.").showAndWait());

        profileGrid.add(lblDeleteAccount, 0, 5);
        profileGrid.add(btnDeleteAccount, 1, 5);

        // Footer Action Trigger Elements
        HBox actionButtons = new HBox(15);
        Button btnSave = new Button("Save Changes");
        btnSave.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 24 10 24; -fx-background-radius: 4; -fx-cursor: hand;");

        Button btnCancel = new Button("Cancel");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-padding: 10 24 10 24; -fx-cursor: hand;");

        actionButtons.getChildren().addAll(btnSave, btnCancel);
        leftColumn.getChildren().addAll(title, profileGrid, actionButtons);

        // --- RIGHT COLUMN: Profile Picture Slot Area ---
        VBox rightColumn = new VBox(12);
        rightColumn.setAlignment(Pos.TOP_CENTER);
        rightColumn.setPadding(new Insets(45, 0, 0, 0));

        Label lblAvatarTitle = new Label("Profile picture:");
        lblAvatarTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #666666;");

        // Circular / rounded picture framing layout window
        VBox avatarBox = new VBox();
        avatarBox.setPrefSize(140, 140);
        avatarBox.setMaxSize(140, 140);
        avatarBox.setStyle("-fx-background-color: #e9e9e9; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 70; -fx-background-radius: 70;");
        avatarBox.setAlignment(Pos.CENTER);

        Label lblAvatarPlaceholder = new Label("👤");
        lblAvatarPlaceholder.setStyle("-fx-font-size: 48px; -fx-text-fill: #aaaaaa;");
        avatarBox.getChildren().add(lblAvatarPlaceholder);

        Button btnUploadAvatar = new Button("Upload photo");
        btnUploadAvatar.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-padding: 6 14 6 14; -fx-font-size: 12px; -fx-cursor: hand;");

        rightColumn.getChildren().addAll(lblAvatarTitle, avatarBox, btnUploadAvatar);

        // Combine left information form layout and right column profile layout together inside center container
        mainContent.getChildren().addAll(leftColumn, rightColumn);
        layout.setCenter(mainContent);

        // 4. Stage Presentation
        stage.setTitle("Account profile");
        stage.setScene(new Scene(layout, 1366, 768)); // Adjusted layout sizing for two columns
        stage.setResizable(false);
        stage.show();
    }

    private static void addField(GridPane profile, int row, String labelText, TextField field, String promptText)
    {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-font-weight: bold;");

        field.setPromptText(promptText);
        field.setMaxWidth(Double.MAX_VALUE);
        field.setPrefHeight(32);
        field.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 10;");

        profile.add(label, 0, row);
        profile.add(field, 1, row);
    }

    public static void main(String[] args) { launch(args); }
}
