package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

//displays the user's profile details and account fields
public class AccountUI extends Application
{

    @Override
    public void start(Stage stage)
    {
        //build the shared navigation bar at the top of the profile page
        Button homeButton = createNavigationButton("Home");
        Button eventsButton = createNavigationButton("My events");
        Button accountButton = createNavigationButton("Account");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox navigationBar = new HBox(30, homeButton, eventsButton, spacer, accountButton);
        navigationBar.setAlignment(Pos.CENTER_LEFT);
        navigationBar.setPadding(new Insets(10, 14, 10, 14));
        navigationBar.setStyle("-fx-background-color: black;");

        Label title = new Label("Account profile");
        title.setStyle("-fx-font-size: 24px; -fx-text-fill: black;");

        GridPane profile = new GridPane();
        profile.setHgap(8);
        profile.setVgap(17);
        profile.setPadding(new Insets(0, 0, 0, 3));

        //add the profile fields in the order shown to the user
        addField(profile, 0, "Username:", new TextField());
        addField(profile, 1, "Location:", new TextField());
        addField(profile, 2, "Email:", new TextField());
        addField(profile, 3, "Password:", new PasswordField());

        VBox content = new VBox(7, title, profile);
        content.setPadding(new Insets(15, 0, 0, 2));

        //combine the navigation bar and profile form in the page layout
        BorderPane layout = new BorderPane();
        layout.setTop(navigationBar);
        layout.setCenter(content);
        layout.setPadding(new Insets(27, 16, 20, 16));
        layout.setStyle("-fx-background-color: #d9d9d9;");

        stage.setTitle("Account profile");
        stage.setScene(new Scene(layout, 640, 400));
        stage.show();
    }

    //creates a consistently styled navigation button
    private static Button createNavigationButton(String text)
    {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: black;" + "-fx-text-fill: white;" + "-fx-font-size: 13px;" + "-fx-padding: 0;");
        return button;
    }

    //adds one labelled profile field to the grid
    private static void addField(GridPane profile, int row, String labelText, TextField field)
    {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: black;");

        field.setPromptText("...");
        field.setPrefSize(205, 23);
        field.setStyle("-fx-background-color: black;" + "-fx-text-fill: white;" + "-fx-prompt-text-fill: white;" + "-fx-font-size: 13px;" + "-fx-padding: 2px 6px;");

        profile.add(label, 0, row);
        profile.add(field, 1, row);
    }

    public static void main(String[] args) { launch(args); }
}
