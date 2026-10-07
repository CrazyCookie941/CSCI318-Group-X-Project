package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class DashboardUI extends Application
{

    /**
     * Creates a scalable visual card frame containing a responsive ImageView component
     */
    private StackPane createScalableImageCard(String path)
    {
        StackPane containerCard = new StackPane();
        containerCard.setMinSize(160, 110); // Floor safety boundary size
        containerCard.setPrefSize(240, 140); // Baseline launching size footprint
        containerCard.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); // Fluid scale limit caps removed

        // Tells the parent HBox carousel layout to expand this card container evenly
        HBox.setHgrow(containerCard, Priority.ALWAYS);

        ImageView imageView = new ImageView();

        // CRITICAL FIXES FOR FLUID RESIZING:
        // Bind the image view boundaries directly to its wrapper container's size updates
        imageView.fitWidthProperty().bind(containerCard.widthProperty());
        imageView.fitHeightProperty().bind(containerCard.heightProperty());
        imageView.setPreserveRatio(false); // Scales fully to match the fluid container dimensions

        containerCard.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 2);");

        try {
            Image image = new Image(getClass().getResource(path).toExternalForm());
            imageView.setImage(image);
        } catch (Exception e) {
            // Safe graphical error visual frame
            containerCard.setStyle(containerCard.getStyle() + "-fx-background-color: #ededed; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-background-radius: 4;");
        }

        containerCard.getChildren().add(imageView);
        return containerCard;
    }

    @Override
    public void start(Stage stage)
    {
        // 1. Root Layout Container
        BorderPane rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: #fafafa;");

        // 2. Styled Top Navigation Bar
        HBox navigationBar = Navigation.createNavigationBar(stage);
        rootLayout.setTop(navigationBar);

        // 3. LEFT COLUMN: Clean Sidebar Filters
        VBox filtersPane = new VBox(12);
        filtersPane.setPadding(new Insets(20, 16, 20, 16));
        filtersPane.setPrefWidth(160);
        filtersPane.setMinWidth(160);
        filtersPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 0 1 0 0;");

        Label filtersTitle = new Label("Filters");
        filtersTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222222; -fx-padding: 0 0 5 0;");

        ComboBox<String> locationBox = createStyledComboBox("Any location", "Wollongong", "Melbourne", "Sydney", "Brisbane");
        ComboBox<String> typeBox = createStyledComboBox("Any type", "Conference", "Workshop", "Concert");
        ComboBox<String> priceBox = createStyledComboBox("Any price", "Free", "Under $50", "Over $50");
        ComboBox<String> sizeBox = createStyledComboBox("Any size", "Small", "Medium", "Large");

        filtersPane.getChildren().addAll(
                filtersTitle,
                createFilterLabel("Location"), locationBox,
                createFilterLabel("Type"), typeBox,
                createFilterLabel("Price"), priceBox,
                createFilterLabel("Size"), sizeBox
        );
        rootLayout.setLeft(filtersPane);

        // 4. CENTRAL CONTENT PANEL
        VBox centerContentStack = new VBox(25);
        centerContentStack.setPadding(new Insets(25, 40, 25, 25));
        centerContentStack.setAlignment(Pos.TOP_LEFT);
        centerContentStack.setStyle("-fx-background-color: #fafafa;");

        // Fluid Search Bar Block
        HBox searchBarRow = new HBox(12);
        searchBarRow.setAlignment(Pos.CENTER_LEFT);
        searchBarRow.setMaxWidth(Double.MAX_VALUE);

        TextField searchField = new TextField();
        searchField.setPromptText("Search for amazing local events...");
        searchField.setPrefHeight(38);
        searchField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 4 0 0 4; -fx-background-radius: 4 0 0 4; -fx-padding: 4 12; -fx-font-size: 14px;");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button btnSearch = new Button("Search");
        btnSearch.setPrefHeight(38);
        btnSearch.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 0 20; -fx-background-radius: 0 4 4 0; -fx-cursor: hand;");

        searchBarRow.getChildren().addAll(searchField, btnSearch);

        // Fluid Carousel Container Block
        VBox carouselSection = new VBox(15);
        carouselSection.setAlignment(Pos.TOP_LEFT);
        carouselSection.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(carouselSection, Priority.ALWAYS);

        Label lblFeatured = new Label("Featured Events Near You");
        lblFeatured.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        HBox carouselContainer = new HBox(15);
        carouselContainer.setAlignment(Pos.CENTER_LEFT);
        carouselContainer.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(carouselContainer, Priority.ALWAYS); // Expand entire bar block horizontally

        Button previousButton = new Button("‹");
        Button nextButton = new Button("›");
        String arrowStyle = "-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 20; -fx-background-radius: 20; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #555555; -fx-pref-width: 36; -fx-pref-height: 36; -fx-min-width: 36; -fx-cursor: hand;";
        previousButton.setStyle(arrowStyle);
        nextButton.setStyle(arrowStyle);

        // Construct flexible container frames instead of standard fixed ImageViews
        StackPane card1 = createScalableImageCard("/Images/images.jpeg");
        StackPane card2 = createScalableImageCard("/Images/istockphoto-964136996-612x612.jpg");
        StackPane card3 = createScalableImageCard("/Images/event-ideas-for-party-eventbookings.jpeg");

        carouselContainer.getChildren().addAll(previousButton, card1, card2, card3, nextButton);
        carouselSection.getChildren().addAll(lblFeatured, carouselContainer);

        centerContentStack.getChildren().addAll(searchBarRow, carouselSection);

        // ScrollPane Setup Layer
        ScrollPane scrollWrapper = new ScrollPane(centerContentStack);
        scrollWrapper.setFitToWidth(true);
        scrollWrapper.setFitToHeight(true);
        scrollWrapper.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        scrollWrapper.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollWrapper.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        BorderPane.setMargin(scrollWrapper, new Insets(0, 15, 0, 0));
        rootLayout.setCenter(scrollWrapper);

        // 5. Stage Setup and Launch Sizing
        stage.setTitle("Event Discovery Dashboard");

        Scene scene = new Scene(rootLayout, 1024, 576);
        stage.setScene(scene);
        stage.setMinWidth(750);
        stage.setMinHeight(480);
        stage.show();
    }

    private static Label createFilterLabel(String text)
    {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #666666;");
        return label;
    }

    private static ComboBox<String> createStyledComboBox(String defaultVal, String... items)
    {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().add(defaultVal);
        cb.getItems().addAll(items);
        cb.setValue(defaultVal);
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setPrefHeight(30);
        cb.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 13px;");
        return cb;
    }

    public static void main(String[] args) { launch(args); }
}
