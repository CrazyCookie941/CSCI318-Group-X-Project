package com.csci318.groupx.registrationservice;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Pane;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class AppPreferences {
    private static final Preferences PREFERENCES =
            Preferences.userNodeForPackage(AppPreferences.class);
    private static final Map<Node, String> ORIGINAL_STYLES = new WeakHashMap<>();
    private static final Map<Node, String> ORIGINAL_ACCESSIBLE_HELP = new WeakHashMap<>();
    private static final Pattern FONT_SIZE =
            Pattern.compile("(-fx-font-size:\\s*)(\\d+(?:\\.\\d+)?)px");
    private static final Pattern COLOR = Pattern.compile("#[0-9a-fA-F]{6}");

    private AppPreferences() {
    }

    static boolean highContrast() {
        return PREFERENCES.getBoolean("accessibility.highContrast", false);
    }

    static void setHighContrast(boolean enabled) {
        PREFERENCES.putBoolean("accessibility.highContrast", enabled);
    }

    static boolean assistiveDescriptions() {
        return PREFERENCES.getBoolean("accessibility.assistiveDescriptions", false);
    }

    static void setAssistiveDescriptions(boolean enabled) {
        PREFERENCES.putBoolean("accessibility.assistiveDescriptions", enabled);
    }

    static int textScale() {
        return PREFERENCES.getInt("display.textScale", 100);
    }

    static void setTextScale(int percent) {
        PREFERENCES.putInt("display.textScale", percent);
    }

    static String theme() {
        return PREFERENCES.get("display.theme", "Light");
    }

    static void setTheme(String theme) {
        PREFERENCES.put("display.theme", theme);
    }

    static boolean showAssistantShortcut() {
        return PREFERENCES.getBoolean("features.assistantShortcut", true);
    }

    static void setShowAssistantShortcut(boolean enabled) {
        PREFERENCES.putBoolean("features.assistantShortcut", enabled);
    }

    static boolean showUnreadBadge() {
        return PREFERENCES.getBoolean("features.unreadBadge", true);
    }

    static void setShowUnreadBadge(boolean enabled) {
        PREFERENCES.putBoolean("features.unreadBadge", enabled);
    }

    static boolean resizable() {
        return PREFERENCES.getBoolean("display.resizable", true);
    }

    static void setResizable(boolean enabled) {
        PREFERENCES.putBoolean("display.resizable", enabled);
    }

    static String preferredPaymentProvider(Long accountId) {
        return accountPreferences(accountId).get("payment.provider", "VISA");
    }

    static void setPreferredPaymentProvider(Long accountId, String provider) {
        accountPreferences(accountId).put("payment.provider", provider);
    }

    static void applyDisplaySettings(Scene scene) {
        if (scene == null) {
            return;
        }
        if (scene.getWindow() instanceof javafx.stage.Stage stage) {
            stage.setResizable(resizable());
        }
        applyDisplaySettings(scene.getRoot());
    }

    private static void applyDisplaySettings(Node node) {
        if (node == null) {
            return;
        }
        String original = ORIGINAL_STYLES.computeIfAbsent(node, ignored -> node.getStyle());
        String styled = scaleFonts(original == null ? "" : original, textScale());
        styled = applyPalette(styled, "Dark".equals(theme()), highContrast());
        if (node instanceof TextInputControl) {
            styled += " -fx-background-color: #ffffff;"
                    + " -fx-text-fill: #000000;"
                    + " -fx-prompt-text-fill: #555555;";
        }
        node.setStyle(styled);

        if (node instanceof Labeled labeled) {
            if (!ORIGINAL_ACCESSIBLE_HELP.containsKey(node)) {
                ORIGINAL_ACCESSIBLE_HELP.put(node, node.getAccessibleHelp());
            }
            String originalHelp = ORIGINAL_ACCESSIBLE_HELP.get(node);
            if (assistiveDescriptions() && originalHelp == null) {
                String text = labeled.getText();
                if (text != null && !text.isBlank()) {
                    node.setAccessibleHelp(text);
                }
            } else {
                node.setAccessibleHelp(originalHelp);
            }
        }

        if (node instanceof Pane pane) {
            pane.getChildren().forEach(AppPreferences::applyDisplaySettings);
        } else if (node instanceof javafx.scene.control.ScrollPane scrollPane) {
            applyDisplaySettings(scrollPane.getContent());
        } else if (node instanceof javafx.scene.control.TitledPane titledPane) {
            applyDisplaySettings(titledPane.getContent());
        }
    }

    private static String scaleFonts(String style, int percent) {
        Matcher matcher = FONT_SIZE.matcher(style);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            double scaled = Double.parseDouble(matcher.group(2)) * percent / 100.0;
            matcher.appendReplacement(result, Matcher.quoteReplacement(
                    matcher.group(1) + String.format(java.util.Locale.ROOT, "%.1fpx", scaled)));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String applyPalette(String style, boolean dark, boolean highContrast) {
        Map<String, String> palette = dark
                ? highContrast
                        ? Map.ofEntries(
                                Map.entry("#fafafa", "#000000"), Map.entry("#f7f9fb", "#000000"),
                                Map.entry("#f7f7f7", "#000000"), Map.entry("#ffffff", "#000000"),
                                Map.entry("#fcfcfc", "#000000"), Map.entry("#fdfdfd", "#000000"),
                                Map.entry("#f3f3f3", "#000000"), Map.entry("#ededed", "#000000"),
                                Map.entry("#eeeeee", "#000000"), Map.entry("#e9e9e9", "#000000"),
                                Map.entry("#f0f0f0", "#000000"), Map.entry("#e0e0e0", "#ffffff"),
                                Map.entry("#dcdcdc", "#ffffff"), Map.entry("#cccccc", "#ffffff"),
                                Map.entry("#777777", "#ffffff"), Map.entry("#666666", "#ffffff"),
                                Map.entry("#555555", "#ffffff"), Map.entry("#444444", "#ffffff"),
                                Map.entry("#333333", "#ffffff"), Map.entry("#222222", "#ffffff"),
                                Map.entry("#111111", "#ffffff"),
                                Map.entry("#000000", "#ffffff"),
                                Map.entry("#d9d9d9", "#333333"))
                        : Map.ofEntries(
                        Map.entry("#fafafa", "#171717"), Map.entry("#f7f9fb", "#171717"),
                        Map.entry("#f7f7f7", "#202020"), Map.entry("#ffffff", "#242424"),
                        Map.entry("#fcfcfc", "#2b2b2b"), Map.entry("#fdfdfd", "#2b2b2b"),
                        Map.entry("#f3f3f3", "#202020"), Map.entry("#ededed", "#303030"),
                        Map.entry("#eeeeee", "#303030"), Map.entry("#e9e9e9", "#303030"),
                        Map.entry("#f0f0f0", "#303030"), Map.entry("#e0e0e0", "#707070"),
                        Map.entry("#dcdcdc", "#707070"), Map.entry("#cccccc", "#888888"),
                        Map.entry("#777777", "#dddddd"), Map.entry("#666666", "#dddddd"),
                        Map.entry("#555555", "#eeeeee"), Map.entry("#444444", "#f5f5f5"),
                        Map.entry("#333333", "#f5f5f5"), Map.entry("#222222", "#ffffff"),
                        Map.entry("#111111", "#ffffff"),
                        Map.entry("#000000", "#f5f5f5"),
                        Map.entry("#d9d9d9", "#3a3a3a"))
                : highContrast
                        ? Map.ofEntries(
                                Map.entry("#fafafa", "#ffffff"), Map.entry("#f7f9fb", "#ffffff"),
                                Map.entry("#f7f7f7", "#ffffff"), Map.entry("#ffffff", "#ffffff"),
                                Map.entry("#fcfcfc", "#ffffff"), Map.entry("#fdfdfd", "#ffffff"),
                                Map.entry("#f3f3f3", "#ffffff"), Map.entry("#ededed", "#ffffff"),
                                Map.entry("#eeeeee", "#ffffff"), Map.entry("#e9e9e9", "#ffffff"),
                                Map.entry("#f0f0f0", "#ffffff"), Map.entry("#e0e0e0", "#000000"),
                                Map.entry("#dcdcdc", "#000000"), Map.entry("#cccccc", "#000000"),
                                Map.entry("#777777", "#000000"), Map.entry("#666666", "#000000"),
                                Map.entry("#555555", "#000000"), Map.entry("#444444", "#000000"),
                                Map.entry("#333333", "#000000"), Map.entry("#222222", "#000000"),
                                Map.entry("#111111", "#000000"))
                        : Map.of();
        Matcher matcher = COLOR.matcher(style);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String replacement = palette.getOrDefault(
                    matcher.group().toLowerCase(java.util.Locale.ROOT), matcher.group());
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static Preferences accountPreferences(Long accountId) {
        if (accountId == null) {
            return PREFERENCES.node("anonymous");
        }
        return PREFERENCES.node("accounts").node(Long.toString(accountId));
    }

    static void setBaseStyle(Node node, String lightStyle) {
        ORIGINAL_STYLES.put(node, lightStyle);
        String styled = scaleFonts(lightStyle, textScale());
        styled = applyPalette(styled, "Dark".equals(theme()), highContrast());
        node.setStyle(styled);
    }
}
