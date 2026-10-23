# Acceptance Criteria Traceability

| Assessment requirement | Implementation / evidence |
|---|---|
| Enroll/drop for one offering | `CourseRegistrationService`; `RegistrationController` |
| Prerequisite eligibility | `AIService` + `Tools.checkPrerequisite`; domain service rejects false flag |
| Full offering uses waitlist | `CourseRegistrationService.enroll` |
| FIFO promotion on drop | `promoteNextEligible`; FIFO `LinkedHashMap` |
| Duplicate learner prevented | conflict check across enrolled and waitlisted |
| Invalid/repeated drop does not corrupt state | errors before promotion; service tests |
| Concurrency test if practical | `CourseRegistrationServiceTest.concurrentEnrollmentsNeverExceedCapacity` |
| AI/agentic engineering artifact | `AIService`, `Tools`, `skill.md`, prompt log and workflow record |
| Design / run instructions / limitations | `docs/DESIGN.md`, `README.md`, `docs/LIMITATIONS.md` |

The separate `Course-Registration-Rubric-Score.md` mentioned by the assessment PDF was not included among the available files. This project therefore addresses the PDF scope and does not claim to satisfy unseen rubric-specific naming or scoring rules.
