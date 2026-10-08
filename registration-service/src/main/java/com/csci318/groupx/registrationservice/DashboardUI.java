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

import java.util.Objects;

public class DashboardUI extends Application
{

    /**
     * Creates a scalable visual card frame containing a responsive ImageView component
     */
    private StackPane createEventCard(EventListing event, Stage stage)
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
            Image image = loadImage(event.imageLocation());
            imageView.setImage(image);
        } catch (IllegalArgumentException e) {
            // Safe graphical error visual frame
            containerCard.setStyle(containerCard.getStyle() + "-fx-background-color: #ededed; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-background-radius: 4;");
        }

        Label title = new Label(event.title());
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        title.setStyle("-fx-background-color: rgba(0,0,0,0.72); -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-padding: 8; -fx-font-size: 13px;");
        StackPane.setAlignment(title, Pos.BOTTOM_CENTER);
        containerCard.getChildren().add(imageView);
        containerCard.getChildren().add(title);
        containerCard.setOnMouseClicked(click -> Navigation.goTo(stage,
                nextStage -> new ViewEventUI(event).start(nextStage)));
        containerCard.setStyle(containerCard.getStyle() + "-fx-cursor: hand;");
        return containerCard;
    }

    /**
     * Generates a clean vertical list layout container for rendering matching queries
     */
    private VBox createSearchResultsSection(java.util.List<EventListing> events, Stage stage) {
        VBox searchResultsSection = new VBox(15);
        searchResultsSection.setPadding(new Insets(10, 0, 0, 0));
        searchResultsSection.setAlignment(Pos.TOP_LEFT);
        searchResultsSection.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(searchResultsSection, Priority.ALWAYS);

        Label searchTitle = new Label("Search Results");
        searchTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        VBox resultsListContainer = new VBox(12);
        resultsListContainer.setMaxWidth(Double.MAX_VALUE);

        // Map live catalog entries smoothly into individual left-aligned row layouts
        for (EventListing event : events) {
            resultsListContainer.getChildren().add(createSearchResultRow(event, stage));
        }

        searchResultsSection.getChildren().addAll(searchTitle, resultsListContainer);
        return searchResultsSection;
    }

    /**
     * Builds a left-aligned row wrapper structure utilizing an identical card theme layout
     */
    private BorderPane createSearchResultRow(EventListing event, Stage stage) {
        BorderPane rowCard = new BorderPane();
        rowCard.setPadding(new Insets(14, 20, 14, 20));
        rowCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.01), 4, 0, 0, 1);");
        rowCard.setMaxWidth(Double.MAX_VALUE);

        // Left Partition Text Layout
        VBox textStack = new VBox(4);
        textStack.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(event.title());
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        Label detailsLabel = new Label("Category Tiers: " + event.type() + " • Real-time Admission Portals Open");
        detailsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        textStack.getChildren().addAll(titleLabel, detailsLabel);

        HBox leftWrapper = new HBox(textStack);
        leftWrapper.setAlignment(Pos.CENTER_LEFT);
        rowCard.setLeft(leftWrapper); // Anchors heading text elements strictly against the absolute left edge

        // Right Partition View Action Trigger Layout
        Button btnView = new Button("View Listing");
        btnView.setPrefWidth(110);
        btnView.setPrefHeight(32);
        btnView.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");

        btnView.setOnMouseEntered(e -> btnView.setStyle("-fx-background-color: #005fa3; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;"));
        btnView.setOnMouseExited(e -> btnView.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;"));

        btnView.setOnAction(eventAction -> Navigation.goTo(stage, nextStage -> new ViewEventUI(event).start(nextStage)));

        HBox rightWrapper = new HBox(btnView);
        rightWrapper.setAlignment(Pos.CENTER_RIGHT);
        rowCard.setRight(rightWrapper); // Anchors primary button actions strictly against the absolute right edge

        return rowCard;
    }

    private Image loadImage(String location)
    {
        if (location.startsWith("/")) {
            return new Image(Objects.requireNonNull(getClass().getResource(location)).toExternalForm());
        }
        return new Image(java.nio.file.Path.of(location).toUri().toString());
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

        ComboBox<String> locationBox = createStyledComboBox("Any location", "Perth", "Melbourne", "Sydney", "Brisbane", "Adelaide", "Darwin", "Hobart");
        ComboBox<String> typeBox = createStyledComboBox("Any type", "Conference", "Workshop", "Concert", "Nightclub", "Exhibition", "Community", "Games", "Festival", "Other");
        ComboBox<String> priceBox = createStyledComboBox("Any price", "Free", "Under $50", "Over $50");

        filtersPane.getChildren().addAll(
                filtersTitle,
                createFilterLabel("Location"), locationBox,
                createFilterLabel("Type"), typeBox,
                createFilterLabel("Price"), priceBox
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

        // Construct flexible container frames instead of standard fixed ImageViews
        java.util.List<EventListing> events = EventCatalog.getEvents();
        StackPane card1 = createEventCard(events.get(0), stage);
        StackPane card2 = createEventCard(events.get(1), stage);
        StackPane card3 = createEventCard(events.get(2), stage);
        StackPane card4 = createEventCard(events.get(3), stage);

        carouselContainer.getChildren().addAll(card1, card2, card3, card4);
        ScrollPane eventScroller = new ScrollPane(carouselContainer);
        eventScroller.setFitToHeight(true);
        eventScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        eventScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        eventScroller.setPrefViewportHeight(150);
        eventScroller.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        carouselSection.getChildren().addAll(lblFeatured, eventScroller);
        // Add only the default visible items to the layout container view on launch
        centerContentStack.getChildren().addAll(searchBarRow, carouselSection);

        // Extract the UI container holding the rows from the wrapper so we can mutate its children
        VBox searchResultsSection = createSearchResultsSection(events, stage);
        // Find the container inside the searchResultsSection (the 2nd element added to it)
        VBox resultsListContainer = (VBox) searchResultsSection.getChildren().get(1);

        // Helper method to refresh calculations and redraw components
        Runnable updateSearchResults = () -> {
            String searchText = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
            String selectedLoc = locationBox.getValue();
            String selectedType = typeBox.getValue();
            // Feel free to pull in priceBox and sizeBox values here if your EventListing model supports them

            // 1. Filter the live master catalog
            java.util.List<EventListing> filteredEvents = EventCatalog.getEvents().stream()
                    .filter(event -> {
                        // Match Text Query (Title)
                        boolean matchesText = searchText.isEmpty() ||
                                event.title().toLowerCase().contains(searchText);

                        // Match Location Filter
                        boolean matchesLocation = "Any location".equals(selectedLoc) ||
                                event.location().equalsIgnoreCase(selectedLoc); // Assumes event.location() exists

                        // Match Type Filter
                        boolean matchesType = "Any type".equals(selectedType) ||
                                event.type().equalsIgnoreCase(selectedType);

                        return matchesText && matchesLocation && matchesType;
                    })
                    .toList();

            // 2. Clear old row view layouts out of DOM tree memory
            resultsListContainer.getChildren().clear();

            // 3. Rebuild updated elements mapping onto matching queries
            for (EventListing filteredEvent : filteredEvents) {
                resultsListContainer.getChildren().add(createSearchResultRow(filteredEvent, stage));
            }

            // 4. View State Toggle: Decides whether to display recommendation panels or results
            if (searchText.isEmpty() && "Any location".equals(selectedLoc) && "Any type".equals(selectedType)) {
                centerContentStack.getChildren().remove(searchResultsSection);
                if (!centerContentStack.getChildren().contains(carouselSection)) {
                    centerContentStack.getChildren().add(carouselSection);
                }
            } else {
                centerContentStack.getChildren().remove(carouselSection);
                if (!centerContentStack.getChildren().contains(searchResultsSection)) {
                    centerContentStack.getChildren().add(searchResultsSection);
                }
            }
        };

        // Attach the real-time listener updates to search inputs and filter changes
        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateSearchResults.run());
        locationBox.valueProperty().addListener((obs, oldVal, newVal) -> updateSearchResults.run());
        typeBox.valueProperty().addListener((obs, oldVal, newVal) -> updateSearchResults.run());
        priceBox.valueProperty().addListener((obs, oldVal, newVal) -> updateSearchResults.run());

        // Connect the "Search" button click event to also execute a query refresh action
        btnSearch.setOnAction(e -> updateSearchResults.run());

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

        Scene scene = new Scene(rootLayout, 1366, 768);
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
