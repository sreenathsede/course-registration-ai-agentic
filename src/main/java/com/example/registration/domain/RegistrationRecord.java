package com.example.registration.domain;

import java.time.Instant;

/**
 * Immutable response/domain snapshot; no mutable internal collections are exposed.
 */
public record RegistrationRecord(String learnerId, EnrollmentStatus status, long queuePosition, Instant createdAt) {
}
