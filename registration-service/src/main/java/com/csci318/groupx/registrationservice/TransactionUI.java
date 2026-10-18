package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import org.springframework.web.client.RestClientResponseException;

import java.util.Locale;

public class TransactionUI extends Application {

    private final EventListing event;

    public TransactionUI() {
        this(null);
    }

    public TransactionUI(EventListing event) {
        this.event = event;
    }

    @Override
    public void start(Stage stage) {

        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: #fafafa;");

        layout.setTop(Navigation.createNavigationBar(stage));

        HBox mainContent = new HBox(45);
        mainContent.setPadding(
                new Insets(35, 45, 35, 45)
        );
        mainContent.setAlignment(Pos.TOP_CENTER);

        // LEFT COLUMN
        VBox leftColumn = new VBox(25);
        leftColumn.setMinWidth(460);

        Label title = new Label(
                "Secure Checkout - Simulation"
        );
        title.setStyle(
                "-fx-font-size: 26px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #222222;"
        );

        Label disclaimer = new Label(
                "Coursework demo only. No real payment occurs.\n" +
                        "Please do not enter real card details."
        );
        disclaimer.setWrapText(true);
        disclaimer.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #805300;"
        );

        GridPane checkoutGrid = new GridPane();
        checkoutGrid.setHgap(20);
        checkoutGrid.setVgap(18);
        checkoutGrid.setAlignment(Pos.TOP_LEFT);

        checkoutGrid.getColumnConstraints().addAll(
                new ColumnConstraints(130),
                new ColumnConstraints(290)
        );

        // Payment method selection
        Label paymentLabel = new Label(
                "Payment Provider:"
        );
        paymentLabel.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #666666;"
        );

        ToggleGroup paymentGroup = new ToggleGroup();

        RadioButton visa =
                providerButton("Visa", paymentGroup);

        RadioButton mastercard =
                providerButton("Mastercard", paymentGroup);

        RadioButton paypal =
                providerButton("PayPal", paymentGroup);

        switch (AppPreferences.preferredPaymentProvider(CurrentUserSession.getAccountId())) {
            case "MASTERCARD" -> mastercard.setSelected(true);
            case "PAYPAL" -> paypal.setSelected(true);
            default -> visa.setSelected(true);
        }

        HBox providers = new HBox(
                18,
                visa,
                mastercard,
                paypal
        );
        providers.setAlignment(Pos.CENTER_LEFT);

        checkoutGrid.add(paymentLabel, 0, 0);
        checkoutGrid.add(providers, 1, 0);

        // Simulated decline switch
        Label sandboxLabel = new Label(
                "Test Scenario:"
        );
        sandboxLabel.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #666666;"
        );

        CheckBox decline = new CheckBox(
                "Simulate declined payment"
        );
        decline.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #444444;"
        );

        checkoutGrid.add(sandboxLabel, 0, 1);
        checkoutGrid.add(decline, 1, 1);

        // Payment feedback
        Label feedback = new Label();
        feedback.setWrapText(true);
        feedback.setMaxWidth(440);
        feedback.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #555555;"
        );

        // Action buttons
        HBox actionButtons = new HBox(15);
        actionButtons.setPadding(
                new Insets(10, 0, 0, 0)
        );

        Button btnPay = new Button(
                "Complete Payment"
        );

        String payStyle =
                "-fx-background-color: #007bc4;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 11 26 11 26;" +
                        "-fx-background-radius: 4;" +
                        "-fx-font-size: 14px;" +
                        "-fx-cursor: hand;";

        btnPay.setStyle(payStyle);

        btnPay.setOnMouseEntered(e -> {
            if (!btnPay.isDisabled()) {
                btnPay.setStyle(
                        payStyle.replace(
                                "#007bc4",
                                "#005fa3"
                        )
                );
            }
        });

        btnPay.setOnMouseExited(e ->
                btnPay.setStyle(payStyle)
        );

        Button btnCancel = new Button("Back");
        btnCancel.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #555555;" +
                        "-fx-border-color: #cccccc;" +
                        "-fx-border-radius: 4;" +
                        "-fx-padding: 11 24 11 24;" +
                        "-fx-font-size: 14px;" +
                        "-fx-cursor: hand;"
        );

        btnCancel.setOnAction(e ->
                Navigation.goBack(stage)
        );

        actionButtons.getChildren().addAll(
                btnPay,
                btnCancel
        );

        leftColumn.getChildren().addAll(
                title,
                disclaimer,
                checkoutGrid,
                actionButtons,
                feedback
        );

        // RIGHT COLUMN - ORDER SUMMARY
        VBox rightColumn = new VBox(15);
        rightColumn.setMinWidth(300);
        rightColumn.setPadding(
                new Insets(50, 0, 0, 0)
        );

        Label summaryTitle = new Label(
                "Order Summary"
        );
        summaryTitle.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #666666;"
        );

        VBox summaryCard = new VBox(14);
        summaryCard.setPadding(new Insets(20));
        summaryCard.setStyle(
                "-fx-background-color: #f7f7f7;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;"
        );

        if (event == null || event.id() <= 0) {

            summaryCard.getChildren().add(
                    summaryRow(
                            "Event:",
                            "No valid event selected"
                    )
            );

            btnPay.setDisable(true);

        } else {

            String amount = String.format(
                    Locale.US,
                    "AUD $%.2f",
                    EventCatalog.parsePrice(
                            event.price()
                    )
            );

            summaryCard.getChildren().addAll(
                    summaryRow(
                            "Event:",
                            event.title()
                    ),
                    summaryRow(
                            "Venue:",
                            event.location()
                    ),
                    summaryRow(
                            "Type:",
                            event.type()
                    ),
                    summaryRow(
                            "Ticket:",
                            "1x General Admission"
                    ),
                    new Separator(),
                    summaryRow(
                            "Subtotal:",
                            amount
                    ),
                    totalRow(
                            "Total Amount:",
                            amount
                    )
            );
        }

        rightColumn.getChildren().addAll(
                summaryTitle,
                summaryCard
        );

        mainContent.getChildren().addAll(
                leftColumn,
                rightColumn
        );

        ScrollPane scroll = new ScrollPane(
                mainContent
        );
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background: transparent;"
        );
        scroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );
        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        layout.setCenter(scroll);

        // COMPLETE PAYMENT ACTION
        btnPay.setOnAction(action -> {

            if (event == null || event.id() <= 0) {
                show(
                        Alert.AlertType.WARNING,
                        "Event required",
                        "Please select an event."
                );
                return;
            }

            if (!CurrentUserSession.isLoggedIn()) {
                show(
                        Alert.AlertType.WARNING,
                        "Login required",
                        "Please sign in before making a payment."
                );
                return;
            }

            if (CurrentUserSession.isManager()) {
                show(
                        Alert.AlertType.WARNING,
                        "Event Goer required",
                        "Please use an Event Goer account."
                );
                return;
            }

            RadioButton selected =
                    (RadioButton)
                            paymentGroup.getSelectedToggle();

            if (selected == null) {
                show(
                        Alert.AlertType.WARNING,
                        "Payment method required",
                        "Select a payment provider."
                );
                return;
            }

            String method = selected.getText()
                    .toUpperCase(Locale.ROOT);

            btnPay.setDisable(true);
            feedback.setText(
                    "Processing simulated payment..."
            );

            Long accountId =
                    CurrentUserSession.getAccountId();

            PaymentApiClient.simulate(
                    event.id(),
                    accountId,
                    method,
                    decline.isSelected()
            ).whenComplete((result, error) ->

                    Platform.runLater(() -> {

                        if (error != null) {

                            btnPay.setDisable(false);

                            String detail =
                                    "Payment could not be completed. " +
                                            "Check Payment Service (8085) " +
                                            "and Event Service (8081).";

                            Throwable cause = error;

                            while (cause != null) {

                                if (cause instanceof
                                        RestClientResponseException responseEx) {

                                    detail =
                                            "Payment request failed (HTTP " +
                                                    responseEx.getStatusCode().value() +
                                                    "). Check service logs.";
                                    break;
                                }

                                cause = cause.getCause();
                            }

                            feedback.setText(detail);

                            show(
                                    Alert.AlertType.ERROR,
                                    "Payment not completed",
                                    detail
                            );

                        } else if (result == null) {

                            btnPay.setDisable(false);
                            feedback.setText(
                                    "Payment Service returned no response."
                            );

                        } else if (
                                "SUCCEEDED".equals(result.status())
                        ) {
                            feedback.setText("Payment succeeded. Saving your booking...");
                            EventServiceClient.registerAsync(accountId, event.id())
                                    .whenComplete((registration, registrationError) ->
                                            Platform.runLater(() -> {
                                                if (registrationError != null || registration == null) {
                                                    btnPay.setDisable(false);
                                                    feedback.setText(
                                                            "Payment succeeded, but your booking could not be saved. "
                                                                    + "You can retry to finish registration."
                                                    );
                                                    show(
                                                            Alert.AlertType.ERROR,
                                                            "Booking not saved",
                                                            "Your simulated payment succeeded, but the event service "
                                                                    + "could not save your booking. Retry to complete "
                                                                    + "registration; the successful payment will not be "
                                                                    + "charged again."
                                                    );
                                                    return;
                                                }

                                                feedback.setText(
                                                        "Booking confirmed. Reference: " + result.reference()
                                                );
                                                show(
                                                        Alert.AlertType.INFORMATION,
                                                        "Booking Confirmed",
                                                        "Payment ID: " + result.paymentId()
                                                                + "\nReference: " + result.reference()
                                                                + "\nAmount: " + result.currency() + " " + result.amount()
                                                                + "\n\nNo real charge occurred. Your booking is saved."
                                                );
                                            })
                                    );

                        } else {

                            btnPay.setDisable(false);

                            feedback.setText(
                                    "Simulated payment declined. " +
                                            "You may try again."
                            );

                            show(
                                    Alert.AlertType.WARNING,
                                    "Simulated Payment Declined",
                                    "No real charge occurred.\n" +
                                            "Uncheck the declined payment " +
                                            "test option to retry."
                            );
                        }
                    })
            );
        });

        stage.setTitle(
                "Secure Checkout - Simulation"
        );
        stage.setScene(
                new Scene(layout, 1366, 768)
        );
        stage.setResizable(false);
        stage.show();
    }

    private static RadioButton providerButton(
            String name,
            ToggleGroup group
    ) {
        RadioButton button = new RadioButton(name);
        button.setToggleGroup(group);
        button.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #444444;"
        );
        return button;
    }

    private static BorderPane summaryRow(
            String key,
            String value
    ) {
        BorderPane row = new BorderPane();

        Label left = new Label(key);
        left.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #666666;"
        );

        Label right = new Label(
                value == null ? "" : value
        );
        right.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #222222;" +
                        "-fx-font-weight: bold;"
        );

        row.setLeft(left);
        row.setRight(right);
        return row;
    }

    private static BorderPane totalRow(
            String key,
            String value
    ) {
        BorderPane row = summaryRow(key, value);

        ((Label) row.getLeft()).setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: #1a1a1a;" +
                        "-fx-font-weight: bold;"
        );

        ((Label) row.getRight()).setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-text-fill: #007bc4;" +
                        "-fx-font-weight: bold;"
        );

        return row;
    }

    private static void show(
            Alert.AlertType type,
            String title,
            String message
    ) {
        Alert dialog = new Alert(type);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        dialog.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}