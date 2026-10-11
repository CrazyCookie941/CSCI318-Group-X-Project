package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateEventUI extends Application
{
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f4f4;");

        HBox navBar = Navigation.createNavigationBar(primaryStage);
        root.setTop(navBar);

        HBox mainContent = new HBox(40);
        mainContent.setPadding(new Insets(30, 40, 30, 40));
        mainContent.setAlignment(Pos.TOP_CENTER);

        VBox leftColumn = new VBox(20);
        leftColumn.setMinWidth(500);

        Label headerTitle = new Label("Create an event listing");
        headerTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setAlignment(Pos.TOP_LEFT);

        ColumnConstraints labelColumn = new ColumnConstraints(150);
        ColumnConstraints fieldColumn = new ColumnConstraints(320);
        formGrid.getColumnConstraints().addAll(labelColumn, fieldColumn);

        Label lblTitle = new Label("Event name:");
        TextField txtTitle = new TextField();
        txtTitle.setPromptText("e.g. Radiohead concert");

        Label lblType = new Label("Event type:");
        ComboBox<String> cbEventType = new ComboBox<>();
        cbEventType.getItems().addAll("Concert", "Nightclub", "Conference", "Exhibition", "Festival", "Workshop", "Community", "Games");
        cbEventType.setPromptText("Select event type...");
        cbEventType.setMaxWidth(Double.MAX_VALUE);

        Label lblLocation = new Label("Event location:");
        TextField txtLocation = new TextField();
        txtLocation.setPromptText("Enter a venue or street address");
        Button btnSearchLocation = new Button("Find location");
        HBox locationSearch = new HBox(8, txtLocation, btnSearchLocation);
        HBox.setHgrow(txtLocation, Priority.ALWAYS);
        ComboBox<LocationGeocoder.GeocodedLocation> cbLocationResults = new ComboBox<>();
        cbLocationResults.setPromptText("Choose the matching real-world location");
        cbLocationResults.setMaxWidth(Double.MAX_VALUE);
        final LocationGeocoder.GeocodedLocation[] selectedLocation = {null};
        cbLocationResults.setOnAction(action -> selectedLocation[0] = cbLocationResults.getValue());
        txtLocation.textProperty().addListener((observable, previous, current) -> {
            selectedLocation[0] = null;
            cbLocationResults.getItems().clear();
            cbLocationResults.getSelectionModel().clearSelection();
        });
        btnSearchLocation.setOnAction(action -> {
            String query = txtLocation.getText().trim();
            if (query.isBlank()) {
                new Alert(Alert.AlertType.WARNING, "Enter a venue or address to search.").showAndWait();
                return;
            }
            btnSearchLocation.setDisable(true);
            Task<List<LocationGeocoder.GeocodedLocation>> searchTask = new Task<>() {
                @Override
                protected List<LocationGeocoder.GeocodedLocation> call() {
                    return LocationGeocoder.search(query);
                }
            };
            searchTask.setOnSucceeded(result -> {
                cbLocationResults.getItems().setAll(searchTask.getValue());
                cbLocationResults.getSelectionModel().clearSelection();
                selectedLocation[0] = null;
                if (cbLocationResults.getItems().isEmpty()) {
                    new Alert(Alert.AlertType.INFORMATION,
                            "No matching locations were found. Try adding a city or country.").showAndWait();
                } else {
                    cbLocationResults.show();
                }
                btnSearchLocation.setDisable(false);
            });
            searchTask.setOnFailed(result -> {
                new Alert(Alert.AlertType.ERROR,
                        "The location could not be searched. Check your internet connection and try again.\n"
                                + searchTask.getException().getMessage()).showAndWait();
                btnSearchLocation.setDisable(false);
            });
            Thread searchThread = new Thread(searchTask, "event-location-search");
            searchThread.setDaemon(true);
            searchThread.start();
        });

        Label lblPrice = new Label("Event price:");
        TextField txtPrice = new TextField();
        txtPrice.setPromptText("e.g. Free or $25.00");

        Label lblSize = new Label("Event size:");
        TextField txtSize = new TextField();
        txtSize.setPromptText("Maximum attendee capacity");

        Label lblPayment = new Label("Payment prefs:");
        ComboBox<String> cbPaymentPreference = new ComboBox<>();
        cbPaymentPreference.getItems().addAll("Cash", "Card", "Bank transfer", "PayPal", "On arrival", "Split payment");
        cbPaymentPreference.setPromptText("Select payment method...");
        cbPaymentPreference.setMaxWidth(Double.MAX_VALUE);

        Label lblInfo = new Label("Other information:");
        TextArea txtOtherInfo = new TextArea();
        txtOtherInfo.setPromptText("Provide any extra essential details for attendees...");
        txtOtherInfo.setPrefRowCount(3);
        txtOtherInfo.setWrapText(true);

        Label lblMiscDocs = new Label("Misc. info docs:");
        VBox miscDocUpload = new VBox(8);
        Button btnUploadDocs = new Button("Upload documents");
        Label lblUploadedDocs = new Label("No misc documents selected");
        lblUploadedDocs.setWrapText(true);
        lblUploadedDocs.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
        miscDocUpload.getChildren().addAll(btnUploadDocs, lblUploadedDocs);

        List<String> selectedDocumentPaths = new ArrayList<>();
        btnUploadDocs.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose supporting documents");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx", "*.txt", "*.csv"),
                    new FileChooser.ExtensionFilter("All files", "*.*")
            );
            List<java.io.File> files = chooser.showOpenMultipleDialog(primaryStage);
            if (files != null && !files.isEmpty()) {
                selectedDocumentPaths.clear();
                for (java.io.File file : files) {
                    selectedDocumentPaths.add(file.getAbsolutePath());
                }
                lblUploadedDocs.setText(String.join(", ", files.stream().map(java.io.File::getName).toList()));
            }
        });

        formGrid.add(lblTitle, 0, 0);
        formGrid.add(txtTitle, 1, 0);

        formGrid.add(lblType, 0, 1);
        formGrid.add(cbEventType, 1, 1);

        formGrid.add(lblLocation, 0, 2);
        formGrid.add(locationSearch, 1, 2);
        formGrid.add(new Label("Confirm location:"), 0, 3);
        formGrid.add(cbLocationResults, 1, 3);

        formGrid.add(lblPrice, 0, 4);
        formGrid.add(txtPrice, 1, 4);

        formGrid.add(lblSize, 0, 5);
        formGrid.add(txtSize, 1, 5);

        formGrid.add(lblPayment, 0, 6);
        formGrid.add(cbPaymentPreference, 1, 6);

        formGrid.add(lblMiscDocs, 0, 7);
        formGrid.add(miscDocUpload, 1, 7);

        formGrid.add(lblInfo, 0, 8);
        formGrid.add(txtOtherInfo, 1, 8);

        formGrid.getChildren().stream()
                .filter(Label.class::isInstance)
                .map(Label.class::cast)
                .forEach(label -> label.setStyle("-fx-text-fill: #222222"));

        final String[] selectedImage = {null};

        Button btnSubmit = new Button("Create Listing");
        btnSubmit.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20 10 20; -fx-background-radius: 4;");

        btnSubmit.setOnAction(event -> {
            if (txtTitle.getText().isBlank() || cbEventType.getValue() == null || txtLocation.getText().isBlank()
                    || selectedLocation[0] == null) {
                new Alert(Alert.AlertType.WARNING,
                        "Please provide an event name and type, then search for and confirm the event location.")
                        .showAndWait();
                return;
            }

            try {
                EventCatalog.parsePrice(txtPrice.getText());
            } catch (NumberFormatException exception) {
                new Alert(Alert.AlertType.WARNING, "Price must be a number (for example 25 or 25.50), or 'Free'.").showAndWait();
                return;
            }

            EventListing saved;
            try {
                String imageLocation = copyUploadedImage(selectedImage[0]);
                EventListing listing = new EventListing(
                        txtTitle.getText().trim(),
                        cbEventType.getValue(),
                        selectedLocation[0].displayName(),
                        txtPrice.getText().isBlank() ? "Free" : txtPrice.getText().trim(),
                        txtSize.getText().isBlank() ? "Not specified" : txtSize.getText().trim(),
                        txtOtherInfo.getText().isBlank() ? "No additional information provided." : txtOtherInfo.getText().trim(),
                        imageLocation,
                        null,
                        selectedLocation[0].latitude(),
                        selectedLocation[0].longitude());
                saved = EventCatalog.add(listing);
            } catch (IOException exception) {
                new Alert(Alert.AlertType.ERROR, "The photo could not be uploaded: " + exception.getMessage()).showAndWait();
                return;
            } catch (RuntimeException exception) {
                new Alert(Alert.AlertType.ERROR, "The event could not be saved: " + exception.getMessage()).showAndWait();
                return;
            }
            Navigation.goTo(primaryStage, nextStage -> new ViewEventUI(saved).start(nextStage));
        });

        leftColumn.getChildren().addAll(headerTitle, formGrid, btnSubmit);

        VBox rightColumn = new VBox(12);
        rightColumn.setAlignment(Pos.TOP_LEFT);

        Label lblPromoImage = new Label("Promotional images:");
        lblPromoImage.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #555555;");

        VBox imagePlaceholderBox = new VBox();
        imagePlaceholderBox.setPrefSize(280, 240);
        imagePlaceholderBox.setStyle("-fx-border-color: #cccccc; -fx-border-style: dashed; -fx-border-width: 2; -fx-background-color: #e9e9e9; -fx-border-radius: 6; -fx-background-radius: 6;");
        imagePlaceholderBox.setAlignment(Pos.CENTER);

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

        VBox paymentSummary = new VBox(8);
        paymentSummary.setPadding(new Insets(12));
        paymentSummary.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d0d0d0; -fx-border-radius: 6; -fx-background-radius: 6;");

        Label lblPaymentSummaryTitle = new Label("Payment preferences");
        lblPaymentSummaryTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #555555;");
        CheckBox chkRequiresDeposit = new CheckBox("Require a deposit for registration");
        chkRequiresDeposit.setSelected(false);
        paymentSummary.getChildren().addAll(lblPaymentSummaryTitle, chkRequiresDeposit);

        rightColumn.getChildren().addAll(lblPromoImage, imagePlaceholderBox, uploadButton, paymentSummary);

        mainContent.getChildren().addAll(leftColumn, rightColumn);
        ScrollPane formScrollPane = new ScrollPane(mainContent);
        formScrollPane.setFitToWidth(true);
        formScrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        root.setCenter(formScrollPane);

        Scene scene = new Scene(root, 1366, 768);
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