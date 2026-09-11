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

//displays the account registration form
public class SignUpUI extends Application
{

    @Override
    public void start(Stage stage)
    {
        //create the registration heading and account input controls
        Label welcome = new Label("Welcome");
        welcome.setStyle("-fx-font-size: 34px; -fx-text-fill: black;");

        Label title = new Label("Sign Up");
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

        Button signUpButton = new Button("Sign Up");
        signUpButton.setPrefSize(195, 35);
        signUpButton.setStyle("-fx-background-color: #2410ff;" + "-fx-text-fill: white;" + "-fx-font-size: 23px;");

        Label accountLink = new Label("Already have an account?");
        accountLink.setStyle("-fx-text-fill: #2410ff; -fx-font-size: 12px;");

        Label message = new Label();
        message.setStyle("-fx-text-fill: #d00000;");

        //validate the required fields before reporting the submission result
        signUpButton.setOnAction(event -> {
            if (emailField.getText().isBlank() || passwordField.getText().isBlank()) { message.setText("Please enter both email and password."); }
            else { message.setText("Account created."); }
        });

        //keep the existing-account link aligned with the form
        HBox accountRow = new HBox(accountLink);
        accountRow.setAlignment(Pos.CENTER_LEFT);

        VBox form = new VBox(
                20,
                welcome,
                title,
                emailField,
                passwordField,
                rememberMe,
                signUpButton,
                accountRow,
                message
        );
        form.setAlignment(Pos.CENTER_LEFT);
        form.setFillWidth(false);
        form.setPrefWidth(195);

        //place the form on the page and apply the page background
        StackPane layout = new StackPane(form);
        layout.setStyle("-fx-background-color: #d9d9d9;");
        layout.setPadding(new Insets(20));

        stage.setTitle("Sign Up");
        stage.setScene(new Scene(layout, 620, 390));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
