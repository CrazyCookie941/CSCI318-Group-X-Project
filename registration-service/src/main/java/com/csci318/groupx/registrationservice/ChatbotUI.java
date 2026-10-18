package com.csci318.groupx.registrationservice;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChatbotUI extends Application {

    private static final int MAX_MESSAGE_LENGTH = 2000;
    private static final String USER_BUBBLE_STYLE =
            "-fx-background-color: #007bc4; -fx-text-fill: white; "
                    + "-fx-padding: 12; -fx-background-radius: 12;";
    private static final String ASSISTANT_BUBBLE_STYLE =
            "-fx-background-color: white; -fx-text-fill: #222222; "
                    + "-fx-padding: 12; -fx-background-radius: 12; "
                    + "-fx-border-color: #e0e0e0; -fx-border-radius: 12;";

    private final List<ConversationMessage> conversation = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f7f9fb;");
        root.setTop(Navigation.createNavigationBar(stage));

        VBox content = new VBox(16);
        content.setPadding(new Insets(28, 36, 24, 36));
        content.setMaxWidth(900);

        Label title = new Label("AI event assistant");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        Label description = new Label(
                "Ask about events, locations, dates, categories, or prices.");
        description.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

        Label connectionStatus = new Label("Checking assistant availability…");
        connectionStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        VBox messages = new VBox(14);
        messages.setPadding(new Insets(18));
        messages.setStyle("-fx-background-color: #f7f9fb;");
        messages.setFillWidth(true);
        addMessage(messages,
                "Hi! Tell me what kind of event you’re looking for, and I’ll help you find one.",
                false);

        ScrollPane chatScroll = new ScrollPane(messages);
        chatScroll.setFitToWidth(true);
        chatScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatScroll.setStyle("-fx-background: white; -fx-background-color: white; "
                + "-fx-border-color: #e0e0e0; -fx-border-radius: 8; "
                + "-fx-background-radius: 8;");
        VBox.setVgrow(chatScroll, Priority.ALWAYS);

        TextArea messageInput = new TextArea();
        messageInput.setPromptText("Type your message… (Ctrl+Enter to send)");
        messageInput.setWrapText(true);
        messageInput.setPrefRowCount(2);
        messageInput.setMaxHeight(100);

        Button sendButton = new Button("Send");
        sendButton.setDefaultButton(true);
        sendButton.setStyle("-fx-background-color: #007bc4; -fx-text-fill: white; "
                + "-fx-font-size: 14px; -fx-font-weight: bold; "
                + "-fx-padding: 10 22; -fx-background-radius: 5; -fx-cursor: hand;");
        sendButton.setOnAction(event ->
                sendMessage(messageInput, sendButton, messages, chatScroll, connectionStatus));
        messageInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && event.isControlDown()) {
                sendMessage(messageInput, sendButton, messages, chatScroll, connectionStatus);
                event.consume();
            }
        });

        HBox composer = new HBox(12, messageInput, sendButton);
        composer.setAlignment(Pos.BOTTOM_RIGHT);
        HBox.setHgrow(messageInput, Priority.ALWAYS);

        Label lengthHint = new Label("Maximum 2,000 characters per message");
        lengthHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #777777;");

        content.getChildren().addAll(title, description, connectionStatus, chatScroll,
                composer, lengthHint);
        root.setCenter(content);
        BorderPane.setAlignment(content, Pos.TOP_CENTER);

        stage.setTitle("AI Event Assistant");
        stage.setScene(new Scene(root, 1000, 700));
        stage.setMinWidth(650);
        stage.setMinHeight(500);
        stage.show();

        CompletableFuture.supplyAsync(ChatApiClient::getStatus)
                .whenComplete((status, error) -> Platform.runLater(() -> {
                    if (error != null) {
                        connectionStatus.setText(connectionError(error));
                        connectionStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: #b3261e;");
                    } else if (status == null || !status.configured()) {
                        connectionStatus.setText(
                                "The assistant needs GROQ_API_KEY configured in event-service.");
                        connectionStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: #b3261e;");
                    } else {
                        connectionStatus.setText("Assistant ready · " + status.model());
                        connectionStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: #388e3c;");
                    }
                }));
    }

    private void sendMessage(
            TextArea messageInput,
            Button sendButton,
            VBox messages,
            ScrollPane chatScroll,
            Label connectionStatus
    ) {
        String message = messageInput.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            addMessage(messages, "Please keep each message under 2,000 characters.", false);
            scrollToBottom(chatScroll);
            return;
        }

        addMessage(messages, message, true);
        messageInput.clear();
        sendButton.setDisable(true);
        connectionStatus.setText("Thinking…");
        Label pendingReply = addMessage(messages, "Thinking…", false);
        scrollToBottom(chatScroll);

        String prompt = buildPrompt(message);
        CompletableFuture.supplyAsync(() -> ChatApiClient.send(prompt))
                .whenComplete((response, error) -> Platform.runLater(() -> {
                    sendButton.setDisable(false);
                    if (error != null) {
                        String userFacingError = chatError(error);
                        pendingReply.setText(userFacingError);
                        pendingReply.setStyle(ASSISTANT_BUBBLE_STYLE
                                + "-fx-text-fill: #b3261e;");
                        connectionStatus.setText(userFacingError);
                        connectionStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: #b3261e;");
                    } else if (response == null || response.reply() == null
                            || response.reply().isBlank()) {
                        String userFacingError = "The assistant returned an empty response. Please try again.";
                        pendingReply.setText(userFacingError);
                        connectionStatus.setText(userFacingError);
                        connectionStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: #b3261e;");
                    } else {
                        pendingReply.setText(response.reply());
                        conversation.add(new ConversationMessage("User", message));
                        conversation.add(new ConversationMessage("Assistant", response.reply()));
                        connectionStatus.setText("Assistant ready");
                        connectionStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: #388e3c;");
                    }
                    scrollToBottom(chatScroll);
                }));
    }

    private String buildPrompt(String currentMessage) {
        String currentQuestion = "Current user question:\n" + currentMessage;
        String context = "";
        for (int index = conversation.size() - 1; index >= 0; index--) {
            ConversationMessage turn = conversation.get(index);
            String addition = turn.speaker() + ": " + turn.text() + "\n";
            String candidate = addition + context;
            String prompt = "Previous conversation:\n" + candidate + "\n" + currentQuestion;
            if (prompt.length() > MAX_MESSAGE_LENGTH) {
                break;
            }
            context = candidate;
        }
        return context.isEmpty()
                ? currentMessage
                : "Previous conversation:\n" + context + "\n" + currentQuestion;
    }

    private static Label addMessage(VBox messages, String text, boolean fromUser) {
        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(640);
        bubble.setStyle(fromUser ? USER_BUBBLE_STYLE : ASSISTANT_BUBBLE_STYLE);

        HBox row = new HBox(bubble);
        row.setMaxWidth(Double.MAX_VALUE);
        row.setAlignment(fromUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messages.getChildren().add(row);
        return bubble;
    }

    private static void scrollToBottom(ScrollPane scrollPane) {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    private static String chatError(Throwable error) {
        Throwable cause = rootCause(error);
        if (cause instanceof ResourceAccessException) {
            return "Could not reach event-service. Make sure it is running on port 8081.";
        }
        if (cause instanceof RestClientResponseException response) {
            return switch (response.getStatusCode().value()) {
                case 503 -> "The chatbot is not configured. Set GROQ_API_KEY in event-service.";
                case 502 -> "The AI assistant is unavailable right now. Please try again shortly.";
                default -> "The chat request failed (HTTP "
                        + response.getStatusCode().value() + "). Please try again.";
            };
        }
        return "The chat request failed. Please try again.";
    }

    private static String connectionError(Throwable error) {
        if (rootCause(error) instanceof ResourceAccessException) {
            return "Event service is unavailable. Start it on port 8081 to use the assistant.";
        }
        return "Could not check assistant availability.";
    }

    private static Throwable rootCause(Throwable error) {
        Throwable cause = error;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    private record ConversationMessage(String speaker, String text) {
    }

    public static void main(String[] args) {
        launch(args);
    }
}
