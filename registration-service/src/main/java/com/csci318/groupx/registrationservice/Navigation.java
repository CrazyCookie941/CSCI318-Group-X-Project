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
        homeLink.setOnAction(event ->
                goTo(stage, nextStage ->
                        new DashboardUI().start(nextStage))
        );

        Hyperlink eventsLink = createNavigationLink("My events");
        eventsLink.setOnAction(event ->
                goTo(stage, nextStage ->
                        new MyListingsEventGoer().start(nextStage))
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button notificationButton =
                createHeaderButton(
                        createNotificationIcon(),
                        "Notifications"
                );

        Button profileButton =
                createHeaderButton(
                        createProfileIcon(),
                        "Profile"
                );

        notificationButton.setOnAction(event ->
                showNotificationsPopup(notificationButton)
        );

        profileButton.setOnAction(event ->
                showProfilePopup(profileButton, stage)
        );

        navigationBar.getChildren().addAll(
                homeLink,
                eventsLink,
                spacer,
                notificationButton,
                profileButton
        );

        return navigationBar;
    }

    private static Button createHeaderButton(
            Node graphic,
            String accessibleText
    )
    {
        Button button = new Button();

        button.setGraphic(graphic);
        button.setAccessibleText(accessibleText);
        button.setFocusTraversable(false);

        button.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-padding: 4 7; " +
                        "-fx-cursor: hand;"
        );

        return button;
    }

    private static StackPane createProfileIcon()
    {
        Circle head = new Circle(4, Color.WHITE);
        head.setTranslateY(-5);

        SVGPath shoulders = new SVGPath();

        shoulders.setContent(
                "M 3 22 C 3 15, 21 15, 21 22 Z"
        );

        shoulders.setFill(Color.WHITE);

        StackPane icon = new StackPane(
                shoulders,
                head
        );

        icon.setMinSize(24, 24);
        icon.setPrefSize(24, 24);
        icon.setMaxSize(24, 24);

        return icon;
    }

    private static SVGPath createNotificationIcon()
    {
        SVGPath bell = new SVGPath();

        bell.setContent(
                "M 12 3 " +
                        "C 8.7 3, 6.5 5.6, 6.5 9 " +
                        "V 14 " +
                        "L 4 17 " +
                        "H 20 " +
                        "L 17.5 14 " +
                        "V 9 " +
                        "C 17.5 5.6, 15.3 3, 12 3 " +
                        "Z " +
                        "M 9.5 19 " +
                        "C 9.8 20.5, 10.7 21.5, 12 21.5 " +
                        "C 13.3 21.5, 14.2 20.5, 14.5 19 Z"
        );

        bell.setFill(Color.WHITE);
        bell.setStroke(Color.WHITE);
        bell.setStrokeWidth(0.5);
        bell.setScaleX(0.9);
        bell.setScaleY(0.9);

        return bell;
    }

    private static void showProfilePopup(
            Button profileButton,
            Stage stage
    )
    {
        VBox menu = createPopupMenu();

        Button accountButton =
                createPopupButton("Account");

        accountButton.setOnAction(event -> {
            closePopup(menu);

            goTo(
                    stage,
                    nextStage ->
                            new AccountUI().start(nextStage)
            );
        });

        Button settingsButton =
                createPopupButton("Settings");

        settingsButton.setOnAction(event -> {
            closePopup(menu);

            goTo(
                    stage,
                    nextStage ->
                            new SettingsUI().start(nextStage)
            );
        });

        Button logoutButton =
                createPopupButton("Log out");

        logoutButton.setOnAction(event -> {
            closePopup(menu);

            /*
             * Clear the current user's session before
             * returning to the landing page.
             */
            CurrentUserSession.logout();

            goTo(
                    stage,
                    nextStage ->
                            new LandingPage().start(nextStage)
            );
        });

        menu.getChildren().addAll(
                accountButton,
                settingsButton,
                logoutButton
        );

        showPopup(
                menu,
                profileButton
        );
    }

    private static void showNotificationsPopup(
            Button notificationButton
    )
    {
        VBox menu = createPopupMenu();

        Label title =
                new Label("Notifications");

        title.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #222222;"
        );

        Label notification =
                new Label("No new notifications");

        notification.setStyle(
                "-fx-font-size: 13px; " +
                        "-fx-text-fill: #666666;"
        );

        menu.getChildren().addAll(
                title,
                new Separator(),
                notification
        );

        showPopup(
                menu,
                notificationButton
        );
    }

    private static VBox createPopupMenu()
    {
        VBox menu = new VBox(8);

        menu.setPadding(new Insets(10));
        menu.setMinWidth(180);

        menu.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #d8d8d8; " +
                        "-fx-border-radius: 4; " +
                        "-fx-background-radius: 4; " +
                        "-fx-effect: dropshadow(" +
                        "three-pass-box, " +
                        "rgba(0,0,0,0.2), " +
                        "8, 0, 0, 2);"
        );

        return menu;
    }

    private static Button createPopupButton(
            String text
    )
    {
        Button button = new Button(text);

        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);

        button.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #222222; " +
                        "-fx-font-size: 13px; " +
                        "-fx-padding: 7 10; " +
                        "-fx-cursor: hand;"
        );

        return button;
    }

    private static void showPopup(
            VBox content,
            Button anchor
    )
    {
        if (activePopup != null)
        {
            Popup popup = activePopup;
            Button previousAnchor = activePopupAnchor;

            popup.hide();

            activePopup = null;
            activePopupAnchor = null;

            if (previousAnchor == anchor)
            {
                return;
            }
        }

        Popup popup = new Popup();

        popup.setAutoHide(true);
        popup.getContent().add(content);

        content.getProperties().put(
                "popup",
                popup
        );

        activePopup = popup;
        activePopupAnchor = anchor;

        popup.setOnHidden(event -> {
            if (activePopup == popup)
            {
                activePopup = null;
                activePopupAnchor = null;
            }
        });

        popup.show(
                anchor,
                anchor.localToScreen(
                                anchor.getBoundsInLocal()
                        )
                        .getMaxX()
                        - content.getMinWidth(),

                anchor.localToScreen(
                                anchor.getBoundsInLocal()
                        )
                        .getMaxY()
                        + 6
        );
    }

    private static void closePopup(
            VBox content
    )
    {
        if (content
                .getProperties()
                .get("popup")
                instanceof Popup popup)
        {
            popup.hide();

            if (activePopup == popup)
            {
                activePopup = null;
                activePopupAnchor = null;
            }
        }
    }

    private static Hyperlink createNavigationLink(
            String text
    )
    {
        Hyperlink link = new Hyperlink(text);

        link.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-underline: false; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold;"
        );

        return link;
    }
}