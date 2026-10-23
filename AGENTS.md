# Repository agent guidance

Read `src/main/resources/skills/skill.md` before changing behavior. Keep the scope to one offering. Preserve invariants: no duplicates across enrolled/waitlisted, capacity is never exceeded, FIFO promotion, and failed requests do not corrupt state. Add or update tests for every behavior change. Do not use agent/LLM output as authority for eligibility or state mutation. Run `mvn clean test` before reporting completion.
