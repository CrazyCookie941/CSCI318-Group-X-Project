package com.csci318.groupx.registrationservice;

import javafx.scene.image.Image;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

final class EventImageLoader {

    private EventImageLoader() {
    }

    static Image load(String location) {
        String imageUrl = resolveUrl(location);
        if (imageUrl == null) {
            return null;
        }

        Image image = new Image(imageUrl);
        return image.isError() ? null : image;
    }

    static String resolveUrl(String location) {
        if (location == null || location.isBlank()) {
            return null;
        }

        try {
            if (location.startsWith("/")) {
                URL resource = EventImageLoader.class.getResource(location);
                if (resource != null) {
                    return resource.toExternalForm();
                }
            }

            Path imagePath = Path.of(location);
            return Files.isRegularFile(imagePath)
                    ? imagePath.toUri().toString()
                    : null;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
