package com.example.registration.agent;

import com.example.registration.agent.tools.ToolResult;
import com.example.registration.agent.tools.Tools;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.Map;

/**
 * Small agentic orchestration boundary for the assessment. It loads the skill instructions,
 * selects allow-listed tools, records each tool call, and returns a grounded explanation.
 * It intentionally does not call a hosted LLM: critical registration decisions are deterministic.
 */
@Service
public class AIService {
    private final Tools tools;
    private final String skillInstructions;

    public AIService(Tools tools) {
        this.tools = tools;
        // Load the checked-in skill at startup so the agent's operating contract is versioned.
        try (var input = new ClassPathResource("skills/skill.md").getInputStream()) {
            this.skillInstructions = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Required agent skill file skills/skill.md is missing", ex);
        }
        if (skillInstructions.isBlank()) throw new IllegalStateException("Agent skill instructions must not be blank");
    }

    /**
     * Exposes the loaded skill for diagnostics and tests without allowing runtime mutation.
     */
    public String getSkillInstructions() {
        return skillInstructions;
    }

    public AgentAssessment assessEnrollment(String offeringId, boolean prerequisiteMet) {
        ToolResult prerequisite = tools.checkPrerequisite(prerequisiteMet);
        if (!(prerequisite.data() instanceof Boolean eligible) || !eligible) {
            return new AgentAssessment(false, "Prerequisite requirement is not satisfied.", List.of(prerequisite));
        }
        ToolResult status = tools.getOfferingStatus(offeringId);
        if (!status.success()) return new AgentAssessment(false, status.summary(), List.of(prerequisite, status));
        return new AgentAssessment(true, "Prerequisite is satisfied. Current offering status was checked; the registration service makes the final atomic enrollment/waitlist decision.", List.of(prerequisite, status));
    }

    public record AgentAssessment(boolean eligibleToProceed, String explanation, List<ToolResult> toolTrace) {
    }
}
