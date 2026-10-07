package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
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
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #fafafa;");

        HBox navBar = Navigation.createNavigationBar(primaryStage);
        root.setTop(navBar);

        VBox pageContent = new VBox(28);
        pageContent.setPadding(new Insets(35, 45, 35, 45));

        HBox mainContent = new HBox(50);
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setMinHeight(520);

        VBox leftColumn = new VBox(25);
        leftColumn.setMinWidth(420);

        Label headerTitle = new Label(event.title());
        headerTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(18);

        ColumnConstraints labelColumn = new ColumnConstraints(120);
        ColumnConstraints valueColumn = new ColumnConstraints(280);
        infoGrid.getColumnConstraints().addAll(labelColumn, valueColumn);

        String labelStyle = "-fx-font-size: 14px; -fx-text-fill: #666666; -fx-font-weight: bold;";
        String valueStyle = "-fx-font-size: 15px; -fx-text-fill: #111111; -fx-font-weight: normal;";

        Label lblType = new Label("Event type:");
        lblType.setStyle(labelStyle);
        Label valType = new Label(event.type());
        valType.setStyle(valueStyle);

        Label lblLocation = new Label("Event location:");
        lblLocation.setStyle(labelStyle);
        Label valLocation = new Label(event.location());
        valLocation.setStyle(valueStyle);

        Label lblPrice = new Label("Event price:");
        lblPrice.setStyle(labelStyle);
        Label valPrice = new Label(event.price());
        valPrice.setStyle(valueStyle);

        Label lblSize = new Label("Event size:");
        lblSize.setStyle(labelStyle);
        Label valSize = new Label(event.size());
        valSize.setStyle(valueStyle);

        infoGrid.add(lblType, 0, 0);
        infoGrid.add(valType, 1, 0);
        infoGrid.add(lblLocation, 0, 1);
        infoGrid.add(valLocation, 1, 1);
        infoGrid.add(lblPrice, 0, 2);
        infoGrid.add(valPrice, 1, 2);
        infoGrid.add(lblSize, 0, 3);
        infoGrid.add(valSize, 1, 3);

        VBox descriptionBox = new VBox(8);
        Label lblInfo = new Label("Other information:");
        lblInfo.setStyle(labelStyle);

        Label valInfo = new Label(event.description());
        valInfo.setWrapText(true);
        valInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-line-spacing: 4;");
        valInfo.setPadding(new Insets(12));
        valInfo.setStyle(valInfo.getStyle() + "-fx-background-color: #ededed; -fx-background-radius: 6;");
        valInfo.setMaxWidth(400);

        descriptionBox.getChildren().addAll(lblInfo, valInfo);

        HBox actionButtons = new HBox(15);
        Button btnRegister = new Button("Register for Event");
        btnRegister.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20 10 20; -fx-background-radius: 4; -fx-cursor: hand;");
        btnRegister.setOnAction(event -> Navigation.goTo(primaryStage, nextStage -> new TransactionUI().start(nextStage)));

        Button btnBack = new Button("Back to Listings");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-padding: 10 20 10 20; -fx-cursor: hand;");
        btnBack.setOnAction(event -> Navigation.goTo(primaryStage, nextStage -> new MyListingsEventGoer().start(nextStage)));

        actionButtons.getChildren().addAll(btnRegister, btnBack);
        leftColumn.getChildren().addAll(headerTitle, infoGrid, descriptionBox, actionButtons);

        VBox rightColumn = new VBox(12);
        rightColumn.setAlignment(Pos.TOP_LEFT);

        Label lblImages = new Label("Images:");
        lblImages.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #666666;");

        StackPane imageFrame = new StackPane();
        imageFrame.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-background-color: #f0f0f0; -fx-background-radius: 8; -fx-border-radius: 8;");
        imageFrame.setPrefSize(340, 240);

        ImageView eventImageView = new ImageView();
        eventImageView.setFitWidth(338);
        eventImageView.setFitHeight(238);
        eventImageView.setPreserveRatio(false);

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

        VBox mapSection = new VBox(8);
        Label lblMap = new Label("Map:");
        lblMap.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #666666;");

        Pane mapPanel = new Pane();
        mapPanel.setPrefSize(340, 180);
        mapPanel.setMinSize(340, 180);
        mapPanel.setMaxSize(340, 180);
        mapPanel.setStyle("-fx-background-color: linear-gradient(to bottom, #eaf7ea, #dfeee6); -fx-border-color: #b7d7a8; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        mapPanel.setClip(new Rectangle(340, 180));

        mapPanel.getChildren().addAll(
                createRoadLine(0, 42, 340, 42, 8),
                createRoadLine(0, 105, 340, 105, 8),
                createRoadLine(72, 0, 72, 180, 7),
                createRoadLine(230, 0, 230, 180, 7),
                createRoadLine(-10, 155, 350, 15, 5),
                createLocationMarker(170, 87, event.location())
        );

        mapSection.getChildren().addAll(lblMap, mapPanel);
        rightColumn.getChildren().addAll(lblImages, imageFrame, mapSection);

        mainContent.getChildren().addAll(leftColumn, rightColumn);

        Label recTitle = new Label("Recommended listings");
        recTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        HBox recRow = new HBox(16);
        recRow.setAlignment(Pos.CENTER_LEFT);

        EventCatalog.getEvents().stream()
                .filter(item -> !item.title().equals(event.title()))
                .limit(3)
                .forEach(item -> recRow.getChildren().add(createRecommendationCard(item, primaryStage)));

        VBox recommendedSection = new VBox(12);
        recommendedSection.setPadding(new Insets(4, 0, 12, 0));
        recommendedSection.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 0;");
        recommendedSection.getChildren().addAll(recTitle, recRow);

        pageContent.getChildren().addAll(mainContent, recommendedSection);

        ScrollPane scrollPane = new ScrollPane(pageContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 1366, 768);
        primaryStage.setTitle("Event details workspace view");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private VBox createRecommendationCard(EventListing item, Stage stage) {
        VBox card = new VBox(8);
        card.setMinSize(200, 215);
        card.setPrefSize(200, 215);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 4, 0, 0, 2);");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(90);
        imageView.setPreserveRatio(false);

        try {
            Image img = item.imageLocation().startsWith("/")
                    ? new Image(getClass().getResource(item.imageLocation()).toExternalForm())
                    : new Image(java.nio.file.Path.of(item.imageLocation()).toUri().toString());
            imageView.setImage(img);
        } catch (IllegalArgumentException ignored) {
            imageView.setStyle("-fx-background-color: #eeeeee; -fx-border-color: #cccccc; -fx-border-radius: 6; -fx-background-radius: 6;");
        }

        Label title = new Label(item.title());
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        Label location = new Label(item.location());
        location.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        Button button = new Button("View");
        button.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12 6 12; -fx-background-radius: 4; -fx-cursor: hand;");
        button.setOnAction(event -> Navigation.goTo(stage, nextStage -> new ViewEventUI(item).start(nextStage)));

        card.getChildren().addAll(imageView, title, location, button);
        return card;
    }

    private Line createRoadLine(double startX, double startY, double endX, double endY, double width) {
        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(javafx.scene.paint.Color.web("#d2d5d3"));
        line.setStrokeWidth(width);
        line.setOpacity(0.8);
        return line;
    }

    private StackPane createLocationMarker(double x, double y, String labelText) {
        StackPane marker = new StackPane();
        marker.setLayoutX(x - 12);
        marker.setLayoutY(y - 12);
        marker.setPrefSize(24, 24);
        marker.setStyle("-fx-background-color: #d32f2f; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: white; -fx-border-width: 2;");

        Label pin = new Label("📍");
        pin.setStyle("-fx-font-size: 11px;");
        marker.getChildren().add(pin);

        Label locationLabel = new Label(labelText);
        locationLabel.setMaxWidth(150);
        locationLabel.setWrapText(true);
        locationLabel.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 4; -fx-padding: 3 6; -fx-font-size: 11px; -fx-text-fill: #444444; -fx-font-weight: bold;");
        locationLabel.setTranslateY(28);
        locationLabel.setTranslateX(38);
        marker.getChildren().add(locationLabel);

        return marker;
    }

    public static void main(String[] args) {
        launch(args);
    }
}