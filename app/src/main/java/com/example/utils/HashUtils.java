package com.example.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for security operations, including password hashing.
 */
public class HashUtils {

    /**
     * Hashes a string using the SHA-256 algorithm.
     * @param input The plain text input (e.g. password)
     * @return The SHA-256 hashed string in hexadecimal format
     */
    public static String hashSHA256(String input) {
        if (input == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return input; // Fallback to plain text if algorithm not found, though SHA-256 is guaranteed in Android
        }
    }
}
