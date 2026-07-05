<!--
SYNC IMPACT REPORT
==================
Version change: [TEMPLATE] → 1.0.0
Modified principles: N/A (initial ratification from template)
Added sections:
  - I. Clean Architecture
  - II. BDD Testing Strategy (NON-NEGOTIABLE)
  - III. Programming Best Practices
  - IV. API First
  - V. Quality Gates & Coverage
  - Code Quality Standards
  - Development Workflow
  - Governance
Removed sections: All placeholder template tokens replaced
Templates requiring updates:
  ✅ .specify/templates/plan-template.md — Constitution Check section aligned
  ✅ .specify/templates/spec-template.md — Acceptance Scenarios already use BDD format; no change needed
  ✅ .specify/templates/tasks-template.md — Test tasks and phases already compatible; no change needed
Follow-up TODOs: None — all placeholders resolved
-->

# Dropshipping Service Constitution

## Core Principles

### I. Clean Architecture (NON-NEGOTIABLE)

The codebase MUST follow the Clean Architecture model as defined by Robert C. Martin.

- **Layers**: The system MUST be organized into four concentric layers: Entities (enterprise business rules), Use Cases (application business rules), Interface Adapters (controllers, presenters, gateways), and Frameworks & Drivers (databases, web, UI).
- **Dependency Rule**: Source code dependencies MUST always point inward. Nothing in an inner circle may know about something in an outer circle.
- **Entities**: MUST contain only pure business logic with zero framework or infrastructure dependencies.
- **Use Cases**: MUST orchestrate entities; MUST NOT depend on delivery mechanisms (HTTP, messaging) or infrastructure (DB, external APIs) directly.
- **Interface Adapters**: MUST translate data between the format convenient for use cases and the format convenient for external agencies (e.g., REST controllers, JPA repositories implementing domain ports).
- **Frameworks & Drivers**: MUST be treated as implementation details; replaceable without touching inner layers.
- **No framework leakage**: Annotations or classes from frameworks (Spring, JPA, etc.) MUST NOT appear in Entities or Use Cases.

**Rationale**: Keeping business logic independent of frameworks, databases, and delivery mechanisms maximizes testability, replaceability, and longevity of the system.

### II. BDD Testing Strategy (NON-NEGOTIABLE)

All automated tests MUST be written using Behavior-Driven Development (BDD) with the Given-When-Then structure.

- **Unit Tests**: MUST cover every class/component in the Entities and Use Cases layers. Each test MUST follow the Given-When-Then pattern.
- **Integration Tests**: MUST validate the interaction between Interface Adapters and Frameworks & Drivers layers (e.g., repository against real or containerized DB, controller against the full Spring context).
- **Functional/Acceptance Tests**: MUST validate complete end-to-end user scenarios as described in the spec's Acceptance Scenarios. These tests exercise the system from the API contract level down.
- **Test-First discipline**: For every new use case or entity behavior, tests MUST be written before implementation code (Red → Green → Refactor).
- **BDD tooling**: Use Cucumber or equivalent (e.g., JUnit 5 + `@DisplayName` with Given/When/Then naming) to express scenarios in business language.
- **Scenario traceability**: Every Acceptance Scenario in `spec.md` MUST map 1:1 to at least one functional test.

**Rationale**: BDD aligns tests with business value, ensures requirements are testable by definition, and creates living documentation understood by both technical and non-technical stakeholders.

### III. Programming Best Practices (NON-NEGOTIABLE)

All production code MUST adhere to the following principles:

- **SOLID**:
  - *Single Responsibility*: Every class/module MUST have exactly one reason to change.
  - *Open/Closed*: Classes MUST be open for extension, closed for modification.
  - *Liskov Substitution*: Subtypes MUST be substitutable for their base types without altering program correctness.
  - *Interface Segregation*: Interfaces MUST be small and client-specific; no client MUST be forced to depend on methods it does not use.
  - *Dependency Inversion*: High-level modules MUST NOT depend on low-level modules; both MUST depend on abstractions.
- **YAGNI (You Aren't Gonna Need It)**: Features or abstractions MUST NOT be implemented until they are demonstrably needed. Speculative generality is prohibited.
- **DRY (Don't Repeat Yourself)**: Every piece of knowledge MUST have a single, unambiguous, authoritative representation in the codebase. Duplication detected in code reviews MUST be refactored before merge.

**Rationale**: These principles reduce coupling, increase cohesion, and lower the cost of change over the software's lifetime.

### IV. API First

All external-facing APIs MUST be designed and contracted before implementation begins.

- **OpenAPI Contract Required**: Every API endpoint MUST be described in an OpenAPI 3.x specification file (`openapi.yml` or `openapi.yaml`) located in the feature's `contracts/` directory before any implementation task starts.
- **openapi-generator Mandatory**: Server stubs and/or client SDKs MUST be generated from the OpenAPI contract using `openapi-generator-cli`. Hand-written controllers MUST implement generated interfaces; they MUST NOT diverge from the contract.
- **Contract as Single Source of Truth**: Request/response models, validation rules, and HTTP status codes defined in the OpenAPI spec are authoritative. Implementation MUST conform to the spec, not the other way around.
- **Breaking changes**: Any breaking change to an existing OpenAPI contract (removing fields, changing types, removing endpoints) MUST be versioned (e.g., `/v2/...`) and MUST NOT modify the existing contract in place.
- **Contract review gate**: No implementation PR MUST be merged if the OpenAPI contract has not been reviewed and approved.

**Rationale**: API First decouples design from implementation, enables parallel front-end/back-end development, and ensures the contract is always the authoritative reference for consumers.

### V. Quality Gates & Coverage

No code MUST be merged to the main branch unless the following metrics are satisfied:

- **Per-class coverage**: Every class in the `src/main` source set MUST have a JaCoCo line/branch coverage of **> 80%**.
- **Global coverage**: The overall project line/branch coverage as reported by JaCoCo MUST be **≥ 80%**.
- **JaCoCo reporting**: The Maven/Gradle build MUST include the JaCoCo plugin configured to:
  - Generate HTML, XML, and CSV reports on every build.
  - Fail the build (`check` goal / `jacocoTestCoverageVerification` task) if either threshold is not met.
- **Report location**: JaCoCo reports MUST be generated at `target/site/jacoco/` (Maven) or `build/reports/jacoco/` (Gradle) and committed artifacts MUST include coverage badges where applicable.
- **Exclusions policy**: Exclusions from coverage (e.g., generated code, DTOs) MUST be explicitly declared in the JaCoCo configuration and justified in the PR description.

**Rationale**: Automated coverage gates prevent regression in test discipline, make coverage visible on every build, and enforce the team's commitment to testability.

## Code Quality Standards

- **Static analysis**: The build pipeline MUST include a static analysis tool (e.g., Checkstyle, SpotBugs, SonarQube) with a zero-new-violations policy on findings of severity HIGH or CRITICAL.
- **Code formatting**: A consistent code formatter (e.g., google-java-format, Spotless) MUST be enforced in CI; PRs that fail formatting checks MUST NOT be merged.
- **Dependency management**: All third-party library versions MUST be declared in a centralized BOM (Bill of Materials) or dependency management block; no ad-hoc version overrides in submodules.
- **No dead code**: Unused classes, methods, fields, and imports MUST be removed before merging. Static analysis tools MUST enforce this.

## Development Workflow

- **Feature branches**: All work MUST happen on a feature branch named `###-feature-name` derived from the feature spec.
- **PR required**: No direct commits to `main`/`master`. Every change MUST go through a pull request.
- **CI gate**: The full build (compile → unit tests → integration tests → functional tests → JaCoCo check → static analysis) MUST pass in CI before a PR can be merged.
- **OpenAPI contract first**: For any PR that introduces or modifies an API, the OpenAPI contract change MUST be included and approved before implementation tasks begin.
- **BDD scenarios drive acceptance**: PRs MUST reference the spec's Acceptance Scenarios and confirm corresponding functional tests are green.
- **Architecture compliance**: Every PR MUST verify compliance with the Clean Architecture Dependency Rule. Dependency violations detected by ArchUnit or equivalent MUST fail the build.

## Governance

This Constitution supersedes all other development practices and informal team agreements. It is binding for all contributors and enforced via automated tooling wherever possible.

**Amendment procedure**:
1. Propose the amendment in writing, referencing the principle(s) affected and the motivation.
2. Amendment requires approval from at least one architect and one senior engineer.
3. Approved amendments MUST include a migration plan for existing code/tests that violates the new rule.
4. Increment `CONSTITUTION_VERSION` following semantic versioning (MAJOR for removals/redefinitions, MINOR for additions, PATCH for clarifications).
5. Update `LAST_AMENDED_DATE` on every ratified change.

**Compliance reviews**: Every sprint retrospective MUST include a brief review of constitution violations found during the sprint and whether rule refinement is warranted.

**Guidance files**: Use `.specify/memory/constitution.md` as the authoritative runtime reference for all agents and team members.

**Version**: 1.0.0 | **Ratified**: 2026-07-04 | **Last Amended**: 2026-07-04
