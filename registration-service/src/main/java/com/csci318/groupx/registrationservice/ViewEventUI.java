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

public class ViewEventUI extends Application {
    private final EventListing event;

    public ViewEventUI() {
        this(EventCatalog.getEvents().get(2));
    }

    public ViewEventUI(EventListing event) {
        this.event = event;
    }

    @Override
    public void start(Stage primaryStage) {
        // 1. Root Layout Container
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #fafafa;"); // Clean, bright workspace backdrop

        // 2. Top Navigation Bar
        HBox navBar = Navigation.createNavigationBar(primaryStage);
        root.setTop(navBar);

        // 3. Central Viewing Container Split
        HBox mainContent = new HBox(50); // Generous spacing between columns
        mainContent.setPadding(new Insets(35, 45, 35, 45));
        mainContent.setAlignment(Pos.TOP_CENTER);

        // --- LEFT COLUMN: Read-Only Event Information ---
        VBox leftColumn = new VBox(25);
        leftColumn.setMinWidth(420);

        Label headerTitle = new Label(event.title());
        headerTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        // Structured display grid for the information fields
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(18);

        ColumnConstraints labelColumn = new ColumnConstraints(120);
        ColumnConstraints valueColumn = new ColumnConstraints(280);
        infoGrid.getColumnConstraints().addAll(labelColumn, valueColumn);

        // Style strings to separate data labels from factual values cleanly
        String labelStyle = "-fx-font-size: 14px; -fx-text-fill: #666666; -fx-font-weight: bold;";
        String valueStyle = "-fx-font-size: 15px; -fx-text-fill: #111111; -fx-font-weight: normal;";

        // Event Type
        Label lblType = new Label("Event type:");
        lblType.setStyle(labelStyle);
        Label valType = new Label(event.type());
        valType.setStyle(valueStyle);

        // Event Location
        Label lblLocation = new Label("Event location:");
        lblLocation.setStyle(labelStyle);
        Label valLocation = new Label(event.location());
        valLocation.setStyle(valueStyle);

        // Event Price
        Label lblPrice = new Label("Event price:");
        lblPrice.setStyle(labelStyle);
        Label valPrice = new Label(event.price());
        valPrice.setStyle(valueStyle);

        // Event Size
        Label lblSize = new Label("Event size:");
        lblSize.setStyle(labelStyle);
        Label valSize = new Label(event.size());
        valSize.setStyle(valueStyle);

        // Map components sequentially to the presentation grid
        infoGrid.add(lblType, 0, 0);
        infoGrid.add(valType, 1, 0);
        infoGrid.add(lblLocation, 0, 1);
        infoGrid.add(valLocation, 1, 1);
        infoGrid.add(lblPrice, 0, 2);
        infoGrid.add(valPrice, 1, 2);
        infoGrid.add(lblSize, 0, 3);
        infoGrid.add(valSize, 1, 3);

        // Separate container for multi-line description text block
        VBox descriptionBox = new VBox(8);
        Label lblInfo = new Label("Other information:");
        lblInfo.setStyle(labelStyle);

        Label valInfo = new Label(event.description());
        valInfo.setWrapText(true);
        valInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-line-spacing: 4;");
        // Styled container panel behind text for readability
        valInfo.setPadding(new Insets(12));
        valInfo.setStyle(valInfo.getStyle() + "-fx-background-color: #ededed; -fx-background-radius: 6;");
        valInfo.setMaxWidth(400);

        descriptionBox.getChildren().addAll(lblInfo, valInfo);

        // Navigation Footer Action Buttons appropriate for viewing an item
        HBox actionButtons = new HBox(15);
        Button btnRegister = new Button("Register for Event");
        btnRegister.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20 10 20; -fx-background-radius: 4; -fx-cursor: hand;");
        btnRegister.setOnAction(event -> Navigation.goTo(primaryStage, nextStage -> new TransactionUI().start(nextStage)));

        Button btnBack = new Button("Back to Listings");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-padding: 10 20 10 20; -fx-cursor: hand;");
        btnBack.setOnAction(event -> Navigation.goTo(primaryStage, nextStage -> new MyListingsEventGoer().start(nextStage)));

        actionButtons.getChildren().addAll(btnRegister, btnBack);
        leftColumn.getChildren().addAll(headerTitle, infoGrid, descriptionBox, actionButtons);

        // --- RIGHT COLUMN: Image Graphic Presentation ---
        VBox rightColumn = new VBox(12);
        rightColumn.setAlignment(Pos.TOP_LEFT);

        Label lblImages = new Label("Images:");
        lblImages.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #666666;");

        // Graphical Image Element Container
        StackPane imageFrame = new StackPane();
        imageFrame.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-background-color: #f0f0f0; -fx-background-radius: 8; -fx-border-radius: 8;");
        imageFrame.setPrefSize(340, 240);

        ImageView eventImageView = new ImageView();
        eventImageView.setFitWidth(338);
        eventImageView.setFitHeight(238);
        eventImageView.setPreserveRatio(false); // Fills frame box area uniformly

        Label imagePlaceholderText = new Label("No event image available");
        imagePlaceholderText.setStyle("-fx-text-fill: #999999; -fx-font-style: italic;");
        imageFrame.getChildren().addAll(imagePlaceholderText, eventImageView);

        try {
            Image img = event.imageLocation().startsWith("/")
                    ? new Image(getClass().getResource(event.imageLocation()).toExternalForm())
                    : new Image(java.nio.file.Path.of(event.imageLocation()).toUri().toString());
            eventImageView.setImage(img);
            imagePlaceholderText.setVisible(false);
        } catch (IllegalArgumentException e) {
            imagePlaceholderText.setVisible(true);
        }

        rightColumn.getChildren().addAll(lblImages, imageFrame);

        // Combine left information data side and right graphical image panels
        mainContent.getChildren().addAll(leftColumn, rightColumn);
        root.setCenter(mainContent);

        // 4. Set Scene
        Scene scene = new Scene(root, 900, 540);
        primaryStage.setTitle("Event details workspace view");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}