package com.csci318.groupx.notificationservice;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.application.Platform;

public class NotificationPopup
{
    public static void showInvalidInputError()
    {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Input Error");
        alert.setHeaderText(null); // Removes the default header space
        alert.setContentText("Invalid input! Please check your fields.");
        alert.showAndWait();
    }

    public static void main(String[] args)
    {
        Platform.startup(NotificationPopup::showInvalidInputError);
    }
}


