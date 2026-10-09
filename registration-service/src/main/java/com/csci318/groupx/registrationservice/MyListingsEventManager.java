
package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.control.ButtonType;

// Displays and manages the events uploaded by the current corporate event organiser
public class MyListingsEventManager extends Application
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

        Label title = new Label("Manage Events");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        Label listingsTitle = new Label("Your listings");
        listingsTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        // Vertical stack for the individual listing cards
        VBox listingsContainer = new VBox(15);
        listingsContainer.setStyle("-fx-background-color: transparent;");
        listingsContainer.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(listingsContainer, Priority.ALWAYS);

        // Show only events owned by the currently signed-in event manager.
        Long managerId = CurrentUserSession.getAccountId();
        if (managerId != null && CurrentUserSession.isManager()) {
            for (EventListing listing : EventCatalog.getEvents()) {
                if (listing.ownerId() != null
                        && managerId.longValue() == listing.ownerId().longValue()) {
                    listingsContainer.getChildren().add(createListing(stage, listing));
                }
            }
        }
        if (listingsContainer.getChildren().isEmpty()) {
            Label noEvents = new Label("You haven't created any events yet.");
            noEvents.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
            listingsContainer.getChildren().add(noEvents);
        }

        Button createEventButton = new Button("Create event");
        createEventButton.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 8 18 8 18; -fx-cursor: hand;");
        createEventButton.setOnAction(event -> Navigation.goTo(stage, nextStage -> new CreateEventUI().start(nextStage)));

        content.getChildren().addAll(title, createEventButton, listingsTitle, listingsContainer);

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
        stage.setTitle("Organiser Administration Console");
        stage.setScene(new Scene(layout, 1366, 768));
        stage.show();
    }

    // Creates a consistently styled hyperlink navigation item matching prior designs
    // Creates one clean modern listing card row configured with left-snapped typography
    private HBox createListing(Stage stage, EventListing listing)
    {
        String listingName = listing.title();
        HBox listingRow = new HBox(15);
        listingRow.setAlignment(Pos.CENTER_LEFT);
        listingRow.setPadding(new Insets(12, 20, 12, 16));
        listingRow.setPrefHeight(75);
        listingRow.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.02), 4, 0, 0, 2);");
        HBox.setHgrow(listingRow, Priority.ALWAYS);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(0);   // Set to 0 if not displaying images to avoid blank spacing gaps
        imageView.setFitHeight(0);
        imageView.setPreserveRatio(true);

        // Text Heading Configuration
        Label name = new Label(listingName);
        name.setWrapText(true);
        name.setAlignment(Pos.CENTER_LEFT);
        name.setStyle("-fx-text-fill: #222222; -fx-font-size: 15px; -fx-font-weight: bold;");
        // Force the text component wrapper to grow dynamically and take up the remaining left space
        HBox.setHgrow(name, Priority.ALWAYS);
        name.setMaxWidth(Double.MAX_VALUE);

        // CRITICAL FIX: The spacer is now placed on the right side of the heading label
        // This keeps the title locked to the left side while pushing only the button group to the right
        Region listingSpacer = new Region();
        HBox.setHgrow(listingSpacer, Priority.ALWAYS);

        HBox actionButtonGroup = new HBox(10);
        actionButtonGroup.setAlignment(Pos.CENTER_RIGHT);

        // View Listing Button
        Button viewButton = new Button("View Listing");
        viewButton.setPrefWidth(100);
        viewButton.setPrefHeight(32);
        viewButton.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");
        viewButton.setOnMouseEntered(e -> viewButton.setStyle("-fx-background-color: #005fa3; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;"));
        viewButton.setOnMouseExited(e -> viewButton.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;"));
        viewButton.setOnAction(event -> Navigation.goTo(stage, nextStage -> new ViewEventUI(listing).start(nextStage)));

        // View Invoice Button
        Button invoiceButton = new Button("Edit Listing");
        invoiceButton.setPrefWidth(100);
        invoiceButton.setPrefHeight(32);
        String normalInvoiceStyle = "-fx-background-color: transparent; -fx-text-fill: #555555; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;";
        String hoverInvoiceStyle = "-fx-background-color: #f5f5f5; -fx-text-fill: #333333; -fx-border-color: #999999; -fx-border-radius: 4; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;";
        invoiceButton.setStyle(normalInvoiceStyle);
        invoiceButton.setOnMouseEntered(e -> invoiceButton.setStyle(hoverInvoiceStyle));
        invoiceButton.setOnMouseExited(e -> invoiceButton.setStyle(normalInvoiceStyle));
        invoiceButton.setOnAction(event -> showMessage("Invoice Details", "Generating financial invoice tracking slip for: " + listingName));

        // Cancel Booking Button
        Button removeButton = new Button("Cancel Event");
        removeButton.setPrefWidth(110);
        removeButton.setPrefHeight(32);
        String normalRemoveStyle = "-fx-background-color: transparent; -fx-text-fill: #c62828; -fx-border-color: #d32f2f; -fx-border-radius: 4; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;";
        String hoverRemoveStyle = "-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-border-color: #d32f2f; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;";
        removeButton.setStyle(normalRemoveStyle);
        removeButton.setOnMouseEntered(e -> removeButton.setStyle(hoverRemoveStyle));
        removeButton.setOnMouseExited(e -> removeButton.setStyle(normalRemoveStyle));
        removeButton.setOnAction(event -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete \"" + listingName + "\"? This can't be undone.",
                    ButtonType.OK, ButtonType.CANCEL);
            confirm.setTitle("Cancel event");
            confirm.setHeaderText(null);
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }

            try {
                if (EventCatalog.delete(listing)) {
                    VBox parentContainer = (VBox) listingRow.getParent();
                    parentContainer.getChildren().remove(listingRow);
                    showMessage("Event deleted", "\"" + listingName + "\" has been deleted.");
                } else {
                    showMessage("Not allowed", "Only the manager who created this event can delete it.");
                }
            } catch (RuntimeException exception) {
                showMessage("Error", "The event could not be deleted: " + exception.getMessage());
            }
        });

        MenuItem telemetryItem = new MenuItem("View telemetry");
        telemetryItem.setOnAction(event -> showTelemetryWindow(listingName));

        MenuButton moreButton = new MenuButton("...");
        moreButton.setPrefWidth(42);
        moreButton.setPrefHeight(32);
        moreButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand;");
        moreButton.getItems().add(telemetryItem);

        actionButtonGroup.getChildren().addAll(viewButton, invoiceButton, removeButton, moreButton);

        // ARRANGE NODES CORRECTLY: Name stays on the left, spacer expands in the middle, buttons sit on the right
        listingRow.getChildren().addAll(imageView, name, listingSpacer, actionButtonGroup);
        return listingRow;
    }

    private void showTelemetryWindow(String listingName)
    {
        Stage telemetryStage = new Stage();
        telemetryStage.initOwner(Window.getWindows().stream()
                .filter(window -> window instanceof Stage)
                .map(window -> (Stage) window)
                .findFirst()
                .orElse(null));
        telemetryStage.setTitle("Telemetry - " + listingName);

        TelemetryData data = mockTelemetryFor(listingName);

        VBox telemetryContent = new VBox(18);
        telemetryContent.setPadding(new Insets(28));
        telemetryContent.setStyle("-fx-background-color: #fafafa;");

        Label title = new Label(listingName);
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        Label subtitle = new Label("Mock telemetry data for this event listing");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

        HBox stats = new HBox(14,
                createTelemetryValue("Views", String.valueOf(data.views())),
                createTelemetryValue("Registrations", String.valueOf(data.registrations())),
                createTelemetryValue("Conversion rate", data.conversionRate() + "%"),
                createTelemetryValue("Shares", String.valueOf(data.shares())));
        stats.setAlignment(Pos.CENTER_LEFT);

        Label activityTitle = new Label("Recent activity");
        activityTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        Label activity = new Label(
                "Last 7 days: " + data.weeklyViews() + " views, " + data.weeklyRegistrations()
                        + " registrations, and " + data.attendanceRate() + "% estimated attendance."
        );
        activity.setWrapText(true);
        activity.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");

        telemetryContent.getChildren().addAll(title, subtitle, stats, activityTitle, activity);
        telemetryStage.setScene(new Scene(telemetryContent, 760, 300));
        telemetryStage.show();
    }

    private VBox createTelemetryValue(String labelText, String valueText)
    {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: #777777;");

        Label value = new Label(valueText);
        value.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #007bc4;");

        VBox valueBox = new VBox(3, label, value);
        valueBox.setPadding(new Insets(12, 16, 12, 16));
        valueBox.setMinWidth(130);
        valueBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
        return valueBox;
    }

    private TelemetryData mockTelemetryFor(String listingName)
    {
        return switch (listingName) {
            case "Community barbecue" -> new TelemetryData(482, 76, 15.8, 34, 301, 49, 82);
            case "Radiohead concert" -> new TelemetryData(1250, 214, 17.1, 126, 780, 144, 91);
            case "Nightclub dance event" -> new TelemetryData(936, 128, 13.7, 88, 574, 79, 76);
            case "Bingo night social" -> new TelemetryData(318, 61, 19.2, 21, 202, 39, 88);
            default -> new TelemetryData(0, 0, 0, 0, 0, 0, 0);
        };
    }

    private record TelemetryData(
            int views,
            int registrations,
            double conversionRate,
            int shares,
            int weeklyViews,
            int weeklyRegistrations,
            int attendanceRate
    )
    {
    }

    // Displays feedback for listing actions
    private static void showMessage(String title, String message)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}
