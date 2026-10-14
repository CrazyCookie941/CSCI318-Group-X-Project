package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.concurrent.CompletableFuture;

// Displays a clean, flat settings view using functional CheckBoxes instead of nested accordion dropdowns
public class SettingsUI extends Application
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

        // 3. Central Working Content Pane
        VBox content = new VBox(25);
        content.setPadding(new Insets(35, 40, 35, 45));
        content.setAlignment(Pos.TOP_LEFT);

        Label title = new Label("Settings");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        // Vertical stack for the flat layout card boxes
        VBox settingsContainer = new VBox(15);
        settingsContainer.setStyle("-fx-background-color: transparent;");
        settingsContainer.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(settingsContainer, Priority.ALWAYS);

        // Load flat sections cleanly with static contents displayed directly
        settingsContainer.getChildren().addAll(
                createFlatSettingsSection("Notifications", "Choose whether to receive event notifications and which area to follow.",
                        createNotificationSettingsContent())
        );

        content.getChildren().addAll(title, settingsContainer);

        // 4. Scroll Wrapper Integration
        ScrollPane scrollWrapper = new ScrollPane(content);
        scrollWrapper.setFitToWidth(true);
        scrollWrapper.setFitToHeight(true);
        scrollWrapper.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollWrapper.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollWrapper.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        BorderPane.setMargin(scrollWrapper, new Insets(0, 15, 0, 0));
        layout.setCenter(scrollWrapper);

        // 5. Stage Initialization Settings
        stage.setTitle("Application Settings");
        stage.setScene(new Scene(layout, 1366, 768));
        stage.show();
    }

    private static VBox createNotificationSettingsContent()
    {
        CheckBox enabled = new CheckBox("Receive notifications");
        enabled.setStyle("-fx-opacity: 1.0; -fx-text-fill: #222222;");

        TextField area = new TextField();
        area.setPromptText("Area or suburb to follow (for example, Sydney)");
        area.setStyle("-fx-opacity: 1.0; -fx-text-fill: #222222;");

        area.setMaxWidth(420);
        Button save = new Button("Save notification settings");
        Label desc = new Label("Notify me when a new event is posted in this area:");
        desc.setStyle("-fx-opacity: 1.0; -fx-text-fill: #222222;");
        Label status = new Label();
        VBox controls = new VBox(10,
                enabled,
                desc,
                area,
                save,
                status);

        Long accountId = CurrentUserSession.getAccountId();
        if (accountId == null) {
            enabled.setDisable(true);
            area.setDisable(true);
            save.setDisable(true);
            return controls;
        }

        enabled.setDisable(true);
        area.setDisable(true);
        save.setDisable(true);
        status.setText("Loading settings...");
        CompletableFuture.supplyAsync(() -> NotificationApiClient.preference(accountId))
                .whenComplete((preference, error) -> Platform.runLater(() -> {
                    if (error != null || preference == null) {
                        status.setText("Could not load notification settings.");
                        return;
                    }
                    enabled.setSelected(preference.enabled());
                    area.setText(preference.area() == null ? "" : preference.area());
                    enabled.setDisable(false);
                    area.setDisable(false);
                    save.setDisable(false);
                    status.setText("");
                }));

        save.setOnAction(event -> {
            enabled.setDisable(true);
            area.setDisable(true);
            save.setDisable(true);
            status.setText("Saving...");
            CompletableFuture.supplyAsync(() -> NotificationApiClient.savePreference(
                            accountId, enabled.isSelected(), area.getText()))
                    .whenComplete((saved, error) -> Platform.runLater(() -> {
                        enabled.setDisable(false);
                        area.setDisable(false);
                        save.setDisable(false);
                        status.setText(error == null && saved != null
                                ? "Notification settings saved."
                                : "Could not save settings. Please try again.");
                    }));
        });
        return controls;
    }

    // Generates a container card that explicitly displays its configuration options immediately
    private static VBox createFlatSettingsSection(String sectionTitle, String detailsText, Node settingsContent)
    {
        VBox componentBlock = new VBox(0);
        componentBlock.setMaxWidth(Double.MAX_VALUE);

        // Header Panel Container
        VBox headerCard = new VBox(4);
        headerCard.setPadding(new Insets(16, 24, 12, 24));
        headerCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0 #e0e0e0 transparent #e0e0e0; -fx-border-width: 1; -fx-border-radius: 6 6 0 0; -fx-background-radius: 6 6 0 0;");

        Label titleLabel = new Label(sectionTitle);
        titleLabel.setStyle("-fx-text-fill: #222222; -fx-font-size: 15px; -fx-font-weight: bold;");

        Label summaryLabel = new Label(detailsText);
        summaryLabel.setWrapText(true);
        summaryLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 13px;");

        headerCard.getChildren().addAll(titleLabel, summaryLabel);

        // Options Content Panel Box
        VBox optionsDrawer = new VBox(settingsContent);
        optionsDrawer.setPadding(new Insets(12, 24, 18, 24));
        optionsDrawer.setStyle("-fx-background-color: #f7f7f7; -fx-border-color: transparent #e0e0e0 #e0e0e0 #e0e0e0; -fx-border-width: 1; -fx-border-radius: 0 0 6 6; -fx-background-radius: 0 0 6 6;");

        componentBlock.getChildren().addAll(headerCard, optionsDrawer);
        return componentBlock;
    }

    // Helper utility to turn string properties into checkbox nodes instantly
    private static VBox createCheckboxGroup(String... options)
    {
        VBox container = new VBox(10);
        for (String option : options) {
            CheckBox cb = new CheckBox(option);
            cb.setStyle("-fx-text-fill: #444444; -fx-font-size: 14px;");
            container.getChildren().add(cb);
        }
        return container;
    }

    public static void main(String[] args) { launch(args); }
}
