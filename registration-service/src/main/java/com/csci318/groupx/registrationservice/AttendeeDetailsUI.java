package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

// Displays the ticket details data entry screen matching the ecosystem's design tokens
public class AttendeeDetailsUI extends Application
{
    private final EventListing event;

    public AttendeeDetailsUI()
    {
        this(EventCatalog.getEvents().stream().findFirst().orElse(null));
    }

    public AttendeeDetailsUI(EventListing event)
    {
        this.event = event;
    }

    @Override
    public void start(Stage stage)
    {
        // 1. Root Layout Container
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: #fafafa;"); // Modern light canvas backdrop

        // 2. Styled Top Navigation Bar
        HBox navigationBar = Navigation.createNavigationBar(stage);
        layout.setTop(navigationBar);

        // 3. Central Working Content Pane (Form Left, Event Info Right)
        HBox mainContent = new HBox(45);
        mainContent.setPadding(new Insets(35, 45, 35, 45));
        mainContent.setAlignment(Pos.TOP_CENTER);

        // --- LEFT COLUMN: Attendee Registration Form ---
        VBox leftColumn = new VBox(25);
        leftColumn.setMinWidth(460);

        Label title = new Label("Admission Details");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        // Structured inputs grid
        GridPane formGrid = new GridPane();
        formGrid.setHgap(20);
        formGrid.setVgap(18);
        formGrid.setAlignment(Pos.TOP_LEFT);

        ColumnConstraints labelColumn = new ColumnConstraints(130);
        ColumnConstraints fieldColumn = new ColumnConstraints(290);
        formGrid.getColumnConstraints().addAll(labelColumn, fieldColumn);

        // Form Fields
        addFormField(formGrid, 0, "Full Name:", new TextField(), "Enter attendee name");
        addFormField(formGrid, 1, "Address:", new TextField(), "Enter address");
        addFormField(formGrid, 2, "Contact Phone:", new TextField(), "e.g. +61 400 000 000");

        // Ticket Type Dropdown selection
        Label lblTicketType = new Label("Ticket Option:");
        lblTicketType.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #666666;");

        ComboBox<String> cbTicket = new ComboBox<>();
        if (event != null) {
            String admission = "General Admission (" + event.price() + ")";
            cbTicket.getItems().add(admission);
            cbTicket.setValue(admission);
        } else {
            cbTicket.setPromptText("No event selected");
            cbTicket.setDisable(true);
        }
        cbTicket.setMaxWidth(Double.MAX_VALUE);
        cbTicket.setPrefHeight(34);
        cbTicket.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 13px;");

        formGrid.add(lblTicketType, 0, 3);
        formGrid.add(cbTicket, 1, 3);

        // Multi-line Special Request entry block
        Label lblRequirements = new Label("Other Notes:");
        lblRequirements.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #666666;");

        TextArea txtRequirements = new TextArea();
        txtRequirements.setPromptText("Please specify any dietary preferences, allergies, or accessibility considerations...");
        txtRequirements.setPrefRowCount(3);
        txtRequirements.setWrapText(true);
        txtRequirements.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 6; -fx-font-size: 14px;");

        formGrid.add(lblRequirements, 0, 4);
        formGrid.add(txtRequirements, 1, 4);

        // Action Buttons Grouping
        HBox actionButtons = new HBox(15);
        actionButtons.setPadding(new Insets(10, 0, 0, 0));

        Button btnProceed = new Button("Proceed to Payment");
        btnProceed.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 11 26 11 26; -fx-background-radius: 4; -fx-font-size: 14px; -fx-cursor: hand;");
        btnProceed.setOnMouseEntered(e -> btnProceed.setStyle("-fx-background-color: #005fa3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 11 26 11 26; -fx-background-radius: 4; -fx-font-size: 14px; -fx-cursor: hand;"));
        btnProceed.setOnMouseExited(e -> btnProceed.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 11 26 11 26; -fx-background-radius: 4; -fx-font-size: 14px; -fx-cursor: hand;"));
        btnProceed.setOnAction(action -> Navigation.goTo(stage,
                nextStage -> new TransactionUI(event).start(nextStage)));

        Button btnBack = new Button("Back");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-padding: 11 24 11 24; -fx-font-size: 14px; -fx-cursor: hand;");
        btnBack.setOnAction(event -> Navigation.goBack(stage));

        actionButtons.getChildren().addAll(btnProceed, btnBack);
        leftColumn.getChildren().addAll(title, formGrid, actionButtons);

        // --- RIGHT COLUMN: Selected Event Overview Panel ---
        VBox rightColumn = new VBox(15);
        rightColumn.setMinWidth(300);
        rightColumn.setPadding(new Insets(50, 0, 0, 0));

        Label lblSummaryTitle = new Label("Selected Event");
        lblSummaryTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #666666;");

        // Dynamic context review wrapper card matching settings page styling rules
        VBox summaryCard = new VBox(14);
        summaryCard.setPadding(new Insets(20));
        summaryCard.setStyle("-fx-background-color: #f7f7f7; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6;");

        if (event != null) {
            summaryCard.getChildren().addAll(
                    createSummaryRow("Event:", event.title()),
                    createSummaryRow("Venue:", event.location()),
                    createSummaryRow("Type:", event.type()),
                    createSummaryRow("Price:", event.price()),
                    createSummaryRow("Size:", event.size()),
                    new Separator(),
                    createSummaryRow("Details:", event.description())
            );
        } else {
            summaryCard.getChildren().add(createSummaryRow("Event:", "No event selected"));
        }

        rightColumn.getChildren().addAll(lblSummaryTitle, summaryCard);

        mainContent.getChildren().addAll(leftColumn, rightColumn);

        // 4. Scroll Wrapper Integration (Safeguards against window clipping)
        ScrollPane scrollWrapper = new ScrollPane(mainContent);
        scrollWrapper.setFitToWidth(true);
        scrollWrapper.setFitToHeight(true);
        scrollWrapper.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollWrapper.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollWrapper.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        BorderPane.setMargin(scrollWrapper, new Insets(0, 15, 0, 0));
        layout.setCenter(scrollWrapper);

        // 5. Stage Initialization Setup
        stage.setTitle("EventRunner AI Event Management Platform");
        stage.setScene(new Scene(layout, 1366, 768));
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

    // Foolproof BorderPane row constructor mapping values strictly left and right
    private static BorderPane createSummaryRow(String labelText, String valText)
    {
        BorderPane row = new BorderPane();
        Label leftLabel = new Label(labelText);
        leftLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");
        Label rightLabel = new Label(valText);
        rightLabel.setWrapText(true);
        rightLabel.setMaxWidth(160);
        rightLabel.setAlignment(Pos.CENTER_RIGHT);
        rightLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #222222; -fx-font-weight: bold;");
        row.setLeft(leftLabel);
        row.setRight(rightLabel);
        return row;
    }

    public static void main(String[] args) { launch(args); }
}
