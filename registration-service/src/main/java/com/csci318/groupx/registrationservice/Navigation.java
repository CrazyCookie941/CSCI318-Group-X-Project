package com.csci318.groupx.registrationservice;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.util.function.Consumer;

final class Navigation
{
    private static Popup activePopup;
    private static Button activePopupAnchor;

    private Navigation() {}

    static void goTo(Stage stage, Consumer<Stage> screen)
    {
        screen.accept(stage);
    }

    static HBox createNavigationBar(Stage stage)
    {
        HBox navigationBar = new HBox();
        navigationBar.setPadding(new Insets(12, 24, 12, 24));
        navigationBar.setSpacing(20);
        navigationBar.setAlignment(Pos.CENTER_LEFT);
        navigationBar.setStyle("-fx-background-color: #1a1a1a;");

        Hyperlink homeLink = createNavigationLink("Home");
        homeLink.setOnAction(event -> goTo(stage, nextStage -> new DashboardUI().start(nextStage)));

        Hyperlink eventsLink = createNavigationLink("My Events");
        eventsLink.setOnAction(event ->
        {
            if (Session.isManager()) { goTo(stage, nextStage -> new MyListingsEventManager().start(nextStage)); }
            else { goTo(stage, nextStage -> new MyListingsEventGoer().start(nextStage)); }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addEventButton = createHeaderButton(createPlusIcon(), "Add Event");
        Button notificationButton = createHeaderButton(createNotificationIcon(), "Notifications");
        Button profileButton = createHeaderButton(createProfileIcon(), "Profile");
        notificationButton.setOnAction(event -> showNotificationsPopup(notificationButton));
        profileButton.setOnAction(event -> showProfilePopup(profileButton, stage));

        navigationBar.getChildren().addAll(homeLink, eventsLink, spacer, addEventButton, notificationButton, profileButton);
        return navigationBar;
    }

    private static StackPane createPlusIcon()
    {
        // Vector path for a clean, sharp plus math symbol matching a 24x24 layout grid
        SVGPath plusPath = new SVGPath();
        plusPath.setContent("M 19 13 h -6 v 6 h -2 v -6 H 5 v -2 h 6 V 5 h 2 v 6 h 6 z");

        // Change the fill to match your navigation bar background color (#1a1a1a)
        // to create the solid visual cutout effect shown in your image
        plusPath.setFill(Color.web("#1a1a1a"));

        // Outer layout circle frame - now filled with solid white
        Circle backgroundCircle = new Circle(12, Color.WHITE);

        StackPane icon = new StackPane(backgroundCircle, plusPath);
        icon.setMinSize(24, 24);
        icon.setPrefSize(24, 24);
        icon.setMaxSize(24, 24);
        return icon;
    }

    private static Button createHeaderButton(Node graphic, String accessibleText)
    {
        Button button = new Button();
        button.setGraphic(graphic);
        button.setAccessibleText(accessibleText);
        button.setFocusTraversable(false);
        button.setStyle("-fx-background-color: transparent; -fx-padding: 4 7; -fx-cursor: hand;");
        return button;
    }

    private static StackPane createProfileIcon()
    {
        SVGPath profilePath = new SVGPath();
        // A balanced, modern profile path (24x24 scale) with a round head and contoured shoulders
        profilePath.setContent("M 12 12 C 14.21 12, 16 10.21, 16 8 C 16 5.79, 14.21 4, 12 4 C 9.79 4, 8 5.79, 8 8 C 8 10.21, 9.79 12, 12 12 Z M 12 14 C 9.33 14, 4 15.34, 4 18 V 20 H 20 V 18 C 20 15.34, 14.67 14, 12 14 Z");
        profilePath.setFill(Color.WHITE);

        // Center it cleanly within the 24x24 container
        StackPane icon = new StackPane(profilePath);
        icon.setMinSize(24, 24);
        icon.setPrefSize(24, 24);
        icon.setMaxSize(24, 24);
        return icon;
    }

    private static SVGPath createNotificationIcon()
    {
        SVGPath bell = new SVGPath();
        bell.setContent("M 12 3 C 8.7 3, 6.5 5.6, 6.5 9 V 14 L 4 17 H 20 L 17.5 14 V 9 C 17.5 5.6, 15.3 3, 12 3 Z M 9.5 19 C 9.8 20.5, 10.7 21.5, 12 21.5 C 13.3 21.5, 14.2 20.5, 14.5 19 Z");
        bell.setFill(Color.WHITE);
        bell.setStroke(Color.WHITE);
        bell.setStrokeWidth(0.5);
        bell.setScaleX(0.9);
        bell.setScaleY(0.9);
        return bell;
    }

    private static void showProfilePopup(Button profileButton, Stage stage)
    {
        VBox menu = createPopupMenu();

        Button accountButton = createPopupButton("Account");
        accountButton.setOnAction(event -> {
            closePopup(menu);
            goTo(stage, nextStage -> new AccountUI().start(nextStage));
        });

        Button settingsButton = createPopupButton("Settings");
        settingsButton.setOnAction(event -> {
            closePopup(menu);
            goTo(stage, nextStage -> new SettingsUI().start(nextStage));
        });

        Button logoutButton = createPopupButton("Log out");
        logoutButton.setOnAction(event -> {
            closePopup(menu);
            goTo(stage, nextStage -> new LandingPage().start(nextStage));
        });

        menu.getChildren().addAll(accountButton, settingsButton, logoutButton);
        showPopup(menu, profileButton);
    }

    private static void showNotificationsPopup(Button notificationButton)
    {
        VBox menu = createPopupMenu();
        Label title = new Label("Notifications");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        Label notification = new Label("No new notifications");
        notification.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");
        menu.getChildren().addAll(title, new Separator(), notification);
        showPopup(menu, notificationButton);
    }

    private static VBox createPopupMenu()
    {
        VBox menu = new VBox(8);
        menu.setPadding(new Insets(10));
        menu.setMinWidth(180);
        menu.setStyle("-fx-background-color: white; -fx-border-color: #d8d8d8; -fx-border-radius: 4; -fx-background-radius: 4; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 2);");
        return menu;
    }

    private static Button createPopupButton(String text)
    {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: #222222; -fx-font-size: 13px; -fx-padding: 7 10; -fx-cursor: hand;");
        return button;
    }

    private static void showPopup(VBox content, Button anchor)
    {
        if (activePopup != null)
        {
            Popup popup = activePopup;
            Button previousAnchor = activePopupAnchor;
            popup.hide();
            activePopup = null;
            activePopupAnchor = null;

            if (previousAnchor == anchor) { return; }
        }

        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.getContent().add(content);
        content.getProperties().put("popup", popup);
        activePopup = popup;
        activePopupAnchor = anchor;
        popup.setOnHidden(event -> {
            if (activePopup == popup)
            {
                activePopup = null;
                activePopupAnchor = null;
            }
        });
        popup.show(anchor, anchor.localToScreen(anchor.getBoundsInLocal()).getMaxX() - content.getMinWidth(),
                anchor.localToScreen(anchor.getBoundsInLocal()).getMaxY() + 6);
    }

    private static void closePopup(VBox content)
    {
        if (content.getProperties().get("popup") instanceof Popup popup)
        {
            popup.hide();
            if (activePopup == popup)
            {
                activePopup = null;
                activePopupAnchor = null;
            }
        }
    }

    private static Hyperlink createNavigationLink(String text)
    {
        Hyperlink link = new Hyperlink(text);
        link.setStyle("-fx-text-fill: white; -fx-underline: false; -fx-font-size: 14px; -fx-font-weight: bold;");
        return link;
    }
}
