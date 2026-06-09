package com.rioni.lk.api;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password1 = "password123";
        String password2 = "securePassword456";
        
        String hash1 = encoder.encode(password1);
        String hash2 = encoder.encode(password2);
        
        System.out.println("Password 1: " + password1);
        System.out.println("Hash 1: " + hash1);
        System.out.println();
        System.out.println("Password 2: " + password2);
        System.out.println("Hash 2: " + hash2);
    }
}