package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.web.client.RestClientResponseException;

public class MyListingsEventGoer extends Application
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

        Label title = new Label("Event Admissions");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        VBox listingsContainer = new VBox(15);
        listingsContainer.setStyle("-fx-background-color: transparent;");
        listingsContainer.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(listingsContainer, Priority.ALWAYS);

        Long accountId = CurrentUserSession.getAccountId();
        boolean bookingsLoaded = false;
        try {
            if (accountId == null || CurrentUserSession.isManager()) {
                throw new IllegalStateException("Sign in as an event goer to view your bookings.");
            }

            for (EventServiceClient.ServiceRegistration registration
                    : EventServiceClient.getRegistrations(accountId)) {
                EventCatalog.getEvents().stream()
                        .filter(listing -> listing.id() == registration.eventId())
                        .findFirst()
                        .ifPresent(listing -> listingsContainer.getChildren().add(
                                createListing(stage, listing, registration, accountId)
                        ));
            }
            bookingsLoaded = true;
        } catch (RuntimeException exception) {
            new Alert(Alert.AlertType.ERROR,
                    "Your bookings could not be loaded. "
                            + bookingLoadError(exception))
                    .showAndWait();
        }
        if (bookingsLoaded && listingsContainer.getChildren().isEmpty()) {
            listingsContainer.getChildren().add(createEmptyMessage());
        }

        content.getChildren().addAll(title, listingsContainer);

        // 4. Scroll Wrapper Integration
        ScrollPane scrollWrapper = new ScrollPane(content);
        scrollWrapper.setFitToWidth(true);
        scrollWrapper.setFitToHeight(true);
        scrollWrapper.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollWrapper.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollWrapper.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        BorderPane.setMargin(scrollWrapper, new Insets(0, 15, 0, 0));
        layout.setCenter(scrollWrapper);

        // 5. Stage Initialization Settings
        stage.setTitle("EventRunner AI Event Management Platform");
        stage.setScene(new Scene(layout, 1366, 768));
        stage.setResizable(false);
        stage.show();
    }

    private HBox createListing(
            Stage stage,
            EventListing listing,
            EventServiceClient.ServiceRegistration registration,
            long accountId
    )
    {
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
        Label name = new Label(listing.title());
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
        Button invoiceButton = new Button("View Invoice");
        invoiceButton.setPrefWidth(100);
        invoiceButton.setPrefHeight(32);
        String normalInvoiceStyle = "-fx-background-color: transparent; -fx-text-fill: #555555; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;";
        String hoverInvoiceStyle = "-fx-background-color: #f5f5f5; -fx-text-fill: #333333; -fx-border-color: #999999; -fx-border-radius: 4; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;";
        invoiceButton.setStyle(normalInvoiceStyle);
        invoiceButton.setOnMouseEntered(e -> invoiceButton.setStyle(hoverInvoiceStyle));
        invoiceButton.setOnMouseExited(e -> invoiceButton.setStyle(normalInvoiceStyle));
        invoiceButton.setOnAction(event -> Navigation.goTo(stage,
                nextStage -> new TransactionUI(listing).start(nextStage)));

        // Cancel Booking Button
        Button removeButton = new Button("Cancel Booking");
        removeButton.setPrefWidth(125);
        removeButton.setPrefHeight(32);
        String normalRemoveStyle = "-fx-background-color: transparent; -fx-text-fill: #c62828; -fx-border-color: #d32f2f; -fx-border-radius: 4; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;";
        String hoverRemoveStyle = "-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-border-color: #d32f2f; -fx-border-radius: 4; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;";
        removeButton.setStyle(normalRemoveStyle);
        removeButton.setOnMouseEntered(e -> removeButton.setStyle(hoverRemoveStyle));
        removeButton.setOnMouseExited(e -> removeButton.setStyle(normalRemoveStyle));
        removeButton.setOnAction(event -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Cancel your booking for \"" + listing.title() + "\"?",
                    ButtonType.OK, ButtonType.CANCEL);
            confirm.setTitle("Cancel booking");
            confirm.setHeaderText(null);
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }

            removeButton.setDisable(true);
            try {
                EventServiceClient.cancelRegistration(registration.id(), accountId);
                VBox parentContainer = (VBox) listingRow.getParent();
                parentContainer.getChildren().remove(listingRow);
                if (parentContainer.getChildren().isEmpty()) {
                    parentContainer.getChildren().add(createEmptyMessage());
                }
                showMessage("Booking Cancelled",
                        "Your booking for \"" + listing.title() + "\" has been cancelled.");
            } catch (RuntimeException exception) {
                removeButton.setDisable(false);
                new Alert(Alert.AlertType.ERROR,
                        "The booking could not be cancelled: " + exception.getMessage())
                        .showAndWait();
            }
        });

        actionButtonGroup.getChildren().addAll(viewButton, invoiceButton, removeButton);

        // ARRANGE NODES CORRECTLY: Name stays on the left, spacer expands in the middle, buttons sit on the right
        listingRow.getChildren().addAll(imageView, name, listingSpacer, actionButtonGroup);
        return listingRow;
    }

    private static Label createEmptyMessage() {
        Label emptyMessage = new Label("You have no event bookings yet.");
        emptyMessage.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
        return emptyMessage;
    }

    private static String bookingLoadError(RuntimeException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof RestClientResponseException response
                    && response.getStatusCode().value() == 404) {
                return "Restart the event service so it loads the bookings API, then try again.";
            }
            cause = cause.getCause();
        }
        return "Check that the event service is running on port 8081, then try again.";
    }

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
