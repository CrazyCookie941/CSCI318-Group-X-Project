package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class IntroUI extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. Root Layout Container
        VBox root = new VBox();
        root.setStyle("-fx-background-color: #ffffff;");
        root.setAlignment(Pos.TOP_CENTER);

        // 2. HERO SECTION (Header, Subtitle, and Call To Actions)
        VBox heroContainer = new VBox(24);
        heroContainer.setPadding(new Insets(80, 40, 60, 40));
        heroContainer.setAlignment(Pos.CENTER);
        // Subtle background gradient to give depth to the landing screen
        heroContainer.setStyle("-fx-background-color: linear-gradient(to bottom, #f0f7fc, #ffffff);");

        // Main catchy application title (H1)
        Label mainTitle = new Label("Discover What's Happening Locally");
        mainTitle.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #111111; -fx-text-alignment: center;");

        // Subtitle line containing the wireframe text
        Label subTitle = new Label("Your guide to the best local events, powered by cutting-edge AI");
        subTitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #555555; -fx-text-alignment: center;");

        // Horizontal layout for actionable buttons
        HBox ctaBox = new HBox(16);
        ctaBox.setAlignment(Pos.CENTER);

        Button btnSignUp = new Button("Sign up");
        btnSignUp.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 12 28 12 28; -fx-background-radius: 6; -fx-cursor: hand;");

        Button btnLogIn = new Button("Log in");
        btnLogIn.setStyle("-fx-background-color: transparent; -fx-text-fill: #007bc4; -fx-border-color: #007bc4; -fx-border-width: 2; -fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 10 26 10 26; -fx-background-radius: 6; -fx-cursor: hand;");

        // Dynamic styling transitions on element interaction
        btnSignUp.setOnMouseEntered(e -> btnSignUp.setStyle("-fx-background-color: #005fa3; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 12 28 12 28; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnSignUp.setOnMouseExited(e -> btnSignUp.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 12 28 12 28; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnSignUp.setOnAction(event -> Navigation.goTo(primaryStage, nextStage -> new SignUpUI().start(nextStage)));
        btnLogIn.setOnAction(event -> Navigation.goTo(primaryStage, nextStage -> new LoginUI().start(nextStage)));

        ctaBox.getChildren().addAll(btnSignUp, btnLogIn);
        heroContainer.getChildren().addAll(mainTitle, subTitle, ctaBox);

        // 3. FEATURES GRID SECTION (Value Propositions)
        HBox featuresGrid = new HBox(20); // 20px spacing between metric columns
        featuresGrid.setPadding(new Insets(20, 40, 60, 40));
        featuresGrid.setAlignment(Pos.CENTER);
        featuresGrid.setMaxWidth(1000); // Caps content width neatly on large screens

        // Build feature cards using correct static API layout assignments
        VBox card1 = createFeatureCard("Trusted by over\n10M+ users", "Join a thriving community searching for secure local spaces daily.");
        VBox card2 = createFeatureCard("Used by event\norganisers globally", "Scale your operations utilizing corporate management systems seamlessly.");
        VBox card3 = createFeatureCard("Find the best quality\nevents, at lowest prices", "Maximize savings tracking early-bird discounts built for budgets.");
        VBox card4 = createFeatureCard("All through\ntop-tier AI assistance", "Personalized predictive parsing feeds smart calendars instantly.");

        featuresGrid.getChildren().addAll(card1, card2, card3, card4);

        // Assemble the page parts
        root.getChildren().addAll(heroContainer, featuresGrid);

        // 4. Scene Presentation
        Scene scene = new Scene(root, 1024, 576);
        primaryStage.setTitle("Welcome to EventGuide AI Platform");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Helper layout constructor factory creating unified promotional info cards
     */
    private VBox createFeatureCard(String boldTitleText, String paragraphText) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        // Clean grey container border styling
        card.setStyle("-fx-background-color: #fafafa; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        card.setAlignment(Pos.TOP_LEFT);

        // Corrected layout allocation assignment
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setPrefWidth(220);

        Label title = new Label(boldTitleText);
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #111111; -fx-line-spacing: 2;");

        Label body = new Label(paragraphText);
        body.setWrapText(true);
        body.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666; -fx-line-spacing: 3;");

        card.getChildren().addAll(title, body);
        return card;
    }

    public static void main(String[] args) {
        launch(args);
    }
}