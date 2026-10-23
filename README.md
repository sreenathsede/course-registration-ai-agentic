# Course Registration — AI-Assisted / Agentic Assessment

Minimal Java 17 + Spring Boot REST project for **one offering** (`JAVA-101` by default). It includes an explicit `AIService`, allow-listed `Tools`, and a `skill.md` file loaded by `AIService` as the versioned agent operating contract. Core registration decisions remain deterministic and synchronized; no external LLM/API key is required.

## Prerequisites
- JDK 17+
- Maven 3.8+

## Run
```bash
mvn clean test
mvn spring-boot:run
```

## API examples
Enroll (eligible learner):
```bash
curl -X POST http://localhost:8080/api/offerings/JAVA-101/enroll \
  -H 'Content-Type: application/json' \
  -d '{"learnerId":"learner-001","prerequisiteMet":true}'
```
Status:
```bash
curl http://localhost:8080/api/offerings/JAVA-101
```
Drop enrolled learner:
```bash
curl -X DELETE http://localhost:8080/api/offerings/JAVA-101/enrollments/learner-001
```

Default capacity is 2. Override with `REGISTRATION_CAPACITY` only if mapped through your deployment configuration, or edit `application.properties`. Enrollment returns `ENROLLED` or `WAITLISTED`; invalid prerequisite returns HTTP 422, duplicate enrollment HTTP 409, unknown offering/learner drop HTTP 404, malformed learner ID HTTP 400.

## Agentic/AICDL implementation
- **Skill:** `src/main/resources/skills/skill.md` defines scope, tool rules, safe workflow and output constraints.
- **AIService:** preflight orchestrator. It calls `checkPrerequisite` and `getOfferingStatus`, retaining a trace of tool results.
- **Tools:** explicit allow-listed, read-only tool methods. State mutation stays inside the domain service.
- **Agentic safety:** tool calls are bounded and deterministic; no free-form model output can modify registration state.
- **AICDL evidence:** see `docs/AICDL_AGENTIC_WORKFLOW.md` and `docs/AI_PROMPT_LOG.md`. The project uses an explicit lifecycle-inspired workflow; it does not claim conformance to an external proprietary framework whose exact rubric/spec was not supplied.

## Business policy
- One learner can be enrolled or waitlisted only once.
- Ineligible learners are rejected and never added to the waitlist.
- Eligible learners are enrolled if seats remain; otherwise appended FIFO.
- Dropping an enrolled learner promotes the earliest waitlisted learner.
- Duplicate/unknown drops return an error and do not change enrollment state.
- All state mutations are synchronized for in-process concurrency.

## Limitations
This is an in-memory, single-process prototype: restart clears state; no persistent database, authentication, external student-information-system prerequisite lookup, multi-offering catalog, or production distributed lock. The prerequisite boolean is supplied by the caller for the assessment only and must be replaced with a trusted server-side lookup in production. See `docs/LIMITATIONS.md`.
