package com.csci318.groupx.registrationservice;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.Scene;
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
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

final class Navigation
{
    private static Popup activePopup;
    private static Button activePopupAnchor;
    private static final Map<Stage, Deque<Screen>> history = new WeakHashMap<>();

    private record Screen(
            Scene scene,
            String title,
            boolean resizable
    ) {}

    private Navigation() {}

    static void goTo(Stage stage, Consumer<Stage> screen)
    {
        if (stage.getScene() != null)
        {
            history.computeIfAbsent(stage, ignored -> new ArrayDeque<>())
                    .push(new Screen(
                            stage.getScene(),
                            stage.getTitle(),
                            stage.isResizable()
                    ));
        }

        screen.accept(stage);
    }

    static void goBack(Stage stage)
    {
        Deque<Screen> screens = history.get(stage);
        if (screens == null || screens.isEmpty())
        {
            return;
        }

        Screen previousScreen = screens.pop();
        stage.setScene(previousScreen.scene());
        stage.setTitle(previousScreen.title());
        stage.setResizable(previousScreen.resizable());

        if (screens.isEmpty())
        {
            history.remove(stage);
        }
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
                goTo(stage, nextStage -> {
                    if (CurrentUserSession.isManager()) {
                        new MyListingsEventManager().start(nextStage);
                    } else {
                        new MyListingsEventGoer().start(nextStage);
                    }
                })
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label unreadBadge = new Label();
        unreadBadge.setVisible(false);
        unreadBadge.setManaged(false);
        unreadBadge.setMinSize(18, 18);
        unreadBadge.setAlignment(Pos.CENTER);
        unreadBadge.setStyle(
                "-fx-background-color: #d32f2f; -fx-text-fill: white; " +
                        "-fx-font-size: 10px; -fx-font-weight: bold; " +
                        "-fx-background-radius: 10; -fx-padding: 1 4;"
        );
        StackPane notificationGraphic = new StackPane(createNotificationIcon(), unreadBadge);
        notificationGraphic.setMinSize(30, 30);
        notificationGraphic.setPrefSize(30, 30);
        StackPane.setAlignment(unreadBadge, Pos.TOP_RIGHT);
        Button notificationButton = createHeaderButton(notificationGraphic, "Notifications");

        Button profileButton =
                createHeaderButton(
                        createProfileIcon(),
                        "Profile"
                );

        notificationButton.setOnAction(event ->
                showNotificationsPopup(notificationButton, unreadBadge)
        );
        refreshUnreadBadge(unreadBadge);
        Timeline badgeRefresh = new Timeline(
                new KeyFrame(Duration.seconds(20), event -> refreshUnreadBadge(unreadBadge)));
        badgeRefresh.setCycleCount(Timeline.INDEFINITE);
        badgeRefresh.play();

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
            Session.logout();

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
            Button notificationButton,
            Label unreadBadge
    )
    {
        VBox menu = createPopupMenu();
        menu.setMinWidth(300);
        menu.setMaxWidth(380);

        Label title =
                new Label("Notifications");

        title.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #222222;"
        );

        menu.getChildren().addAll(
                title,
                new Separator()
        );

        showPopup(
                menu,
                notificationButton
        );
        loadNotifications(menu, unreadBadge);
    }

    private static void loadNotifications(VBox menu, Label unreadBadge)
    {
        Long accountId = CurrentUserSession.getAccountId();
        if (accountId == null) {
            menu.getChildren().add(new Label("Sign in to view notifications."));
            return;
        }

        Label loading = new Label("Loading notifications...");
        menu.getChildren().add(loading);
        CompletableFuture.supplyAsync(() -> NotificationApiClient.list(accountId))
                .whenComplete((notifications, error) -> Platform.runLater(() -> {
                    menu.getChildren().remove(loading);
                    if (error != null) {
                        menu.getChildren().add(new Label(
                                "Notifications are unavailable. Please try again later."));
                        return;
                    }
                    if (notifications.isEmpty()) {
                        menu.getChildren().add(new Label("No notifications."));
                    } else {
                        notifications.forEach(notification ->
                                menu.getChildren().add(createNotificationCard(
                                        notification, accountId, menu, unreadBadge)));
                    }
                    refreshUnreadBadge(unreadBadge);
                }));
    }

    private static VBox createNotificationCard(
            NotificationApiClient.Notification notification,
            long accountId,
            VBox menu,
            Label unreadBadge
    )
    {
        Label heading = new Label(notification.title());
        heading.setWrapText(true);
        heading.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        Label message = new Label(notification.message());
        message.setWrapText(true);
        message.setStyle("-fx-font-size: 13px; -fx-text-fill: #444444;");

        HBox actions = new HBox(8);
        if (!notification.read()) {
            Button markRead = createPopupButton("Mark read");
            markRead.setOnAction(event -> runNotificationAction(
                    () -> NotificationApiClient.markRead(notification.id(), accountId),
                    menu, unreadBadge));
            actions.getChildren().add(markRead);
        } else {
            Button clear = createPopupButton("Clear");
            clear.setOnAction(event -> runNotificationAction(
                    () -> NotificationApiClient.clear(notification.id(), accountId),
                    menu, unreadBadge));
            actions.getChildren().add(clear);
        }

        VBox card = new VBox(5, heading, message, actions);
        card.setPadding(new Insets(8, 0, 8, 0));
        card.setStyle("-fx-border-color: transparent transparent #eeeeee transparent;");
        return card;
    }

    private static void runNotificationAction(
            Runnable action,
            VBox menu,
            Label unreadBadge
    )
    {
        CompletableFuture.runAsync(action)
                .whenComplete((ignored, error) -> Platform.runLater(() -> {
                    if (error != null) {
                        menu.getChildren().add(new Label(
                                "Could not update notification. Please try again."));
                    } else {
                        menu.getChildren().remove(2, menu.getChildren().size());
                        loadNotifications(menu, unreadBadge);
                    }
                    refreshUnreadBadge(unreadBadge);
                }));
    }

    private static void refreshUnreadBadge(Label unreadBadge)
    {
        Long accountId = CurrentUserSession.getAccountId();
        if (accountId == null) {
            unreadBadge.setVisible(false);
            unreadBadge.setManaged(false);
            return;
        }
        CompletableFuture.supplyAsync(() -> NotificationApiClient.unreadCount(accountId))
                .whenComplete((count, error) -> Platform.runLater(() -> {
                    if (error != null || count == null || count <= 0) {
                        unreadBadge.setVisible(false);
                        unreadBadge.setManaged(false);
                    } else {
                        unreadBadge.setText(count > 99 ? "99+" : Long.toString(count));
                        unreadBadge.setVisible(true);
                        unreadBadge.setManaged(true);
                    }
                }));
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
