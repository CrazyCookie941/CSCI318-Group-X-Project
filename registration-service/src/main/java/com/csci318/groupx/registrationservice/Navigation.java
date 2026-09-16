package com.csci318.groupx.registrationservice;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.util.function.Consumer;

final class Navigation
{
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

        Hyperlink eventsLink = createNavigationLink("My events");
        eventsLink.setOnAction(event -> goTo(stage, nextStage -> new MyListingsEventGoer().start(nextStage)));

        Hyperlink accountLink = createNavigationLink("Account");
        accountLink.setOnAction(event -> goTo(stage, nextStage -> new AccountUI().start(nextStage)));

        Hyperlink settingsLink = createNavigationLink("Settings");
        settingsLink.setOnAction(event -> goTo(stage, nextStage -> new SettingsUI().start(nextStage)));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        navigationBar.getChildren().addAll(homeLink, eventsLink, spacer, settingsLink, accountLink);
        return navigationBar;
    }

    private static Hyperlink createNavigationLink(String text)
    {
        Hyperlink link = new Hyperlink(text);
        link.setStyle("-fx-text-fill: white; -fx-underline: false; -fx-font-size: 14px; -fx-font-weight: bold;");
        return link;
    }
}
