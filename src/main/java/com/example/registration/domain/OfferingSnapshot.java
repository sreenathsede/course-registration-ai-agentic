package com.example.registration.domain;

import java.util.List;

/**
 * Read-only view of the single offering and its current registrations.
 */
public record OfferingSnapshot(String offeringId, int capacity, List<String> enrolledLearners,
                               List<String> waitlistedLearners, int availableSeats) {
}
