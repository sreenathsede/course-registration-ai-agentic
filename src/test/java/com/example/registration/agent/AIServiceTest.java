package com.example.registration.agent;

import com.example.registration.agent.tools.Tools;
import com.example.registration.service.CourseRegistrationService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AIServiceTest {
    @Test void preflightRecordsAllowListedToolCallsWhenEligible() {
        var ai = new AIService(new Tools(new CourseRegistrationService("JAVA-101", 2)));
        var result = ai.assessEnrollment("JAVA-101", true);
        assertTrue(result.eligibleToProceed());
        assertTrue(ai.getSkillInstructions().contains("Allowed workflow"));
        assertEquals(2, result.toolTrace().size());
        assertEquals("checkPrerequisite", result.toolTrace().get(0).toolName());
        assertEquals("getOfferingStatus", result.toolTrace().get(1).toolName());
    }
    @Test void rejectsIneligibleLearnerBeforeStatusLookup() {
        var ai = new AIService(new Tools(new CourseRegistrationService("JAVA-101", 2)));
        var result = ai.assessEnrollment("JAVA-101", false);
        assertFalse(result.eligibleToProceed());
        assertEquals(1, result.toolTrace().size());
    }
    @Test void unknownOfferingCannotProceed() {
        var ai = new AIService(new Tools(new CourseRegistrationService("JAVA-101", 2)));
        assertFalse(ai.assessEnrollment("OTHER", true).eligibleToProceed());
    }
}
