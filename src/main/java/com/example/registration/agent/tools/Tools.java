package com.example.registration.agent.tools;

import com.example.registration.domain.OfferingSnapshot;
import com.example.registration.service.CourseRegistrationService;
import org.springframework.stereotype.Component;

/**
 * Explicit, allow-listed tools exposed to the lightweight agent.
 * Tools are read-only by design: enrollment and dropping remain deterministic domain actions.
 */
@Component
public class Tools {
    private final CourseRegistrationService registrationService;

    public Tools(CourseRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    public ToolResult getOfferingStatus(String offeringId) {
        OfferingSnapshot snapshot = registrationService.snapshot();
        if (!snapshot.offeringId().equals(offeringId))
            return new ToolResult("getOfferingStatus", false, "Unknown offering", null);
        return new ToolResult("getOfferingStatus", true, "Offering status retrieved", snapshot);
    }

    /**
     * Local deterministic prerequisite check used in lieu of an external student system.
     */
    public ToolResult checkPrerequisite(boolean prerequisiteMet) {
        return new ToolResult("checkPrerequisite", true,
                prerequisiteMet ? "Prerequisite satisfied" : "Prerequisite not satisfied", prerequisiteMet);
    }
}
