package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class CreateEventUI extends Application
{
    @Override
    public void start(Stage primaryStage) {
        // 1. Root Layout Container
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f4f4;");

        // 2. Top Navigation Bar
        HBox navBar = new HBox();
        navBar.setPadding(new Insets(12, 24, 12, 24));
        navBar.setSpacing(20);
        navBar.setAlignment(Pos.CENTER_LEFT);
        navBar.setStyle("-fx-background-color: #1a1a1a;");

        Hyperlink homeLink = new Hyperlink("Home");
        Hyperlink eventsLink = new Hyperlink("My events");
        Hyperlink accountLink = new Hyperlink("Account");

        // Styling Navigation Links
        String navLinkStyle = "-fx-text-fill: white; -fx-underline: false; -fx-font-size: 14px; -fx-font-weight: bold;";
        homeLink.setStyle(navLinkStyle);
        eventsLink.setStyle(navLinkStyle);
        accountLink.setStyle(navLinkStyle);

        // Spacer to push the 'Account' link cleanly to the far right edge
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        navBar.getChildren().addAll(homeLink, eventsLink, rightSpacer, accountLink);
        root.setTop(navBar);

        // 3. Central Working Area Split (Left Form & Right Media)
        HBox mainContent = new HBox(40); // 40px spacing between left and right column
        mainContent.setPadding(new Insets(30, 40, 30, 40));
        mainContent.setAlignment(Pos.TOP_CENTER);

        // --- LEFT COLUMN: Form Context ---
        VBox leftColumn = new VBox(20);
        leftColumn.setMinWidth(450);

        Label headerTitle = new Label("Create an event listing");
        headerTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        // Grid Container for aligning form fields perfectly
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setAlignment(Pos.TOP_LEFT);

        // Define distinct widths for labels and fields to look uniform
        ColumnConstraints labelColumn = new ColumnConstraints(120);
        ColumnConstraints fieldColumn = new ColumnConstraints(300);
        formGrid.getColumnConstraints().addAll(labelColumn, fieldColumn);

        // Event Type Control
        Label lblType = new Label("Event type:");
        ComboBox<String> cbEventType = new ComboBox<>();
        cbEventType.getItems().addAll("Concert", "Conference", "Exhibition", "Festival", "Workshop");
        cbEventType.setPromptText("Select event type...");
        cbEventType.setMaxWidth(Double.MAX_VALUE);

        // Event Location Control
        Label lblLocation = new Label("Event location:");
        TextField txtLocation = new TextField();
        txtLocation.setPromptText("Enter venue or online address");

        // Event Price Control
        Label lblPrice = new Label("Event price:");
        TextField txtPrice = new TextField();
        txtPrice.setPromptText("e.g. Free or $25.00");

        // Event Size Control
        Label lblSize = new Label("Event size:");
        TextField txtSize = new TextField();
        txtSize.setPromptText("Maximum attendee capacity");

        // Other Information Control
        Label lblInfo = new Label("Other information:");
        TextArea txtOtherInfo = new TextArea();
        txtOtherInfo.setPromptText("Provide any extra essential details for attendees...");
        txtOtherInfo.setPrefRowCount(4);
        txtOtherInfo.setWrapText(true);

        // Assign controls sequentially into the grid layout
        formGrid.add(lblType, 0, 0);
        formGrid.add(cbEventType, 1, 0);

        formGrid.add(lblLocation, 0, 1);
        formGrid.add(txtLocation, 1, 1);

        formGrid.add(lblPrice, 0, 2);
        formGrid.add(txtPrice, 1, 2);

        formGrid.add(lblSize, 0, 3);
        formGrid.add(txtSize, 1, 3);

        formGrid.add(lblInfo, 0, 4);
        formGrid.add(txtOtherInfo, 1, 4);

        // Submission Button Action
        Button btnSubmit = new Button("Create Listing");
        btnSubmit.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20 10 20; -fx-background-radius: 4;");

        leftColumn.getChildren().addAll(headerTitle, formGrid, btnSubmit);

        // --- RIGHT COLUMN: Image Media Handling ---
        VBox rightColumn = new VBox(12);
        rightColumn.setAlignment(Pos.TOP_LEFT);

        Label lblPromoImage = new Label("Promotional images:");
        lblPromoImage.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #555555;");

        // Visual Placeholder representing image collection box
        VBox imagePlaceholderBox = new VBox();
        imagePlaceholderBox.setPrefSize(280, 240);
        imagePlaceholderBox.setStyle("-fx-border-color: #cccccc; -fx-border-style: dashed; -fx-border-width: 2; -fx-background-color: #e9e9e9; -fx-border-radius: 6; -fx-background-radius: 6;");
        imagePlaceholderBox.setAlignment(Pos.CENTER);

        // Icon presentation
        Label lblUploadIcon = new Label("📸");
        lblUploadIcon.setStyle("-fx-font-size: 32px;");
        Label lblUploadText = new Label("Click to upload event image");
        lblUploadText.setStyle("-fx-text-fill: #777777; -fx-font-size: 12px;");

        imagePlaceholderBox.getChildren().addAll(lblUploadIcon, lblUploadText);
        rightColumn.getChildren().addAll(lblPromoImage, imagePlaceholderBox);

        // Combine left column layout and right column layout together inside center container
        mainContent.getChildren().addAll(leftColumn, rightColumn);
        root.setCenter(mainContent);

        // 4. Initialization of Scene
        Scene scene = new Scene(root, 880, 520);
        primaryStage.setTitle("Event Management Workspace");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}