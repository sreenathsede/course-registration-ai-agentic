# Limitations and Production Considerations

- State is in memory and lost on restart.
- Synchronization only protects one JVM; multiple replicas require transactional persistence and database constraints/locking.
- Prerequisite status is a request field for assessment simplicity, not a trusted authorization source. Production must resolve it server-side.
- Eligibility is assumed stable after enrollment; if prerequisites can be revoked, promotion must revalidate and define skip/retain policy.
- No identity/authentication, rate limiting, audit database, metrics, or distributed messaging.
- `AIService` is a local deterministic agentic orchestrator with allow-listed tools, not a hosted generative model integration. The assessment PDF's specific AICDL expansion/framework version was not available.
