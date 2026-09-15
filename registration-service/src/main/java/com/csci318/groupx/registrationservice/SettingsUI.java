package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// Displays expandable account and application settings sections with a clean, unified aesthetic
public class SettingsUI extends Application
{

    @Override
    public void start(Stage stage)
    {
        // 1. Root Layout Container
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: #fafafa;"); // Modern light background matching other views

        // 2. Styled Top Navigation Bar
        HBox navigationBar = Navigation.createNavigationBar(stage);
        layout.setTop(navigationBar);

        // 3. Central Working Content Pane
        VBox content = new VBox(25);
        content.setPadding(new Insets(35, 40, 35, 45));
        content.setAlignment(Pos.TOP_LEFT);

        Label title = new Label("Settings");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        // Vertical stack for the expandable layout card boxes
        VBox settingsContainer = new VBox(15);
        settingsContainer.setStyle("-fx-background-color: transparent;");
        settingsContainer.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(settingsContainer, Priority.ALWAYS);

        // Load expandable sections cleanly without structural constraints
        settingsContainer.getChildren().addAll(
                createSettingsSection("Account details", "Update your name, email address, and account preferences."),
                createSettingsSection("Financial information", "Manage payment methods, transactions, and billing information."),
                createSettingsSection("Notifications", "Choose which event reminders and account updates you receive dynamically."),
                createSettingsSection("Security", "Change your password, review active sessions, and update security parameters."),
                createSettingsSection("Other", "View system version logs, documentation resources, and corporate terms.")
        );

        content.getChildren().addAll(title, settingsContainer);

        // 4. Scroll Wrapper Integration (Safeguards against window clipping)
        ScrollPane scrollWrapper = new ScrollPane(content);
        scrollWrapper.setFitToWidth(true);
        scrollWrapper.setFitToHeight(true);
        scrollWrapper.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollWrapper.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollWrapper.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        BorderPane.setMargin(scrollWrapper, new Insets(0, 15, 0, 0));
        layout.setCenter(scrollWrapper);

        // 5. Stage Initialization Settings
        stage.setTitle("Application Settings");
        stage.setScene(new Scene(layout, 1020, 580)); // Matching design width geometry safely
        stage.show();
    }

    // Creates a consistently styled hyperlink navigation item matching prior designs
    // Creates one clean modern expandable settings block using an ultra-reliable BorderPane card rule
    private static VBox createSettingsSection(String sectionTitle, String detailsText)
    {
        VBox componentBlock = new VBox(0); // Holds header row and details drawer directly together
        componentBlock.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(componentBlock, Priority.ALWAYS);

        // --- ROW CONTROLLER: BorderPane handles foolproof left-text right-arrow positioning ---
        BorderPane rowHeaderCard = new BorderPane();
        rowHeaderCard.setPadding(new Insets(16, 24, 16, 24));
        rowHeaderCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.01), 4, 0, 0, 1);");

        // Section Title Label locked tightly into Left slot territory
        Label titleLabel = new Label(sectionTitle);
        titleLabel.setStyle("-fx-text-fill: #222222; -fx-font-size: 15px; -fx-font-weight: bold;");
        HBox leftWrapper = new HBox(titleLabel);
        leftWrapper.setAlignment(Pos.CENTER_LEFT);
        rowHeaderCard.setLeft(leftWrapper);

        // Dynamic Arrow Indicator locked tightly into Right slot territory
        Label arrowIndicator = new Label("▼");
        arrowIndicator.setStyle("-fx-text-fill: #777777; -fx-font-size: 13px; -fx-font-weight: bold;");
        HBox rightWrapper = new HBox(arrowIndicator);
        rightWrapper.setAlignment(Pos.CENTER_RIGHT);
        rowHeaderCard.setRight(rightWrapper);

        // --- DETAILS DRAWER PANEL ---
        Label detailsLabel = new Label(detailsText);
        detailsLabel.setWrapText(true);
        detailsLabel.setMaxWidth(Double.MAX_VALUE);
        detailsLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 14px; -fx-line-spacing: 3;");

        // Structured wrapper frame behind expanded text instructions
        VBox drawerContent = new VBox(detailsLabel);
        drawerContent.setPadding(new Insets(15, 24, 18, 24));
        drawerContent.setStyle("-fx-background-color: #f7f7f7; -fx-border-color: transparent #e0e0e0 #e0e0e0 #e0e0e0; -fx-border-width: 1; -fx-border-radius: 0 0 6 6; -fx-background-radius: 0 0 6 6;");

        // Starts collapsed cleanly
        drawerContent.setVisible(false);
        drawerContent.setManaged(false);

        // --- INTERACTIVE ACTIONS AND ANIMATION TRIGGERS ---
        // Highlight row header when hovering over the card container area
        rowHeaderCard.setOnMouseEntered(e -> rowHeaderCard.setStyle("-fx-background-color: #fcfcfc; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;"));
        rowHeaderCard.setOnMouseExited(e -> {
            if (drawerContent.isVisible()) {
                // Keep bottom card borders flat if open to link with the expanded panel cleanly
                rowHeaderCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0 #e0e0e0 transparent #e0e0e0; -fx-border-width: 1; -fx-border-radius: 6 6 0 0; -fx-background-radius: 6 6 0 0; -fx-cursor: hand;");
            } else {
                rowHeaderCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
            }
        });

        // Click interaction fires expansion state updates safely
        rowHeaderCard.setOnMouseClicked(event -> {
            boolean isExpanded = drawerContent.isVisible();
            drawerContent.setVisible(!isExpanded);
            drawerContent.setManaged(!isExpanded);
            arrowIndicator.setText(isExpanded ? "▼" : "▲");

            // Adjust card corner curves instantly depending on drawer layout states
            if (!isExpanded) {
                rowHeaderCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0 #e0e0e0 transparent #e0e0e0; -fx-border-width: 1; -fx-border-radius: 6 6 0 0; -fx-background-radius: 6 6 0 0; -fx-cursor: hand;");
            } else {
                rowHeaderCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
            }
        });

        componentBlock.getChildren().addAll(rowHeaderCard, drawerContent);
        return componentBlock;
    }

    public static void main(String[] args) { launch(args); }
}
