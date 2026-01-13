// Create this as a simple main class to generate the correct password hash
// File: src/main/java/com/banksystem/util/PasswordHashGenerator.java

package com.banksystem.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "password123";
        String hash = encoder.encode(password);

        System.out.println("=================================");
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println("=================================");

        // Test the hash
        boolean matches = encoder.matches(password, hash);
        System.out.println("Hash verification: " + matches);

        // Test against your existing hash
        String existingHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd7QA8qkrPm";
        boolean existingMatches = encoder.matches(password, existingHash);
        System.out.println("Existing hash verification: " + existingMatches);
    }
}