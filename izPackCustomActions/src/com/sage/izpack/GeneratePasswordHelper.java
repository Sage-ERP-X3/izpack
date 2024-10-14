package com.sage.izpack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
			import java.util.UUID;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.core.os.RegistryHandler;

/**
 * Generate GUID (X3 Services Setup)
 * 
 * @author Franck DEPOORTERE
 */
public class PasswordGenerator {
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
