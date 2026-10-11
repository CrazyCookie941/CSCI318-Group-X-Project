package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

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
        btnRegister.setOnAction(action -> Navigation.goTo(primaryStage,
                nextStage -> new AttendeeDetailsUI(event).start(nextStage)));

        //Button btnBack = new Button("Back to Listings");
        //btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-padding: 10 20 10 20; -fx-cursor: hand;");
        //btnBack.setOnAction(event -> Navigation.goTo(primaryStage, nextStage -> new MyListingsEventGoer().start(nextStage)));

        actionButtons.getChildren().addAll(btnRegister);
        leftColumn.getChildren().addAll(headerTitle, infoGrid, descriptionBox, actionButtons);

        VBox rightColumn = new VBox(12);
        rightColumn.setAlignment(Pos.TOP_LEFT);

        Label lblImages = new Label("Gallery");
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

        Image img = loadImage(event.imageLocation());
        if (img != null) {
            eventImageView.setImage(img);
            imagePlaceholderText.setVisible(false);
        }

        VBox mapSection = new VBox(8);
        Label lblMap = new Label("Map Location");
        lblMap.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #666666;");

        if (event.latitude() != null && event.longitude() != null
                && Double.isFinite(event.latitude()) && Double.isFinite(event.longitude())
                && event.latitude() >= -90 && event.latitude() <= 90
                && event.longitude() >= -180 && event.longitude() <= 180) {
            WebView map = new WebView();
            map.setPrefSize(340, 220);
            map.setMinSize(340, 220);
            map.setMaxSize(340, 220);
            map.getEngine().loadContent(createMapHtml(event.latitude(), event.longitude(), event.location()));
            mapSection.getChildren().addAll(lblMap, map);
        } else {
            Label mapUnavailable = new Label("No verified map location is available for this event.");
            mapUnavailable.setWrapText(true);
            mapUnavailable.setPrefSize(340, 70);
            mapUnavailable.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 12; -fx-text-fill: #666666;");
            mapSection.getChildren().addAll(lblMap, mapUnavailable);
        }
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

        Image img = loadImage(item.imageLocation());
        if (img != null) {
            imageView.setImage(img);
        } else {
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

    private Image loadImage(String imageLocation) {
        if (imageLocation == null || imageLocation.isBlank()) {
            return null;
        }

        try {
            String imageUrl;
            if (imageLocation.startsWith("/")) {
                URL resource = getClass().getResource(imageLocation);
                if (resource == null) {
                    return null;
                }
                imageUrl = resource.toExternalForm();
            } else {
                Path imagePath = Path.of(imageLocation);
                if (!Files.isRegularFile(imagePath)) {
                    return null;
                }
                imageUrl = imagePath.toUri().toString();
            }

            Image image = new Image(imageUrl);
            return image.isError() ? null : image;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String createMapHtml(double latitude, double longitude, String location) {
        String safeLocation = location.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\u2028", "\\u2028")
                .replace("\u2029", "\\u2029")
                .replace("<", "\\u003C");
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css">
                    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                    <style>html, body, #map { width: 100%%; height: 100%%; margin: 0; }</style>
                </head>
                <body>
                    <div id="map"></div>
                    <script>
                        const map = L.map('map').setView([%s, %s], 15);
                        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            maxZoom: 19,
                            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                        }).addTo(map);
                        L.marker([%s, %s]).addTo(map).bindPopup('%s').openPopup();
                    </script>
                </body>
                </html>
                """.formatted(latitude, longitude, latitude, longitude, safeLocation);
    }

    public static void main(String[] args) {
        launch(args);
    }
}