package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

//displays and manages the events uploaded by the current user
public class MyListingsEventManager extends Application
{

    @Override
    public void start(Stage stage)
    {
        //create the page navigation bar
        Button homeButton = createNavigationButton("Home");
        Button eventsButton = createNavigationButton("My events");
        Button accountButton = createNavigationButton("Account");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox navigationBar = new HBox(30, homeButton, eventsButton, spacer, accountButton);
        navigationBar.setAlignment(Pos.CENTER_LEFT);
        navigationBar.setPadding(new Insets(10, 14, 10, 14));
        navigationBar.setStyle("-fx-background-color: black;");

        //display the page heading above the uploaded listings
        Label title = new Label("View your uploaded listings");
        title.setStyle("-fx-font-size: 24px; -fx-text-fill: black;");

        //create the listings shown on the page
        VBox listings = new VBox(
                19,
                createListing("Community\nbarbecue", "/Images/park-art-P1020661.jpg"),
                createListing("Radiohead\nconcert", "/Images/394f75e3cb710d620129ea9fef2754f83b5bc54b8e8f4ffcfc2badd46c297059.jpeg"),
                createListing("Nightclub", "/Images/Wikipedia_space_ibiza(03).jpg"),
                createListing("Bingo night", "/Images/Bingo_night.jpg")
        );
        listings.setPadding(new Insets(16, 0, 0, 0));

        VBox content = new VBox(12, title, listings);
        content.setPadding(new Insets(18, 0, 0, 0));

        //place the navigation bar and listing content into the main layout
        BorderPane layout = new BorderPane();
        layout.setTop(navigationBar);
        layout.setCenter(content);
        layout.setPadding(new Insets(27, 16, 20, 16));
        layout.setStyle("-fx-background-color: #d9d9d9;");

        stage.setTitle("My listings");
        stage.setScene(new Scene(layout, 640, 400));
        stage.show();
    }

    //creates a consistently styled navigation button
    private static Button createNavigationButton(String text)
    {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: black; -fx-text-fill: white; " + "-fx-font-size: 13px; -fx-padding: 0;");
        return button;
    }

    //creates one listing row with its image and management buttons
    private HBox createListing(String listingName, String imagePath)
    {
        //load and size the image associated with the listing
        ImageView imageView = new ImageView(new Image(getClass().getResource(imagePath).toExternalForm()));
        imageView.setFitWidth(44);
        imageView.setFitHeight(30);
        imageView.setPreserveRatio(false);

        Label name = new Label(listingName);
        name.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        name.setPrefWidth(380);

        //show a message when the user selects the edit action
        Button editButton = new Button("Edit\nlisting");
        editButton.setOnAction(event -> showMessage("Edit listing", "Editing " + listingName.replace("\n", " ")));

        //remove the listing row when the user selects the remove action
        Button removeButton = new Button("Remove\nlisting");
        removeButton.setOnAction(event -> {
            HBox listing = (HBox) removeButton.getParent();
            ((VBox) listing.getParent()).getChildren().remove(listing);
        });

        for (Button button : new Button[]{editButton, removeButton})
        {
            button.setStyle("-fx-background-color: black; -fx-text-fill: white; " + "-fx-font-size: 13px; -fx-padding: 0;");
            button.setPrefWidth(74);
            button.setAlignment(Pos.CENTER_LEFT);
        }

        //assemble the image, title, and action buttons into one row
        HBox listing = new HBox(10, imageView, name, editButton, removeButton);
        listing.setAlignment(Pos.CENTER_LEFT);
        listing.setPadding(new Insets(3, 8, 3, 10));
        listing.setPrefHeight(50);
        listing.setStyle("-fx-background-color: black;");
        return listing;
    }

    //displays feedback for listing actions
    private static void showMessage(String title, String message)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    //launches the JavaFX application
    public static void main(String[] args) { launch(args); }
}
