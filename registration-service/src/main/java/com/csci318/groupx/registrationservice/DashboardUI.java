package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

//displays event discovery controls, navigation, and event images
public class DashboardUI extends Application
{

    //loads an image resource and applies the standard event image size
    private ImageView createImageView(String path)
    {
        Image image = new Image(getClass().getResource(path).toExternalForm());

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(320);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);

        return imageView;
    }
    @Override
    public void start(Stage stage)
    {
        //build the main navigation bar
        Button homeButton = new Button("Home");
        Button eventsButton = new Button("My events");
        Button accountButton = new Button("Account");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox navigationBar = new HBox(30, homeButton, eventsButton, spacer, accountButton);
        navigationBar.setPadding(new Insets(10));
        navigationBar.setStyle("-fx-background-color: black;");

        homeButton.setStyle("-fx-text-fill: white; -fx-background-color: black;");
        eventsButton.setStyle("-fx-text-fill: white; -fx-background-color: black;");
        accountButton.setStyle("-fx-text-fill: white; -fx-background-color: black;");

        //provide the available event filters shown in the sidebar
        ComboBox<String> locationBox = new ComboBox<>();
        locationBox.getItems().addAll("Any location", "Wollongong", "Melbourne", "Sydney", "Brisbane");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Any type", "Conference", "Workshop", "Concert");

        ComboBox<String> priceBox = new ComboBox<>();
        priceBox.getItems().addAll("Any price", "Free", "Under $50", "Over $50");

        ComboBox<String> sizeBox = new ComboBox<>();
        sizeBox.getItems().addAll("Any size", "Small", "Medium", "Large");

        Label filtersTitle = new Label("Filters");
        Label locationLabel = new Label("Location");
        Label typeLabel = new Label("Type");
        Label priceLabel = new Label("Price");
        Label sizeLabel = new Label("Size");

        for (Label label : new Label[]{filtersTitle, locationLabel, typeLabel, priceLabel, sizeLabel}) { label.setStyle("-fx-text-fill: white;"); }

        for (ComboBox<?> comboBox : new ComboBox<?>[]{locationBox, typeBox, priceBox, sizeBox})
        {
            comboBox.setPrefWidth(100);
            comboBox.setMaxWidth(Double.MAX_VALUE);
            comboBox.setStyle("-fx-background-color: white;" + "-fx-border-color: #999999;" + "-fx-border-width: 1;");
        }

        VBox filters = new VBox(
                6,
                filtersTitle,
                locationLabel, locationBox,
                typeLabel, typeBox,
                priceLabel, priceBox,
                sizeLabel, sizeBox
        );

        filters.setPadding(new Insets(12, 13, 14, 15));
        filters.setPrefWidth(120);
        filters.setStyle("-fx-background-color: black;");

        //draw the search icon from simple JavaFX shapes
        Pane searchIcon = new Pane();
        searchIcon.setPrefSize(45, 48);

        Circle circle = new Circle(17, 180, 13);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);

        Line handle = new Line(25.5, 190, 40, 200);
        handle.setStroke(Color.BLACK);
        handle.setStrokeWidth(2);

        searchIcon.getChildren().addAll(circle, handle);

        searchIcon.setPrefSize(45, 48);

        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.setPrefWidth(480);
        searchField.setPrefHeight(48);
        searchField.setStyle("-fx-background-color: black;" + "-fx-text-fill: white;" + "-fx-prompt-text-fill: white;" + "-fx-font-size: 14px;");



        //load the event images displayed in the dashboard carousel area
        ImageView image1 = createImageView("/Images/images.jpeg");
        ImageView image2 = createImageView("/Images/istockphoto-964136996-612x612.jpg");
        ImageView image3 = createImageView("/Images/event-ideas-for-party-eventbookings.jpeg");

        Button previousButton = new Button("<");
        Button nextButton = new Button(">");

        HBox eventImages = new HBox(
                30,
                previousButton, image1, image2, image3, nextButton
        );

        eventImages.setAlignment(Pos.CENTER);
        eventImages.setPadding(new Insets(20, 0, 10, 0));


        HBox searchBar = new HBox(searchIcon, searchField);
        searchBar.setAlignment(Pos.CENTER);


        //arrange navigation, filters, search, and event images in the page
        BorderPane layout = new BorderPane();
        layout.setTop(navigationBar);
        layout.setLeft(filters);
        layout.setCenter(searchBar);
        layout.setPadding(new Insets(18, 14, 10, 14));
        layout.setBottom(eventImages);

        BorderPane.setMargin(filters, new Insets(19, 10, 0, 0));
        BorderPane.setMargin(searchBar, new Insets(50, 0, 0, 0));

        stage.setTitle("Dashboard");
        stage.setScene(new Scene(layout, 600, 350));
        stage.show();
    }
}