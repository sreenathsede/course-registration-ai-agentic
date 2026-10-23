# Design Specification

## Boundary
`RegistrationController` validates HTTP input and maps API calls. `AIService` performs a read-only preflight through `Tools`. `CourseRegistrationService` owns the only mutable aggregate and is authoritative for enrollment/drop/waitlist transitions.

## State model
- `enrolled`: insertion-ordered set of learner IDs.
- `waitlist`: insertion-ordered map of learner IDs to enqueue timestamps.
- Capacity and offering ID are fixed at service startup.
- The API exposes immutable snapshots only.

## Invariants
1. `enrolled.size() <= capacity`.
2. A learner ID cannot be in both collections, or appear twice.
3. Only prerequisite-qualified learners may be enrolled/waitlisted.
4. Waitlist order is FIFO; drop frees one seat and promotes the first queued learner.
5. Invalid operations do not silently mutate state.
6. Each service mutation and snapshot is synchronized on the service instance.

## Queue policy
Ineligible applicants are rejected at enrollment, so no ineligible queue item is created. If eligibility later becomes externally mutable, a production queue processor should revalidate the first item at promotion; this prototype assumes the prerequisite result is stable after acceptance.

## Concurrency
Synchronized methods serialize operations in a single JVM, avoiding races between capacity checks and writes. This does not coordinate multiple application instances. A persistent production implementation should use a database transaction plus unique constraints and row locking/optimistic versioning.

## Trade-offs
An in-memory collection keeps the assessment minimal and easy to run. It loses state on restart, cannot coordinate across replicas, and uses caller-provided prerequisite data as a clearly marked stand-in for a trusted eligibility service.
