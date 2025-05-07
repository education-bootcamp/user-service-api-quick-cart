package com.devstack.system.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class PrefixGenerator {
    // Method to generate a random number string of given length
    public static String generateRandomNumberString(int length) {
        // StringBuilder to build the random number string
        StringBuilder sb = new StringBuilder(length);

        // Random object to generate random numbers
        Random random = new Random();

        // Loop to generate each digit of the random number string
        for (int i = 0; i < length; i++) {
            // Generate a random digit (0-9) and append it to the StringBuilder
            sb.append(random.nextInt(10));
        }

        // Return the generated random number string
        return sb.toString();
    }
}
