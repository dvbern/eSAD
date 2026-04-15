# Project Guidelines

## Project Structure
The project consists of:
- **Frontend**: An Angular application located in the `/frontend` directory.
- **Backend**: A Quarkus-based REST API located in the `/api` directory.

## Architecture

### Layered Architecture
We follow a layered architecture. The layers and their responsibilities are:
- **Resource**: REST endpoints and request/response handling.
- **Mapper**: Mapping between DTOs and Entities.
- **Service**: Business logic and orchestration.
- **Repo**: Data access and persistence.
- **Entity**: Database entities.

See `LayeringTest.java` for ArchUnit rules enforcing these boundaries.

### Feature Grouping
Features are grouped into packages. Each feature package typically contains its own Resource, Service, Repo, and Entity classes. Cross-feature access should generally be done via Service interfaces.

---

## Testing

### Assertions
- Use **AssertJ** for all assertions to ensure readable and fluent test code.

### Test Coverage & Execution
- Maintain a **high test coverage** on the server side.
- Changes should usually be verified by **unit tests** or **QuarkusTests**.
- For bigger changes, run all applicable tests to ensure no regressions are introduced.

### Test Data Helpers
- Use builder classes to create test data. These are typically named with a `Builder` suffix.
- Example: `KindBuilder` provides methods to create default or customized `Kind` entities and DTOs.
- These builders are often located in `testutil.builder` packages.

---

## Code Style
- Follow the existing code style and naming conventions.
- Maintain consistency with the surrounding codebase.
