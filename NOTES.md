# Patch Notes

## Summary of changes
- **SQL Operator Precedence**: Corrected `AND`/`OR` operator precedence in `TaskRepository.java`, `db/queries/search_tasks.sql`, and `db/oracle/task_search_package.sql` by explicitly grouping `title` and `description` conditions in parentheses. Fixed archived task leakage and bypassed status filters.
- **Removed Artificial Delay**: Removed arbitrary `Thread.sleep()` in `TaskController.java` to eliminate thread-blocking latency, replacing `System.out.println` with structured SLF4J logging.
- **Request Parameter Validation**: Added boundary checks in `TaskController.java` for `page >= 1`, `1 <= pageSize <= 100`, and validated `status` against `TaskStatus` enum to reject invalid parameters with HTTP 400.
- **Frontend Debouncing & State Management**: Added a custom `useDebounce` hook (300ms) to eliminate keystroke request flooding, reset `page` to 1 upon search/status filter changes, guarded hook updates against unmounted/stale renders, and ensured `loading` resets in `finally`.
- **Automated Tests**: Added regression integration tests in `TaskControllerIntegrationTest` covering all query edge cases, status combinations, archived exclusion, and parameter validation.

## What I chose not to change
- **In-Memory Pagination**: Retained `subList` pagination rather than refactoring to Spring Data `Pageable` or database-level `LIMIT`/`OFFSET`. Modifying repository method signatures and query return types was deemed an unnecessary architectural disruption for the current scope.
- **Core Schema and Contracts**: Maintained existing H2 database configuration, entity mappings, startup commands, and API response contracts.

## Biggest remaining risk
- **Memory Pressure from In-Memory Slicing**: `TaskRepository.searchTasks()` loads every matching row into memory before slicing with `subList()`. On large datasets (10,000+ tasks), this will increase garbage collection pauses and degrade throughput. A database-level pagination query should be implemented before high-volume production use.

## Tools used
- Used GenAI assistance to inspect SQL precedence edge cases, draft integration test assertions, and structure the `useDebounce` hook.
- Manually verified all fixes against H2 database behavior, reviewed code diffs, and ensured strict adherence to project constraints.
