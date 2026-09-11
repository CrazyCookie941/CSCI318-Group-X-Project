package com.csci318.groupx.paymentservice;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

//displays the form used to collect transaction details
public class TransactionUI extends Application
{

    private static final String BLACK = "#000000";
    private static final String BACKGROUND = "#d9d9d9";

    @Override
    public void start(Stage stage)
    {
        //build the shared navigation bar at the top of the transaction page
        Button homeButton = createNavigationButton("Home");
        Button eventsButton = createNavigationButton("My events");
        Button accountButton = createNavigationButton("Account");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox navigationBar = new HBox(30, homeButton, eventsButton, spacer, accountButton);
        navigationBar.setAlignment(Pos.CENTER_LEFT);
        navigationBar.setPadding(new Insets(10, 14, 10, 14));
        navigationBar.setStyle("-fx-background-color: " + BLACK + ";");

        Label title = new Label("Complete your transaction");
        title.setStyle("-fx-font-size: 24px; -fx-text-fill: black;");

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(11);
        form.setPadding(new Insets(0, 0, 0, 0));

        //add each transaction field in order from top to bottom
        addField(form, 0, "Card number:");
        addField(form, 1, "Exp. date:");
        addField(form, 2, "CVC:");
        addField(form, 3, "Email:");
        addField(form, 4, "Address:");

        VBoxWithTitle content = new VBoxWithTitle(title, form);
        //combine the navigation bar and transaction form in the page layout
        BorderPane layout = new BorderPane(content);
        layout.setTop(navigationBar);
        layout.setPadding(new Insets(27, 16, 20, 16));
        layout.setStyle("-fx-background-color: " + BACKGROUND + ";");

        stage.setTitle("Complete your transaction");
        stage.setScene(new Scene(layout, 640, 400));
        stage.show();
    }

    //creates a consistently styled navigation button
    private static Button createNavigationButton(String text)
    {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + BLACK + ";" + "-fx-text-fill: white;" + "-fx-font-size: 13px;" + "-fx-padding: 0;");
        return button;
    }

    //adds one labelled input field to the transaction form
    private static void addField(GridPane form, int row, String labelText)
    {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: black;");

        TextField field = new TextField();
        field.setPromptText("...");
        field.setPrefSize(205, 23);
        field.setStyle("-fx-background-color: " + BLACK + ";" + "-fx-text-fill: white;" + "-fx-prompt-text-fill: white;" + "-fx-font-size: 13px;" + "-fx-padding: 2px 6px;");

        form.add(label, 0, row);
        form.add(field, 1, row);
    }

    public static void main(String[] args) { launch(args); }

    //groups the transaction title and form with shared spacing and padding
    private static final class VBoxWithTitle extends javafx.scene.layout.VBox
    {
        private VBoxWithTitle(Label title, GridPane form)
        {
            super(8, title, form);
            setAlignment(Pos.TOP_LEFT);
            setPadding(new Insets(20, 8, 0, 8));
        }
    }
}
