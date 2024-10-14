package com.sage.izpack;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generate GUID (X3 Services Setup)
 * 
 * @author Franck DEPOORTERE
 */
public class GeneratePasswordHelper {
    private static final String CHAR_LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPERCASE = CHAR_LOWERCASE.toUpperCase();
    private static final String DIGIT = "0123456789";
    private static final String OTHER_SPECIAL = "!@#$%^&*()_+-={}:<>?,./~`";

    public static String generateStrongPassword(int length) {
        StringBuilder result = new StringBuilder(length);

        // At least 2 chars (lowercase)
        String strLowerCase = generateRandomString(CHAR_LOWERCASE, 2);
        result.append(strLowerCase);

        // At least 2 chars (uppercase)
        String strUppercaseCase = generateRandomString(CHAR_UPPERCASE, 2);
        result.append(strUppercaseCase);

        // At least 2 digits
        String strDigit = generateRandomString(DIGIT, 2);
        result.append(strDigit);

        // At least 2 special characters
        String strSpecialChar = generateRandomString(OTHER_SPECIAL, 2);
        result.append(strSpecialChar);

        // Remaining characters (random)
        String strOther = generateRandomString(getAllowedChars(length - 8), length - 8);
        result.append(strOther);

        // Shuffle the password for added randomness
        String shuffledPassword = shuffleString(result.toString());

        return shuffledPassword;
    }

    private static String generateRandomString(String input, int size) {
        if (input == null || input.length() <= 0) {
            throw new IllegalArgumentException("Invalid input.");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Invalid size.");
        }

        StringBuilder result = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            int index = new SecureRandom().nextInt(input.length());
            result.append(input.charAt(index));
        }
        return result.toString();
    }

    private static String shuffleString(String input) {
        List<Character> chars = new ArrayList<>();
        for (char c : input.toCharArray()) {
            chars.add(c);
        }
        Collections.shuffle(chars);
        StringBuilder result = new StringBuilder();
        for (char c : chars) {
            result.append(c);
        }
        return result.toString();
    }

    private static String getAllowedChars(int length) {
        StringBuilder allowedChars = new StringBuilder();
        allowedChars.append(CHAR_LOWERCASE).append(CHAR_UPPERCASE).append(DIGIT).append(OTHER_SPECIAL);
        return allowedChars.toString();
    }
}
