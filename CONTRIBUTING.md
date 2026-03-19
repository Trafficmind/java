# Contributing

## Prerequisites

- Java 17+
- Maven 3.8+

## Local Development

```bash
mvn -q test
mvn -q verify
```

## Pull Requests

1. Create a branch from `main`
2. Add/adjust tests for behavior changes
3. Ensure `mvn -q verify` passes
4. Open a PR using the provided template

## Commit Guidance

- Keep commits focused and atomic
- Explain behavior changes in commit messages
- Avoid unrelated refactors in the same PR

Preferred commit message convention:

- `feat: ...` new behavior (minor release)
- `fix: ...` bug fix (patch release)
- `docs: ...` documentation-only change
- `refactor: ...` internal change without behavior change
- `test: ...` tests only
- `chore: ...` build/infra/automation

## Security

For security vulnerabilities, follow `SECURITY.md` and avoid public disclosure before patching.
