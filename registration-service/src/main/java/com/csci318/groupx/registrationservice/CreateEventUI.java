package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class CreateEventUI extends Application
{
    @Override
    public void start(Stage primaryStage) {
        // 1. Root Layout Container
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f4f4;");

        // 2. Top Navigation Bar
        HBox navBar = Navigation.createNavigationBar(primaryStage);
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

        Label lblTitle = new Label("Event name:");
        TextField txtTitle = new TextField();
        txtTitle.setPromptText("e.g. Radiohead concert");

        // Event Type Control
        Label lblType = new Label("Event type:");
        ComboBox<String> cbEventType = new ComboBox<>();
        cbEventType.getItems().addAll("Concert", "Nightclub", "Conference", "Exhibition", "Festival", "Workshop", "Community", "Games");
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
        formGrid.add(lblTitle, 0, 0);
        formGrid.add(txtTitle, 1, 0);

        formGrid.add(lblType, 0, 1);
        formGrid.add(cbEventType, 1, 1);

        formGrid.add(lblLocation, 0, 2);
        formGrid.add(txtLocation, 1, 2);

        formGrid.add(lblPrice, 0, 3);
        formGrid.add(txtPrice, 1, 3);

        formGrid.add(lblSize, 0, 4);
        formGrid.add(txtSize, 1, 4);

        formGrid.add(lblInfo, 0, 5);
        formGrid.add(txtOtherInfo, 1, 5);

        final String[] selectedImage = {null};

        // Submission Button Action
        Button btnSubmit = new Button("Create Listing");
        btnSubmit.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20 10 20; -fx-background-radius: 4;");

        btnSubmit.setOnAction(event -> {
            if (txtTitle.getText().isBlank() || cbEventType.getValue() == null
                    || txtLocation.getText().isBlank()) {
                new Alert(Alert.AlertType.WARNING, "Please provide an event name, type, and location.").showAndWait();
                return;
            }

            try {
                String imageLocation = copyUploadedImage(selectedImage[0]);
                EventListing listing = new EventListing(
                        txtTitle.getText().trim(),
                        cbEventType.getValue(),
                        txtLocation.getText().trim(),
                        txtPrice.getText().isBlank() ? "Free" : txtPrice.getText().trim(),
                        txtSize.getText().isBlank() ? "Not specified" : txtSize.getText().trim(),
                        txtOtherInfo.getText().isBlank() ? "No additional information provided." : txtOtherInfo.getText().trim(),
                        imageLocation);
                EventCatalog.add(listing);
                Navigation.goTo(primaryStage, nextStage -> new ViewEventUI(listing).start(nextStage));
            } catch (IOException exception) {
                new Alert(Alert.AlertType.ERROR, "The photo could not be uploaded: " + exception.getMessage()).showAndWait();
            }
        });

        leftColumn.getChildren().addAll(headerTitle, formGrid, btnSubmit);

        // --- RIGHT COLUMN: Image Media Handling ---
        VBox rightColumn = new VBox(12);
        rightColumn.setAlignment(Pos.TOP_LEFT);

        Label lblPromoImage = new Label("Promotional images:");
        lblPromoImage.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #555555;");

        VBox imagePlaceholderBox = new VBox();
        imagePlaceholderBox.setPrefSize(280, 240);
        imagePlaceholderBox.setStyle("-fx-border-color: #cccccc; -fx-border-style: dashed; -fx-border-width: 2; -fx-background-color: #e9e9e9; -fx-border-radius: 6; -fx-background-radius: 6;");
        imagePlaceholderBox.setAlignment(Pos.CENTER);

        // Icon presentation
        Label lblUploadIcon = new Label("Image");
        lblUploadIcon.setStyle("-fx-font-size: 32px;");
        Label lblUploadText = new Label("Choose an event image");
        lblUploadText.setStyle("-fx-text-fill: #777777; -fx-font-size: 12px;");

        imagePlaceholderBox.getChildren().addAll(lblUploadIcon, lblUploadText);
        Button uploadButton = new Button("Choose photo");
        uploadButton.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose an event photo");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                    "Image files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            java.io.File chosenFile = chooser.showOpenDialog(primaryStage);
            if (chosenFile != null) {
                selectedImage[0] = chosenFile.getAbsolutePath();
                ImageView preview = new ImageView(new Image(chosenFile.toURI().toString()));
                preview.setFitWidth(260);
                preview.setFitHeight(180);
                preview.setPreserveRatio(true);
                imagePlaceholderBox.getChildren().setAll(preview, new Label(chosenFile.getName()));
            }
        });
        rightColumn.getChildren().addAll(lblPromoImage, imagePlaceholderBox, uploadButton);

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

    private String copyUploadedImage(String selectedImage) throws IOException {
        if (selectedImage == null) {
            return "/Images/event-ideas-for-party-eventbookings.jpeg";
        }
        Path uploadDirectory = Path.of(System.getProperty("user.home"), ".eventguide", "uploads");
        Files.createDirectories(uploadDirectory);
        Path source = Path.of(selectedImage);
        String storedName = UUID.randomUUID() + "-" + source.getFileName();
        Path destination = uploadDirectory.resolve(storedName);
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        return destination.toString();
    }

    public static void main(String[] args) {
        launch(args);
    }
}