package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

//displays expandable account and application settings sections
public class SettingsUI extends Application
{

    @Override
    public void start(Stage stage)
    {
        //build the shared navigation bar at the top of the settings page
        Button homeButton = createNavigationButton("Home");
        Button eventsButton = createNavigationButton("My events");
        Button accountButton = createNavigationButton("Account");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox navigationBar = new HBox(30, homeButton, eventsButton, spacer, accountButton);
        navigationBar.setAlignment(Pos.CENTER_LEFT);
        navigationBar.setPadding(new Insets(10, 14, 10, 14));
        navigationBar.setStyle("-fx-background-color: black;");

        Label title = new Label("Settings");
        title.setStyle("-fx-font-size: 24px; -fx-text-fill: black;");

        //each section starts collapsed and can reveal its description
        VBox settings = new VBox(
                10,
                createSettingsSection("Account details", "Update your name, email address, and account preferences."),
                createSettingsSection("Financial information", "Manage payment methods and billing information."),
                createSettingsSection("Notifications", "Choose which event and account notifications you receive."),
                createSettingsSection("Security", "Change your password and review account security options."),
                createSettingsSection("Other", "View additional settings for your account.")
        );
        settings.setPadding(new Insets(1, 0, 0, 8));

        VBox content = new VBox(8, title, settings);
        content.setPadding(new Insets(12, 0, 0, 0));

        BorderPane layout = new BorderPane();
        layout.setTop(navigationBar);
        layout.setCenter(content);
        layout.setPadding(new Insets(27, 16, 20, 16));
        layout.setStyle("-fx-background-color: #d9d9d9;");

        stage.setTitle("Settings");
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

    //creates one expandable settings section
    private static VBox createSettingsSection(String title, String detailsText)
    {
        Label arrow = new Label("▼");
        arrow.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

        Button button = createSettingsButton(title, arrow);
        Label details = new Label(detailsText);
        details.setWrapText(true);
        details.setMaxWidth(250);
        details.setStyle("-fx-text-fill: black; -fx-font-size: 12px;");

        VBox section = new VBox(5, button, details);
        details.setVisible(false);
        details.setManaged(false);

        //toggle both visibility and layout space when the section is clicked
        button.setOnAction(event -> {
            boolean expanded = details.isVisible();
            details.setVisible(!expanded);
            details.setManaged(!expanded);
            arrow.setText(expanded ? "▼" : "▲");
        });

        return section;
    }

    //creates the clickable header used for a settings section
    private static Button createSettingsButton(String text, Label arrow)
    {
        Button button = new Button();
        button.setGraphic(createSettingsLabel(text, arrow));
        button.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setPrefSize(154, 23);
        button.setMinSize(154, 23);
        button.setMaxSize(154, 23);
        button.setStyle("-fx-background-color: black;" + "-fx-background-radius: 0;" + "-fx-border-width: 0;" + "-fx-padding: 0;");
        return button;
    }

    //builds the header content with the title and expansion arrow
    private static HBox createSettingsLabel(String text, Label arrow)
    {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox content = new HBox(4, label, spacer, arrow);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(0, 7, 0, 3));
        content.setPrefWidth(154);
        return content;
    }

    public static void main(String[] args) { launch(args); }
}
