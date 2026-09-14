package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

// Displays a modern secure transaction checkout screen matching the ecosystem's design tokens
public class TransactionUI extends Application
{

    @Override
    public void start(Stage stage)
    {
        // 1. Root Layout Container
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: #fafafa;"); // Modern light canvas backdrop

        // 2. Styled Top Navigation Bar
        HBox navigationBar = new HBox();
        navigationBar.setPadding(new Insets(12, 24, 12, 24));
        navigationBar.setSpacing(20);
        navigationBar.setAlignment(Pos.CENTER_LEFT);
        navigationBar.setStyle("-fx-background-color: #1a1a1a;");

        Hyperlink homeLink = createNavigationLink("Home");
        Hyperlink eventsLink = createNavigationLink("My events");
        Hyperlink accountLink = createNavigationLink("Account");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        navigationBar.getChildren().addAll(homeLink, eventsLink, spacer, accountLink);
        layout.setTop(navigationBar);

        // 3. Central Working Content Pane (Form Left, Order Summary Right)
        HBox mainContent = new HBox(45);
        mainContent.setPadding(new Insets(35, 45, 35, 45));
        mainContent.setAlignment(Pos.TOP_CENTER);

        // --- LEFT COLUMN: Secure Billing Information ---
        VBox leftColumn = new VBox(25);
        leftColumn.setMinWidth(460);

        Label title = new Label("Secure Checkout");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        // Structured inputs layout grid
        GridPane checkoutGrid = new GridPane();
        checkoutGrid.setHgap(20);
        checkoutGrid.setVgap(18);
        checkoutGrid.setAlignment(Pos.TOP_LEFT);

        ColumnConstraints labelColumn = new ColumnConstraints(130);
        ColumnConstraints fieldColumn = new ColumnConstraints(290);
        checkoutGrid.getColumnConstraints().addAll(labelColumn, fieldColumn);

        // Add core standard billing text inputs
        addFormField(checkoutGrid, 0, "Cardholder Name:", new TextField(), "John Citizen");
        addFormField(checkoutGrid, 1, "Card Number:", new TextField(), "4111 2222 3333 4444");

        // Expiry & CVV combined inline sub-row block
        Label lblExpiration = new Label("Expiration Date:");
        lblExpiration.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #666666;");

        HBox expiryAndCvvRow = new HBox(12);
        expiryAndCvvRow.setAlignment(Pos.CENTER_LEFT);

        TextField txtExpiry = new TextField();
        txtExpiry.setPromptText("MM/YY");
        txtExpiry.setPrefWidth(100);
        txtExpiry.setPrefHeight(34);
        txtExpiry.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 10; -fx-font-size: 14px;");

        TextField txtCvv = new TextField();
        txtCvv.setPromptText("CVV");
        txtCvv.setPrefWidth(80);
        txtCvv.setPrefHeight(34);
        txtCvv.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 10; -fx-font-size: 14px;");

        Label secureShieldIcon = new Label("🔒 Safe");
        secureShieldIcon.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 13px; -fx-font-weight: bold;");

        expiryAndCvvRow.getChildren().addAll(txtExpiry, txtCvv, secureShieldIcon);
        checkoutGrid.add(lblExpiration, 0, 2);
        checkoutGrid.add(expiryAndCvvRow, 1, 2);

        // Action Buttons corresponding to the workspace context
        HBox actionButtons = new HBox(15);
        actionButtons.setPadding(new Insets(10, 0, 0, 0));

        Button btnPay = new Button("Complete Payment");
        btnPay.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 11 26 11 26; -fx-background-radius: 4; -fx-font-size: 14px; -fx-cursor: hand;");
        btnPay.setOnMouseEntered(e -> btnPay.setStyle("-fx-background-color: #005fa3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 11 26 11 26; -fx-background-radius: 4; -fx-font-size: 14px; -fx-cursor: hand;"));
        btnPay.setOnMouseExited(e -> btnPay.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 11 26 11 26; -fx-background-radius: 4; -fx-font-size: 14px; -fx-cursor: hand;"));

        Button btnCancel = new Button("Cancel");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-padding: 11 24 11 24; -fx-font-size: 14px; -fx-cursor: hand;");

        actionButtons.getChildren().addAll(btnPay, btnCancel);
        leftColumn.getChildren().addAll(title, checkoutGrid, actionButtons);

        // --- RIGHT COLUMN: Order Summary Metadata Panel ---
        VBox rightColumn = new VBox(15);
        rightColumn.setMinWidth(300);
        rightColumn.setPadding(new Insets(50, 0, 0, 0));

        Label lblSummaryTitle = new Label("Order Summary");
        lblSummaryTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #666666;");

        // Clean metadata information overview block matching settings drawer formats
        VBox summaryCard = new VBox(14);
        summaryCard.setPadding(new Insets(20));
        summaryCard.setStyle("-fx-background-color: #f7f7f7; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6;");

        summaryCard.getChildren().addAll(
                createSummaryRow("Event Item:", "Radiohead Concert Tickets"),
                createSummaryRow("Quantity:", "2x General Admission"),
                createSummaryRow("Booking ID:", "#TXN-9841203"),
                new Separator(),
                createSummaryRow("Subtotal:", "$100.00"),
                createSummaryRow("Service Fee:", "$5.00"),
                createTotalRow("Total Amount:", "$105.00")
        );

        rightColumn.getChildren().addAll(lblSummaryTitle, summaryCard);

        // Combine layout tracks smoothly
        mainContent.getChildren().addAll(leftColumn, rightColumn);
        layout.setCenter(mainContent);

        // 4. Scroll Wrapper Integration (Safeguards against structural clipping)
        ScrollPane scrollWrapper = new ScrollPane(mainContent);
        scrollWrapper.setFitToWidth(true);
        scrollWrapper.setFitToHeight(true);
        scrollWrapper.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollWrapper.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollWrapper.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        BorderPane.setMargin(scrollWrapper, new Insets(0, 15, 0, 0));
        layout.setCenter(scrollWrapper);

        // 5. Stage Initialization Setup
        stage.setTitle("Secure Payment Gateway");
        stage.setScene(new Scene(layout, 1020, 560));
        stage.setResizable(false);
        stage.show();
    }

    private static Hyperlink createNavigationLink(String text)
    {
        Hyperlink link = new Hyperlink(text);
        link.setStyle("-fx-text-fill: white; -fx-underline: false; -fx-font-size: 14px; -fx-font-weight: bold;");
        return link;
    }

    private static void addFormField(GridPane grid, int row, String labelText, TextField field, String prompt)
    {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-font-weight: bold;");

        field.setPromptText(prompt);
        field.setMaxWidth(Double.MAX_VALUE);
        field.setPrefHeight(34);
        field.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 10; -fx-font-size: 14px;");

        grid.add(label, 0, row);
        grid.add(field, 1, row);
    }

    private static BorderPane createSummaryRow(String labelText, String valText)
    {
        BorderPane row = new BorderPane();
        Label leftLabel = new Label(labelText);
        leftLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");
        Label rightLabel = new Label(valText);
        rightLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #222222; -fx-font-weight: bold;");
        row.setLeft(leftLabel);
        row.setRight(rightLabel);
        return row;
    }

    private static BorderPane createTotalRow(String labelText, String valText)
    {
        BorderPane row = new BorderPane();
        Label leftLabel = new Label(labelText);
        leftLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #1a1a1a; -fx-font-weight: bold;");
        Label rightLabel = new Label(valText);
        rightLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #007bc4; -fx-font-weight: bold;");
        row.setLeft(leftLabel);
        row.setRight(rightLabel);
        return row;
    }

    public static void main(String[] args) { launch(args); }
}
