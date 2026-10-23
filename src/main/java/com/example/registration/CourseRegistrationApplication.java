package com.example.registration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point. The assessment intentionally uses in-memory state.
 */
@SpringBootApplication
public class CourseRegistrationApplication {
    public static void main(String[] args) {
        SpringApplication.run(CourseRegistrationApplication.class, args);
    }
}
