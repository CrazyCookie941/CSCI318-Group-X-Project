package com.csci318.groupx.accountservice.service;

import com.csci318.groupx.accountservice.domain.model.Account;
import com.csci318.groupx.accountservice.infrastructure.repository.AccountRepository;
import com.csci318.groupx.accountservice.messaging.AccountEventPublisher;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AccountProfilePictureTest {

    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final DatabaseLogService databaseLogService = mock(DatabaseLogService.class);
    private final AccountService accountService = new AccountService(
            accountRepository,
            mock(PasswordHasher.class),
            databaseLogService,
            mock(AccountEventPublisher.class)
    );

    @Test
    void savesValidProfilePictureToAccount() throws IOException {
        Account account = new Account("user", "user@example.com", "hash");
        byte[] picture = pngImage();
        when(accountRepository.findById(7L)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        assertTrue(accountService.updateAccountProfilePicture(7L, picture, "image/png"));

        assertArrayEquals(picture, account.getProfilePicture());
        org.junit.jupiter.api.Assertions.assertEquals(
                "image/png", account.getProfilePictureContentType());
        verify(accountRepository).save(account);
        verify(databaseLogService).info(
                "ACCOUNT_PROFILE_PICTURE_UPDATED",
                7L,
                "Account profile picture updated successfully."
        );
    }

    @Test
    void rejectsImageWithMismatchedContentType() throws IOException {
        byte[] picture = pngImage();

        assertThrows(IllegalArgumentException.class,
                () -> accountService.updateAccountProfilePicture(7L, picture, "image/jpeg"));

        verifyNoInteractions(accountRepository);
    }

    @Test
    void returnsFalseWhenAccountDoesNotExist() throws IOException {
        when(accountRepository.findById(7L)).thenReturn(Optional.empty());

        assertFalse(accountService.updateAccountProfilePicture(
                7L, pngImage(), "image/png"));
    }

    private static byte[] pngImage() throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
