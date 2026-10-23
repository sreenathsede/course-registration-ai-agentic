# AI Engineering Activity Log

This log is a transparent example record for the assessment deliverable; it is not a claim that an external model was called at runtime by this application.

## Prompt 1 — design decomposition
**Prompt:** “Translate the course-registration assessment into a minimal Spring Boot design. Cover capacity, duplicate prevention, FIFO waitlist promotion, invalid drops, concurrency, tests, scope boundaries, and a clear AI-use record.”

**Relevant response summary:** Separate API transport from a synchronized domain service; use insertion-ordered collections for FIFO; test invalid operations and concurrent enrollments; document limitations.

**Disposition:** Accepted with edits. The design was narrowed to one offering and no database/UI/payment features.

**Verification:** Compared implementation against `docs/ACCEPTANCE_TRACEABILITY.md`; tests assert capacity, FIFO behavior and no mutation on failed drops.

## Prompt 2 — agentic boundary and safety
**Prompt:** “Add an explicit skill file, AIService orchestrator and Tools interface/class, but keep critical registration state deterministic and protected from free-form model output.”

**Relevant response summary:** Use skill instructions and allow-listed read-only tools; keep enrollment/drop writes in the domain service; log structured tool results.

**Disposition:** Accepted with edits. No hosted LLM was added because the assessment must run locally without secrets and the supplied PDF does not mandate a specific provider.

**Verification:** `AIServiceTest` checks the prerequisite tool trace and that ineligible applicants cannot proceed; domain tests independently verify final invariants.

## Evidence note
For a formal submission, replace or supplement this illustrative prompt log with the actual prompts, generated outputs, dates, and reviewer decisions from the AI tool used during development. Do not represent this file as provider-generated audit telemetry.
