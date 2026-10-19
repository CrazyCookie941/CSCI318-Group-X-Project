package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.Locale;
import java.util.regex.Pattern;

// Displays expandable account and application settings sections.
public class SettingsUI extends Application
{

    @Override
    public void start(Stage stage)
    {
        // 1. Root Layout Container
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: #fafafa;");

        // 2. Styled Top Navigation Bar
        HBox navigationBar = Navigation.createNavigationBar(stage);
        layout.setTop(navigationBar);

        // 3. Central Working Content Pane
        VBox content = new VBox(25);
        content.setPadding(new Insets(35, 40, 35, 45));
        content.setAlignment(Pos.TOP_LEFT);

        Label title = blackLabel("Settings");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #000000;");

        // Vertical stack for the expandable layout card boxes
        VBox settingsContainer = new VBox(15);
        settingsContainer.setStyle("-fx-background-color: transparent;");
        settingsContainer.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(settingsContainer, Priority.ALWAYS);

        // Keep account, billing, notification, and application settings available.
        settingsContainer.getChildren().addAll(
                createSettingsSection("Account details", createAccountSettingsContent()),
                createSettingsSection("Financial information", createFinancialSettingsContent()),
                createSettingsSection("Notifications", createNotificationSettingsContent()),
                createSettingsSection("Other", createOtherSettingsContent())
        );

        content.getChildren().addAll(title, settingsContainer);

        // 4. Scroll Wrapper Integration (Safeguards against window clipping)
        ScrollPane scrollWrapper = new ScrollPane(content);
        scrollWrapper.setFitToWidth(true);
        scrollWrapper.setFitToHeight(true);
        scrollWrapper.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollWrapper.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollWrapper.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        BorderPane.setMargin(scrollWrapper, new Insets(0, 15, 0, 0));
        layout.setCenter(scrollWrapper);

        // 5. Stage Initialization Settings
        stage.setTitle("EventRunner AI Event Management Platform");
        Scene scene = new Scene(layout, 1366, 768);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        AppPreferences.applyDisplaySettings(stage.getScene());
    }

    private static VBox createAccountSettingsContent()
    {
        TextField username = new TextField();
        TextField email = new TextField();
        TextField location = new TextField();
        username.setPromptText("Username");
        email.setPromptText("Email address");
        location.setPromptText("Location or suburb");
        username.setMaxWidth(420);
        email.setMaxWidth(420);
        location.setMaxWidth(420);

        Button save = new Button("Save account details");
        Button choosePicture = new Button("Upload profile picture");
        Label status = blackLabel("");
        Label pictureStatus = blackLabel("");
        ImageView picturePreview = new ImageView();
        picturePreview.setFitWidth(96);
        picturePreview.setFitHeight(96);
        picturePreview.setPreserveRatio(true);
        picturePreview.setStyle("-fx-background-color: #e5e5e5;");
        HBox pictureControls = new HBox(12, picturePreview, choosePicture);
        pictureControls.setAlignment(Pos.CENTER_LEFT);
        GridPane fields = new GridPane();
        fields.setHgap(12);
        fields.setVgap(10);
        Label usernameLabel = blackLabel("Username");
        Label emailLabel = blackLabel("Email");
        Label locationLabel = blackLabel("Location");
        fields.addRow(0, usernameLabel, username);
        fields.addRow(1, emailLabel, email);
        fields.addRow(2, locationLabel, location);
        VBox controls = new VBox(10, fields, pictureControls, pictureStatus, save, status);

        Long accountId = CurrentUserSession.getAccountId();
        if (accountId == null) {
            username.setDisable(true);
            email.setDisable(true);
            location.setDisable(true);
            save.setDisable(true);
            choosePicture.setDisable(true);
            status.setText("Sign in to update account details.");
            return controls;
        }

        setAccountControlsDisabled(username, email, location, save, choosePicture, true);
        status.setText("Loading account details...");
        AccountApiClient.profile(accountId).whenComplete((profile, error) ->
                Platform.runLater(() -> {
                    if (error != null || profile == null || !profile.success()) {
                        status.setText("Could not load account details. Check account-service (port 8083).");
                        return;
                    }
                    username.setText(profile.username() == null ? "" : profile.username());
                    email.setText(profile.email() == null ? "" : profile.email());
                    location.setText(profile.location() == null ? "" : profile.location());
                    setAccountControlsDisabled(
                            username, email, location, save, choosePicture, false);
                    status.setText("");

                    AccountApiClient.profilePicture(accountId).whenComplete(
                            (picture, pictureError) -> Platform.runLater(() -> {
                                if (pictureError != null) {
                                    pictureStatus.setText(
                                            "Could not load the profile picture.");
                                } else if (picture != null) {
                                    showProfilePicture(picturePreview, picture);
                                }
                            }));
                }));

        save.setOnAction(event -> {
            String name = username.getText().trim();
            String address = email.getText().trim();
            String area = location.getText().trim();
            if (name.isBlank() || address.isBlank()) {
                status.setText("Username and email are required.");
                return;
            }
            if (!Pattern.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", address)) {
                status.setText("Enter a valid email address.");
                return;
            }
            setAccountControlsDisabled(
                    username, email, location, save, choosePicture, true);
            status.setText("Saving account details...");
            AccountApiClient.update(accountId, name, address, area).whenComplete((profile, error) ->
                    Platform.runLater(() -> {
                        setAccountControlsDisabled(
                                username, email, location, save, choosePicture, false);
                        if (error != null || profile == null || !profile.success()) {
                            status.setText(profile == null
                                    ? "Could not save account details. Please try again."
                                    : profile.message());
                            return;
                        }
                        username.setText(profile.username());
                        email.setText(profile.email());
                        location.setText(profile.location() == null ? "" : profile.location());
                        CurrentUserSession.updateProfile(profile.username(), profile.email());
                        status.setText("Account details saved.");
                    }));
        });

        choosePicture.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose a profile picture");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                    "Images (PNG, JPEG, GIF)", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File selectedFile = chooser.showOpenDialog(
                    choosePicture.getScene().getWindow());
            if (selectedFile == null) {
                return;
            }

            String contentType = profilePictureContentType(selectedFile);
            if (contentType == null) {
                pictureStatus.setText("Choose a PNG, JPEG, or GIF image.");
                return;
            }

            try {
                long size = Files.size(selectedFile.toPath());
                if (size == 0 || size > 5L * 1024 * 1024) {
                    pictureStatus.setText("Choose an image no larger than 5 MB.");
                    return;
                }
                byte[] picture = Files.readAllBytes(selectedFile.toPath());
                Image image = new Image(new ByteArrayInputStream(picture));
                if (image.isError()) {
                    pictureStatus.setText("The selected file is not a valid image.");
                    return;
                }

                choosePicture.setDisable(true);
                pictureStatus.setText("Uploading profile picture...");
                AccountApiClient.updateProfilePicture(
                        accountId, picture, contentType).whenComplete((result, uploadError) ->
                        Platform.runLater(() -> {
                            choosePicture.setDisable(false);
                            if (uploadError != null) {
                                pictureStatus.setText(
                                        "Could not upload the profile picture. Please try again.");
                                return;
                            }
                            picturePreview.setImage(image);
                            pictureStatus.setText("Profile picture updated.");
                        }));
            } catch (IOException exception) {
                pictureStatus.setText("Could not read the selected image.");
            }
        });
        return controls;
    }

    private static void setAccountControlsDisabled(
            TextField username,
            TextField email,
            TextField location,
            Button save,
            Button choosePicture,
            boolean disabled
    ) {
        username.setDisable(disabled);
        email.setDisable(disabled);
        location.setDisable(disabled);
        save.setDisable(disabled);
        choosePicture.setDisable(disabled);
    }

    private static void showProfilePicture(ImageView preview, byte[] picture) {
        Image image = new Image(new ByteArrayInputStream(picture));
        if (!image.isError()) {
            preview.setImage(image);
        }
    }

    private static String profilePictureContentType(File file) {
        String fileName = file.getName().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".png")) {
            return "image/png";
        }
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (fileName.endsWith(".gif")) {
            return "image/gif";
        }
        return null;
    }

    private static VBox createFinancialSettingsContent()
    {
        Label note = blackLabel(
                "Checkout payments are simulated. The app does not collect or store card or bank details.");
        note.setWrapText(true);
        ComboBox<String> provider = new ComboBox<>();
        provider.getItems().addAll("VISA", "MASTERCARD", "PAYPAL");
        provider.setPromptText("Choose a default checkout provider");
        provider.setMaxWidth(320);
        styleDropdown(provider, value -> value);
        Button saveProvider = new Button("Save default provider");
        Label status = blackLabel("");
        Label historyTitle = blackLabel("Payment and admission history");
        historyTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #000000;");
        VBox history = new VBox(8);
        VBox controls = new VBox(10, note, blackLabel("Default checkout provider:"), provider,
                saveProvider, status, historyTitle, history);

        Long accountId = CurrentUserSession.getAccountId();
        if (accountId == null) {
            provider.setDisable(true);
            saveProvider.setDisable(true);
            status.setText("Sign in to manage your checkout preference and view payment history.");
            return controls;
        }

        provider.setValue(AppPreferences.preferredPaymentProvider(accountId));
        saveProvider.setOnAction(event -> {
            String selected = provider.getValue();
            if (selected == null) {
                status.setText("Choose a payment provider first.");
                return;
            }
            AppPreferences.setPreferredPaymentProvider(accountId, selected);
            status.setText("Default provider saved on this device.");
        });
        status.setText("Loading payment history...");
        PaymentApiClient.history(accountId).whenComplete((payments, error) ->
                Platform.runLater(() -> {
                    if (error != null || payments == null) {
                        status.setText("Could not load payment history. Check payment-service (port 8085).");
                        return;
                    }
                    status.setText("");
                    if (payments.isEmpty()) {
                        history.getChildren().setAll(blackLabel("No payments have been recorded yet."));
                        return;
                    }
                    DateTimeFormatter dateFormat = DateTimeFormatter
                            .ofPattern("yyyy-MM-dd HH:mm")
                            .withZone(ZoneId.systemDefault());
                    List<Label> rows = payments.stream().map(payment -> {
                        String amount = payment.amount() == null
                                ? "—"
                                : payment.currency() + " " + payment.amount()
                                        .setScale(2, RoundingMode.HALF_UP);
                        String date = payment.createdAt() == null
                                ? "Date unavailable"
                                : dateFormat.format(payment.createdAt());
                        Label row = blackLabel(date + " · " + payment.eventName() + " · "
                                + amount + " · " + payment.paymentMethod() + " · "
                                + payment.status() + " · Ref " + payment.reference());
                        row.setWrapText(true);
                        return row;
                    }).toList();
                    history.getChildren().setAll(rows);
                }));
        return controls;
    }

    private static VBox createNotificationSettingsContent()
    {
        CheckBox enabled = new CheckBox("Receive notifications");
        enabled.setStyle("-fx-opacity: 1.0; -fx-text-fill: #000000;");

        TextField area = new TextField();
        area.setPromptText("Area or suburb to follow (for example, Sydney)");
        area.setStyle("-fx-opacity: 1.0; -fx-text-fill: #000000;");

        area.setMaxWidth(420);
        Button save = new Button("Save notification settings");
        Label desc = blackLabel("Notify me when a new event is posted in this area:");
        desc.setStyle("-fx-opacity: 1.0; -fx-text-fill: #000000;");
        Label status = blackLabel("");
        VBox controls = new VBox(10,
                enabled,
                desc,
                area,
                save,
                status);

        Long accountId = CurrentUserSession.getAccountId();
        if (accountId == null) {
            enabled.setDisable(true);
            area.setDisable(true);
            save.setDisable(true);
            status.setText("Sign in to manage notification settings.");
            return controls;
        }

        enabled.setDisable(true);
        area.setDisable(true);
        save.setDisable(true);
        status.setText("Loading settings...");
        CompletableFuture.supplyAsync(() -> NotificationApiClient.preference(accountId))
                .whenComplete((preference, error) -> Platform.runLater(() -> {
                    if (error != null || preference == null) {
                        status.setText("Could not load notification settings.");
                        return;
                    }
                    enabled.setSelected(preference.enabled());
                    area.setText(preference.area() == null ? "" : preference.area());
                    enabled.setDisable(false);
                    area.setDisable(false);
                    save.setDisable(false);
                    status.setText("");
                }));

        save.setOnAction(event -> {
            enabled.setDisable(true);
            area.setDisable(true);
            save.setDisable(true);
            status.setText("Saving...");
            CompletableFuture.supplyAsync(() -> NotificationApiClient.savePreference(
                            accountId, enabled.isSelected(), area.getText()))
                    .whenComplete((saved, error) -> Platform.runLater(() -> {
                        enabled.setDisable(false);
                        area.setDisable(false);
                        save.setDisable(false);
                        status.setText(error == null && saved != null
                                ? "Notification settings saved."
                                : "Could not save settings. Please try again.");
                    }));
        });
        return controls;
    }

    private static VBox createSettingsSection(String sectionTitle, Node detailsContent)
    {
        VBox componentBlock = new VBox(0);
        componentBlock.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(componentBlock, Priority.ALWAYS);

        BorderPane rowHeaderCard = new BorderPane();
        rowHeaderCard.setPadding(new Insets(16, 24, 16, 24));
        rowHeaderCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");

        Label titleLabel = blackLabel(sectionTitle);
        titleLabel.setStyle("-fx-text-fill: #000000; -fx-font-size: 15px; -fx-font-weight: bold;");
        HBox leftWrapper = new HBox(titleLabel);
        leftWrapper.setAlignment(Pos.CENTER_LEFT);
        rowHeaderCard.setLeft(leftWrapper);

        Label arrowIndicator = blackLabel("▼");
        arrowIndicator.setStyle("-fx-text-fill: #000000; -fx-font-size: 13px; -fx-font-weight: bold;");
        HBox rightWrapper = new HBox(arrowIndicator);
        rightWrapper.setAlignment(Pos.CENTER_RIGHT);
        rowHeaderCard.setRight(rightWrapper);

        VBox drawerContent = new VBox(detailsContent);
        drawerContent.setPadding(new Insets(15, 24, 18, 24));
        drawerContent.setStyle("-fx-background-color: #f7f7f7; -fx-border-color: transparent #e0e0e0 #e0e0e0 #e0e0e0; -fx-border-width: 1; -fx-border-radius: 0 0 6 6; -fx-background-radius: 0 0 6 6;");

        boolean initiallyExpanded = "Other".equals(sectionTitle);
        drawerContent.setVisible(initiallyExpanded);
        drawerContent.setManaged(initiallyExpanded);
        if (initiallyExpanded) {
            arrowIndicator.setText("▲");
            rowHeaderCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0 #e0e0e0 transparent #e0e0e0; -fx-border-width: 1; -fx-border-radius: 6 6 0 0; -fx-background-radius: 6 6 0 0; -fx-cursor: hand;");
        }

        rowHeaderCard.setOnMouseEntered(event -> AppPreferences.setBaseStyle(rowHeaderCard,"-fx-background-color: #fcfcfc; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;"));
        rowHeaderCard.setOnMouseExited(event -> {
            if (drawerContent.isVisible()) {
                AppPreferences.setBaseStyle(rowHeaderCard,"-fx-background-color: #ffffff; -fx-border-color: #e0e0e0 #e0e0e0 transparent #e0e0e0; -fx-border-width: 1; -fx-border-radius: 6 6 0 0; -fx-background-radius: 6 6 0 0; -fx-cursor: hand;");
            } else {
                AppPreferences.setBaseStyle(rowHeaderCard,"-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
            }
        });

        rowHeaderCard.setOnMouseClicked(event -> {
            boolean expanded = drawerContent.isVisible();
            drawerContent.setVisible(!expanded);
            drawerContent.setManaged(!expanded);
            arrowIndicator.setText(expanded ? "▼" : "▲");
            if (!expanded) {
                AppPreferences.setBaseStyle(rowHeaderCard,"-fx-background-color: #ffffff; -fx-border-color: #e0e0e0 #e0e0e0 transparent #e0e0e0; -fx-border-width: 1; -fx-border-radius: 6 6 0 0; -fx-background-radius: 6 6 0 0; -fx-cursor: hand;");
            } else {
                AppPreferences.setBaseStyle(rowHeaderCard,"-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
            }
        });

        componentBlock.getChildren().addAll(rowHeaderCard, drawerContent);
        return componentBlock;
    }

    private static VBox createOtherSettingsContent()
    {
        VBox otherSettingsContainer = new VBox(12);
        otherSettingsContainer.setFillWidth(true);
        otherSettingsContainer.getChildren().addAll(
                createNestedSettingsSection("Accessibility settings",
                        createAccessibilitySettingsContent()),
                createNestedSettingsSection("System & display settings",
                        createDisplaySettingsContent()),
                createNestedSettingsSection("Extra features / toggleable settings",
                        createFeatureSettingsContent())
        );
        return otherSettingsContainer;
    }

    private static VBox createAccessibilitySettingsContent()
    {
        CheckBox highContrast = blackCheckBox("High contrast");
        highContrast.setSelected(AppPreferences.highContrast());
        highContrast.setOnAction(event -> {
            AppPreferences.setHighContrast(highContrast.isSelected());
            AppPreferences.applyDisplaySettings(highContrast.getScene());
        });

        CheckBox descriptions = blackCheckBox("Use descriptive assistive-technology labels");
        descriptions.setSelected(AppPreferences.assistiveDescriptions());
        descriptions.setOnAction(event -> {
            AppPreferences.setAssistiveDescriptions(descriptions.isSelected());
            AppPreferences.applyDisplaySettings(descriptions.getScene());
        });

        ComboBox<Integer> textScale = new ComboBox<>();
        textScale.getItems().addAll(100, 115, 130);
        textScale.setValue(AppPreferences.textScale());
        styleDropdown(textScale, value -> value + "%");
        textScale.setOnAction(event -> {
            if (textScale.getValue() != null) {
                AppPreferences.setTextScale(textScale.getValue());
                AppPreferences.applyDisplaySettings(textScale.getScene());
            }
        });
        Label scaleLabel = blackLabel("Text size:");
        return new VBox(10, highContrast, descriptions, new HBox(10, scaleLabel, textScale));
    }

    private static <T> void styleDropdown(
            ComboBox<T> comboBox,
            java.util.function.Function<T, String> text
    ) {
        comboBox.setStyle("-fx-text-fill: #000000;");
        comboBox.setCellFactory(list -> dropdownCell(text));
        comboBox.setButtonCell(dropdownCell(text));
    }

    private static final String CELL_STYLE =
            "-fx-text-fill: #000000; -fx-background-color: #ffffff;";
    private static final String CELL_HOVER_STYLE =
            "-fx-text-fill: #000000; -fx-background-color: #d9d9d9;";

    private static <T> javafx.scene.control.ListCell<T> dropdownCell(
            java.util.function.Function<T, String> text
    ) {
        return new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(T value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : text.apply(value));
                AppPreferences.setBaseStyle(this, CELL_STYLE);
            }

            {
                setOnMouseEntered(event -> {
                    if (!isEmpty()) {
                        AppPreferences.setBaseStyle(this, CELL_HOVER_STYLE);
                    }
                });
                setOnMouseExited(event ->
                        AppPreferences.setBaseStyle(this, CELL_STYLE));
            }
        };
    }

    private static VBox createDisplaySettingsContent() {
        ComboBox<String> theme = new ComboBox<>();
        theme.getItems().addAll("Light", "Dark");
        theme.setValue(AppPreferences.theme());
        styleDropdown(theme, value -> value);
        theme.setOnAction(event -> {
            if (theme.getValue() != null) {
                AppPreferences.setTheme(theme.getValue());
                AppPreferences.applyDisplaySettings(theme.getScene());
            }
        });

        return new VBox(10, new HBox(10, blackLabel("Appearance:"), theme));
    }

    private static VBox createFeatureSettingsContent() {
        CheckBox assistantShortcut = blackCheckBox("Show AI assistant shortcut in navigation");
        assistantShortcut.setSelected(AppPreferences.showAssistantShortcut());
        assistantShortcut.setOnAction(event ->
                AppPreferences.setShowAssistantShortcut(assistantShortcut.isSelected()));

        CheckBox unreadBadge = blackCheckBox("Show unread notification count");
        unreadBadge.setSelected(AppPreferences.showUnreadBadge());
        unreadBadge.setOnAction(event ->
                AppPreferences.setShowUnreadBadge(unreadBadge.isSelected()));
        Label help = blackLabel("These options take effect the next time a screen is opened.");
        help.setWrapText(true);
        return new VBox(10, assistantShortcut, unreadBadge, help);
    }

    private static VBox createNestedSettingsSection(String sectionTitle, Node settingsContent)
    {
        VBox nestedSection = new VBox(0);
        nestedSection.setMaxWidth(Double.MAX_VALUE);

        BorderPane nestedHeader = new BorderPane();
        nestedHeader.setPadding(new Insets(12, 18, 12, 18));
        nestedHeader.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dcdcdc; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
        Label nestedTitle = blackLabel(sectionTitle);
        nestedTitle.setStyle("-fx-text-fill: #000000; -fx-font-size: 14px; -fx-font-weight: bold;");
        nestedHeader.setLeft(nestedTitle);
        Label nestedArrow = blackLabel("▼");
        nestedArrow.setStyle("-fx-text-fill: #000000; -fx-font-size: 12px; -fx-font-weight: bold;");
        nestedHeader.setRight(nestedArrow);

        VBox nestedDrawer = new VBox(settingsContent);
        nestedDrawer.setPadding(new Insets(12, 18, 14, 18));
        nestedDrawer.setStyle("-fx-background-color: #f3f3f3; -fx-border-color: transparent #dcdcdc #dcdcdc #dcdcdc; -fx-border-width: 1; -fx-border-radius: 0 0 6 6; -fx-background-radius: 0 0 6 6;");
        nestedDrawer.setVisible(false);
        nestedDrawer.setManaged(false);
        nestedHeader.setOnMouseClicked(event -> {
            boolean expanded = nestedDrawer.isVisible();
            nestedDrawer.setVisible(!expanded);
            nestedDrawer.setManaged(!expanded);
            nestedArrow.setText(expanded ? "▼" : "▲");
        });
        nestedSection.getChildren().addAll(nestedHeader, nestedDrawer);
        return nestedSection;
    }

    private static Label blackLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #000000;");
        return label;
    }

    private static CheckBox blackCheckBox(String text) {
        CheckBox checkBox = new CheckBox(text);
        checkBox.setStyle("-fx-text-fill: #000000;");
        return checkBox;
    }

    public static void main(String[] args) { launch(args); }
}
