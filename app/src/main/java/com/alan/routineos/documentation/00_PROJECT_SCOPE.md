# 00_PROJECT_SCOPE.md

# RoutineOS v2 --- Project Scope & Engineering Constitution

**Version:** 1.0\
**Status:** Active

## 1. Vision

RoutineOS is a local-first Android application focused on planning,
execution and analysis of routines. The project prioritizes
maintainability, modularity and long-term scalability.

## 2. Objectives

-   Local-first architecture.
-   Clear separation of concerns.
-   Reusable Design System.
-   AI-assisted development.
-   Stable engineering workflow.

## 3. Architecture

-   MVVM
-   Clean Architecture principles
-   Feature-first organization
-   Repository Pattern
-   Hilt
-   Room
-   Jetpack Compose
-   Navigation Compose

Business logic must never depend on UI.

## 4. Project Philosophy

Every feature must be modular, testable, documented and independently
maintainable.

## 5. Single Responsibility Rule

Every file has one responsibility.

Recommended file size: - Ideal: 150--200 lines - Acceptable: up to 300
lines - Above 300 lines requires refactoring unless justified.

Avoid God Objects.

## 6. Function Complexity

Prefer functions under 30 lines. Avoid deep nesting. Extract reusable
logic.

## 7. Package Responsibilities

Each package owns one domain. Never mix business logic between features.

## 8. UI Rules

Screens render state. ViewModels coordinate logic. Repositories provide
data. Persistence is isolated.

## 9. Code Quality

Prioritize readability, maintainability, scalability and consistency.
Avoid duplicated logic.

## 10. Development Workflow

Roadmap → Engineering Card → Implementation → Audit → Approval → Merge.

## 11. Git Strategy

Protected branches: - main - develop

Working branches: - feature/* - fix/* - docs/* - refactor/* - chore/\*

One branch represents one functional objective.

## 12. AI Agent Rules

Before changing code: 1. Read project documentation. 2. Read
PROJECT_STATUS. 3. Continue the first EC IN_PROGRESS. 4. Otherwise
select the first PENDING EC. 5. Work only on that EC.

## 13. Documentation

Every feature includes implementation notes, architecture decisions,
audit results and completion status.

## 14. Definition of Done

Implementation complete. Build succeeds. Documentation updated. EC
updated. Commit created. Ready for review.

## 15. Non-Negotiable Rules

-   Never break develop.
-   Never modify unrelated files.
-   Keep modules cohesive.
-   Preserve Design System consistency.
-   Prefer extension over rewrite.

## 16. Long-Term Goal

RoutineOS must evolve as an engineering project that can be safely
maintained by both humans and AI agents using standardized documentation
and workflows.
