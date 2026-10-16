package com.csci318.groupx.registrationservice;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LandingPage extends javafx.application.Application
{
    private static final String PRIMARY_BUTTON_STYLE =
            "-fx-background-color: #007bc4; -fx-text-fill: white; "
                    + "-fx-font-size: 15px; -fx-font-weight: bold; "
                    + "-fx-padding: 12 28 12 28; -fx-background-radius: 6; "
                    + "-fx-cursor: hand;";
    private static final String PRIMARY_BUTTON_HOVER_STYLE =
            "-fx-background-color: #005fa3; -fx-text-fill: white; "
                    + "-fx-font-size: 15px; -fx-font-weight: bold; "
                    + "-fx-padding: 12 28 12 28; -fx-background-radius: 6; "
                    + "-fx-cursor: hand;";
    private static final String SECONDARY_BUTTON_STYLE =
            "-fx-background-color: transparent; -fx-text-fill: #007bc4; "
                    + "-fx-border-color: #007bc4; -fx-border-width: 2; "
                    + "-fx-font-size: 15px; -fx-font-weight: bold; "
                    + "-fx-padding: 10 26 10 26; -fx-background-radius: 6; "
                    + "-fx-cursor: hand;";

    @Override
    public void start(Stage stage)
    {
        VBox root = new VBox();
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #ffffff;");

        VBox hero = new VBox(24);
        hero.setAlignment(Pos.CENTER);
        hero.setPadding(new Insets(64, 40, 54, 40));
        hero.setStyle("-fx-background-color: linear-gradient(to bottom, #f0f7fc, #ffffff);");

        Label title = new Label("Discover What's Happening Locally");
        title.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; "
                + "-fx-text-fill: #111111; -fx-text-alignment: center;");

        Label subtitle = new Label("Your guide to the best local events, powered by cutting-edge AI");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #555555; "
                + "-fx-text-alignment: center;");

        Button signUpButton = new Button("Sign up");
        signUpButton.setStyle(PRIMARY_BUTTON_STYLE);
        signUpButton.setOnMouseEntered(event -> signUpButton.setStyle(PRIMARY_BUTTON_HOVER_STYLE));
        signUpButton.setOnMouseExited(event -> signUpButton.setStyle(PRIMARY_BUTTON_STYLE));
        signUpButton.setOnAction(event ->
                Navigation.goTo(stage, nextStage -> new SignUpUI().start(nextStage)));

        Button loginButton = new Button("Log in");
        loginButton.setStyle(SECONDARY_BUTTON_STYLE);
        loginButton.setOnAction(event ->
                Navigation.goTo(stage, nextStage -> new LoginUI().start(nextStage)));

        Button assistantButton = new Button("Ask the AI assistant");
        assistantButton.setStyle(SECONDARY_BUTTON_STYLE);
        assistantButton.setOnAction(event ->
                Navigation.goTo(stage, nextStage -> new ChatbotUI().start(nextStage)));

        HBox actions = new HBox(16, signUpButton, loginButton, assistantButton);
        actions.setAlignment(Pos.CENTER);
        hero.getChildren().addAll(title, subtitle, actions);

        HBox features = new HBox(20);
        features.setAlignment(Pos.CENTER);
        features.setPadding(new Insets(20, 40, 60, 40));
        features.setMaxWidth(1000);

        features.getChildren().addAll(
                createFeatureCard("Trusted by over\n10M+ users",
                        "Join a thriving community searching for secure local spaces daily."),
                createFeatureCard("Used by event\norganisers globally",
                        "Scale your operations utilizing corporate management systems seamlessly."),
                createFeatureCard("Find the best quality\nevents, at lowest prices",
                        "Maximize savings tracking early-bird discounts built for budgets."),
                createFeatureCard("All through\ntop-tier AI assistance",
                        "Personalized predictive parsing feeds smart calendars instantly.")
        );

        root.getChildren().addAll(hero, features);

        stage.setTitle("Welcome to EventGuide AI Platform");
        stage.setScene(new Scene(root, 1366, 768));
        stage.show();
    }

    private VBox createFeatureCard(String titleText, String descriptionText)
    {
        VBox card = new VBox(12);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(20));
        card.setPrefWidth(220);
        card.setStyle("-fx-background-color: #fafafa; -fx-border-color: #e0e0e0; "
                + "-fx-border-width: 1; -fx-border-radius: 8; "
                + "-fx-background-radius: 8;");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label title = new Label(titleText);
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; "
                + "-fx-text-fill: #111111; -fx-line-spacing: 2;");

        Label description = new Label(descriptionText);
        description.setWrapText(true);
        description.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666; "
                + "-fx-line-spacing: 3;");

        card.getChildren().addAll(title, description);
        return card;
    }

    public static void main(String[] args) { launch(args);  }
}
