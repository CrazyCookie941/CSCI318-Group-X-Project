package com.csci318.groupx.eventservice.service;

import org.springframework.stereotype.Component;

@Component
public class FuzzyMatcher {

    public double similarity(String first, String second) {

        if (first == null || second == null) {
            return 0.0;
        }

        first = first.toLowerCase().trim();
        second = second.toLowerCase().trim();

        if (first.equals(second)) {
            return 1.0;
        }

        if (first.isEmpty() || second.isEmpty()) {
            return 0.0;
        }

        int distance = levenshteinDistance(first, second);
        int maxLength = Math.max(first.length(), second.length());

        return 1.0 - ((double) distance / maxLength);
    }

    private int levenshteinDistance(String first, String second) {

        int[][] dp = new int[first.length() + 1][second.length() + 1];

        for (int i = 0; i <= first.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= second.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= first.length(); i++) {

            for (int j = 1; j <= second.length(); j++) {

                int cost =
                        first.charAt(i - 1) == second.charAt(j - 1)
                                ? 0
                                : 1;

                dp[i][j] = Math.min(
                        Math.min(
                                dp[i - 1][j] + 1,
                                dp[i][j - 1] + 1
                        ),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[first.length()][second.length()];
    }
}