package com.example.registration.service;

import com.example.registration.domain.EnrollmentStatus;
import com.example.registration.exception.RegistrationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;

class CourseRegistrationServiceTest {
    private CourseRegistrationService service;
    @BeforeEach void setUp() { service = new CourseRegistrationService("JAVA-101", 2); }

    @Test void enrollsWhenSeatAvailableAndWaitlistsWhenFull() {
        assertEquals(EnrollmentStatus.ENROLLED, service.enroll("JAVA-101", "a", true).status());
        assertEquals(EnrollmentStatus.ENROLLED, service.enroll("JAVA-101", "b", true).status());
        assertEquals(EnrollmentStatus.WAITLISTED, service.enroll("JAVA-101", "c", true).status());
        assertEquals(List.of("a", "b"), service.snapshot().enrolledLearners());
        assertEquals(List.of("c"), service.snapshot().waitlistedLearners());
    }
    @Test void rejectsDuplicateAcrossEnrolledAndWaitlisted() {
        service.enroll("JAVA-101", "a", true); service.enroll("JAVA-101", "b", true); service.enroll("JAVA-101", "c", true);
        assertThrows(RegistrationException.class, () -> service.enroll("JAVA-101", "a", true));
        assertThrows(RegistrationException.class, () -> service.enroll("JAVA-101", "c", true));
        assertEquals(1, service.snapshot().waitlistedLearners().size());
    }
    @Test void dropPromotesFifoLearner() {
        service.enroll("JAVA-101", "a", true); service.enroll("JAVA-101", "b", true);
        service.enroll("JAVA-101", "c", true); service.enroll("JAVA-101", "d", true);
        service.drop("JAVA-101", "a");
        assertEquals(List.of("b", "c"), service.snapshot().enrolledLearners());
        assertEquals(List.of("d"), service.snapshot().waitlistedLearners());
    }
    @Test void prerequisiteFailureDoesNotMutateState() {
        assertThrows(RegistrationException.class, () -> service.enroll("JAVA-101", "a", false));
        assertTrue(service.snapshot().enrolledLearners().isEmpty());
        assertTrue(service.snapshot().waitlistedLearners().isEmpty());
    }
    @Test void invalidAndRepeatedDropsDoNotCorruptState() {
        service.enroll("JAVA-101", "a", true); service.enroll("JAVA-101", "b", true); service.enroll("JAVA-101", "c", true);
        assertThrows(RegistrationException.class, () -> service.drop("JAVA-101", "missing"));
        assertEquals(List.of("a", "b"), service.snapshot().enrolledLearners());
        assertEquals(List.of("c"), service.snapshot().waitlistedLearners());
        service.drop("JAVA-101", "a");
        assertThrows(RegistrationException.class, () -> service.drop("JAVA-101", "a"));
        assertEquals(List.of("b", "c"), service.snapshot().enrolledLearners());
    }
    @Test void rejectsUnknownOfferingAndInvalidLearner() {
        assertThrows(RegistrationException.class, () -> service.enroll("OTHER", "a", true));
        assertThrows(RegistrationException.class, () -> service.enroll("JAVA-101", " ", true));
        assertTrue(service.snapshot().enrolledLearners().isEmpty());
    }
    @Test void concurrentEnrollmentsNeverExceedCapacity() throws Exception {
        int attempts = 30;
        var pool = Executors.newFixedThreadPool(10);
        CountDownLatch start = new CountDownLatch(1);
        List<Runnable> tasks = new ArrayList<>();
        for (int i=0;i<attempts;i++) { final int n=i; tasks.add(() -> { try { start.await(); service.enroll("JAVA-101", "learner-"+n, true); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }); }
        for (Runnable task : tasks) pool.submit(task);
        start.countDown(); pool.shutdown();
        assertTrue(pool.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS));
        assertEquals(2, service.snapshot().enrolledLearners().size());
        assertEquals(attempts-2, service.snapshot().waitlistedLearners().size());
    }
}
