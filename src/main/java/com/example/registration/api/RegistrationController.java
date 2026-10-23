package com.example.registration.api;

import com.example.registration.agent.AIService;
import com.example.registration.domain.OfferingSnapshot;
import com.example.registration.domain.RegistrationRecord;
import com.example.registration.exception.RegistrationException;
import com.example.registration.service.CourseRegistrationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/offerings")
public class RegistrationController {
    private final CourseRegistrationService service;
    private final AIService aiService;

    public RegistrationController(CourseRegistrationService service, AIService aiService) {
        this.service = service;
        this.aiService = aiService;
    }

    @PostMapping("/{offeringId}/enroll")
    public RegistrationRecord enroll(@PathVariable String offeringId, @Valid @RequestBody EnrollmentRequest request) {
        // Agent preflight is advisory and auditable; the domain service rechecks all invariants.
        var assessment = aiService.assessEnrollment(offeringId, request.prerequisiteMet());
        if (!assessment.eligibleToProceed()) {
            if (!request.prerequisiteMet()) return service.enroll(offeringId, request.learnerId(), false);
            throw new RegistrationException(HttpStatus.NOT_FOUND, assessment.explanation());
        }
        return service.enroll(offeringId, request.learnerId(), request.prerequisiteMet());
    }

    @DeleteMapping("/{offeringId}/enrollments/{learnerId}")
    public RegistrationRecord drop(@PathVariable String offeringId, @PathVariable String learnerId) {
        return service.drop(offeringId, learnerId);
    }

    @GetMapping("/{offeringId}")
    public OfferingSnapshot status(@PathVariable String offeringId) {
        var snapshot = service.snapshot();
        if (!snapshot.offeringId().equals(offeringId))
            throw new RegistrationException(HttpStatus.NOT_FOUND, "Unknown offering: " + offeringId);
        return snapshot;
    }

    public record EnrollmentRequest(@NotBlank @Size(max = 100) String learnerId, boolean prerequisiteMet) {
    }
}
