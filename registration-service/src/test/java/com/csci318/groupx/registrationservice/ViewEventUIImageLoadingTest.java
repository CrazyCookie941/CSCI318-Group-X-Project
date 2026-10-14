package com.csci318.groupx.registrationservice;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ViewEventUIImageLoadingTest {

    @Test
    void resolvesUploadedAbsoluteFilePaths() throws Exception {
        Path image = Files.createTempFile("event-image", ".jpg");
        try {
            String resolved = EventImageLoader.resolveUrl(image.toString());

            assertEquals(image.toUri().toString(), resolved);
        } finally {
            Files.deleteIfExists(image);
        }
    }

    @Test
    void resolvesBundledClasspathImages() {
        var resource = EventImageLoader.class.getResource("/Images/Bingo_night.jpg");
        assertNotNull(resource);

        assertEquals(resource.toExternalForm(),
                EventImageLoader.resolveUrl("/Images/Bingo_night.jpg"));
    }

    @Test
    void returnsNoUrlForMissingImages() {
        assertNull(EventImageLoader.resolveUrl("/no/such/event-image.jpg"));
        assertNull(EventImageLoader.resolveUrl(null));
        assertNull(EventImageLoader.resolveUrl(" "));
    }
}
