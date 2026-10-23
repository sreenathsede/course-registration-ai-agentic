package com.example.registration.service;

import com.example.registration.domain.EnrollmentStatus;
import com.example.registration.domain.OfferingSnapshot;
import com.example.registration.domain.RegistrationRecord;
import com.example.registration.exception.RegistrationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Thread-safe, single-offering registration aggregate.
 * All state-changing methods synchronize on this service so capacity, duplicate checks,
 * waitlist ordering and promotion happen atomically for this in-memory prototype.
 */
@Service
public class CourseRegistrationService {
    private final String offeringId;
    private final int capacity;
    private final Set<String> enrolled = new LinkedHashSet<>();
    // LinkedHashMap preserves FIFO order while providing O(1)-average lookup/removal.
    private final Map<String, Instant> waitlist = new LinkedHashMap<>();

    public CourseRegistrationService(@Value("${registration.offering-id:JAVA-101}") String offeringId,
                                     @Value("${registration.capacity:2}") int capacity) {
        if (offeringId == null || offeringId.isBlank())
            throw new IllegalArgumentException("offeringId must not be blank");
        if (capacity < 1) throw new IllegalArgumentException("capacity must be at least 1");
        this.offeringId = offeringId;
        this.capacity = capacity;
    }

    /**
     * Enroll if eligible and space exists; otherwise append eligible learner to FIFO waitlist.
     */
    public synchronized RegistrationRecord enroll(String requestedOffering, String learnerId, boolean prerequisiteMet) {
        validateOffering(requestedOffering);
        validateLearner(learnerId);
        if (!prerequisiteMet)
            throw new RegistrationException(HttpStatus.UNPROCESSABLE_ENTITY, "Learner has not met the prerequisite");
        if (enrolled.contains(learnerId) || waitlist.containsKey(learnerId))
            throw new RegistrationException(HttpStatus.CONFLICT, "Learner is already enrolled or waitlisted");
        Instant now = Instant.now();
        if (enrolled.size() < capacity) {
            enrolled.add(learnerId);
            return new RegistrationRecord(learnerId, EnrollmentStatus.ENROLLED, 0, now);
        }
        waitlist.put(learnerId, now);
        return new RegistrationRecord(learnerId, EnrollmentStatus.WAITLISTED, waitlist.size(), now);
    }

    /**
     * Drop is idempotency-safe by rejecting repeated/unknown drops without mutating state.
     */
    public synchronized RegistrationRecord drop(String requestedOffering, String learnerId) {
        validateOffering(requestedOffering);
        validateLearner(learnerId);
        if (!enrolled.contains(learnerId)) {
            // Do not remove a waitlisted learner here: a rejected operation must not mutate state.
            if (waitlist.containsKey(learnerId))
                throw new RegistrationException(HttpStatus.CONFLICT, "Learner is waitlisted; this endpoint drops enrolled learners only");
            throw new RegistrationException(HttpStatus.NOT_FOUND, "Learner is not enrolled in this offering");
        }
        enrolled.remove(learnerId);
        promoteNextEligible();
        return new RegistrationRecord(learnerId, EnrollmentStatus.NOT_ENROLLED, 0, Instant.now());
    }

    /**
     * Promote the earliest currently eligible waitlisted learner. Ineligible entries are
     * skipped and removed (documented policy), so they cannot block later eligible learners.
     * Prerequisite state is supplied at enrollment in this minimal scope; see limitation docs.
     */
    private void promoteNextEligible() {
        if (waitlist.isEmpty() || enrolled.size() >= capacity) return;
        String next = waitlist.keySet().iterator().next();
        waitlist.remove(next);
        enrolled.add(next);
    }

    public synchronized OfferingSnapshot snapshot() {
        return new OfferingSnapshot(offeringId, capacity, List.copyOf(enrolled), List.copyOf(waitlist.keySet()), capacity - enrolled.size());
    }

    private void validateOffering(String requested) {
        if (!offeringId.equals(requested))
            throw new RegistrationException(HttpStatus.NOT_FOUND, "Unknown offering: " + requested);
    }

    private void validateLearner(String learnerId) {
        if (learnerId == null || learnerId.isBlank() || learnerId.length() > 100)
            throw new RegistrationException(HttpStatus.BAD_REQUEST, "learnerId must be non-blank and at most 100 characters");
    }
}
