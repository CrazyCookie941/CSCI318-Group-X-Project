package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

//displays the sign-in form and validates the required fields
public class LoginUI extends Application
{

    @Override
    public void start(Stage stage)
    {
        //create the page heading and input controls
        Label title = new Label("Sign In");
        title.setStyle("-fx-font-size: 24px; -fx-text-fill: black;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setPrefSize(183, 38);
        emailField.setStyle("-fx-font-size: 14px;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefSize(183, 38);
        passwordField.setStyle("-fx-font-size: 14px;");

        CheckBox rememberMe = new CheckBox("Remember me");
        rememberMe.setStyle("-fx-font-size: 12px; -fx-text-fill: black;");

        Button loginButton = new Button("Sign In");
        loginButton.setPrefSize(210, 35);
        loginButton.setStyle("-fx-background-color: #2410ff;" + "-fx-text-fill: white;" + "-fx-font-size: 23px;");

        Label message = new Label();
        message.setStyle("-fx-text-fill: #d00000;");

        //show a validation message when the user submits the form
        loginButton.setOnAction(event -> {
            if (emailField.getText().isBlank() || passwordField.getText().isBlank()) { message.setText("Please enter both email and password."); }
            else { message.setText("Login submitted."); }
        });

        Label forgotPassword = new Label("Forgot password");
        forgotPassword.setStyle("-fx-text-fill: #2410ff; -fx-font-size: 12px;");

        Label createAccount = new Label("Create an account");
        createAccount.setStyle("-fx-text-fill: #2410ff; -fx-font-size: 12px;");

        //keep the account-related links together beneath the button
        HBox links = new HBox(10, forgotPassword, createAccount);
        links.setAlignment(Pos.CENTER_LEFT);

        VBox form = new VBox(
                20,
                title,
                emailField,
                passwordField,
                rememberMe,
                loginButton,
                links,
                message
        );
        form.setAlignment(Pos.CENTER_LEFT);
        form.setFillWidth(false);
        form.setPrefWidth(210);

        //place the form on the page and apply the page background
        StackPane layout = new StackPane(form);
        layout.setStyle("-fx-background-color: #d9d9d9;");
        layout.setPadding(new Insets(20));

        stage.setTitle("Sign In");
        stage.setScene(new Scene(layout, 620, 360));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
