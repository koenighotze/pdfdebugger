# Agent Instructions for PDF Debugger

This document provides context and guidelines for AI coding assistants working on the PDF Debugger project.

## Project Overview

**PDF Debugger** is a Java command-line tool that pre-stamps PDF forms by filling all text fields with their field names and flattening the result. This helps developers debug and understand PDF form structures.

### Key Information
- **Language**: Java 21 (LTS)
- **Build Tool**: Maven 3.6.3+
- **Main Entry Point**: `org.koenighotze.pdftool.PdfToolCli`
- **Core Library**: Apache PDFBox 3.0.6
- **Architecture**: Layered (CLI → Service → Business Logic → Library Adapter)

## Project Structure

```
src/main/java/org/koenighotze/pdftool/
├── PdfToolCli.java           # CLI entry point, argument parsing
├── PdfTool.java              # File I/O orchestration
└── stamper/
    ├── Stamper.java          # Core PDF stamping logic
    └── PDFieldFacade.java    # PDFBox adapter/wrapper

src/test/java/                # Mirror structure with Test suffix
src/test/resources/           # Test PDF files
```

## Architecture Principles

### Separation of Concerns
- **CLI Layer**: Handles user interaction, argument parsing (Commons CLI)
- **Service Layer**: Manages file operations and orchestration
- **Business Logic**: PDF processing and form manipulation
- **Adapter Layer**: Abstracts PDFBox API complexity

### Key Design Patterns
- **Facade Pattern**: `PDFieldFacade` simplifies PDFBox's `PDField` API
- **Fail-Safe Behavior**: Continue processing even if individual fields fail
- **Resource Management**: Use try-with-resources for all PDF operations

## Development Guidelines

### Code Style

#### Java Conventions
- Use Java 21 features when appropriate (records, pattern matching, sealed classes)
- Follow standard Java naming conventions (camelCase for methods/variables, PascalCase for classes)
- Prefer immutability: use `final` for variables, prefer records over mutable POJOs
- Use descriptive names: `ParseConfiguration` not `Config`, `stampable()` not `canStamp()`

#### Error Handling
```java
// GOOD: Log and continue for non-critical failures
try {
    field.setValue(fieldName);
} catch (IOException e) {
    log.warn("Cannot stamp field with key '{}' because of: {}", fieldName, e.getMessage());
    // Continue processing other fields
}

// BAD: Don't fail entire operation for single field errors
if (!field.isStampable()) {
    throw new RuntimeException("Field not stampable"); // Too harsh!
}
```

#### Logging
- Use SLF4J API (`org.slf4j.Logger`)
- Log levels:
  - **INFO**: Field processing, file operations
  - **WARN**: Recoverable errors, skipped operations
  - **ERROR**: Fatal errors requiring user intervention
- Include context: field names, file paths, error reasons

### Testing Practices

#### Test Organization
- Unit tests for individual components (`PDFieldFacadeTest`, `StamperTest`)
- Integration tests with real PDFs (`PdfToolTest`)
- Keep test PDFs small and focused in `src/test/resources/`

#### Test Naming
```java
// GOOD: Descriptive test names
@Test
void shouldStampTextFieldWithFieldName() { }

@Test
void shouldSkipNonTextFields() { }

// BAD: Unclear test names
@Test
void testStamp() { }
```

#### Mocking Strategy
- Mock PDFBox objects (`PDField`, `PDDocument`) for unit tests
- Use real PDFs for integration tests
- Don't mock what you don't own (except for complex external libraries like PDFBox)

### Maven Best Practices

#### Running Commands
```bash
# Always use wrapper for consistency
./mvnw clean test              # Run tests
./mvnw clean package           # Build JAR
./mvnw verify                  # Full build + checks

# Don't use system Maven
mvn clean test                 # AVOID
```

#### Dependency Management
- Keep dependencies up-to-date (Dependabot enabled)
- Use `dependencyManagement` for version control (e.g., Log4j BOM)
- Justify all dependencies: PDFBox (core), Commons CLI (CLI), Log4j (logging), BouncyCastle (PDF crypto)

### Docker & Containerization

#### Jib Configuration
- Uses Jib Maven plugin for containerization
- Base image: `eclipse-temurin:21-jre` (must match Java version)
- No Dockerfile needed (Jib handles everything)
- Build container: `./mvnw jib:dockerBuild`

#### Container Best Practices
- Keep base image version in sync with Java version
- Use JRE (not JDK) for smaller runtime image
- Set appropriate JVM flags (currently: `-Xms512m`)

## Common Tasks

### Adding a New Dependency
1. Add to `<dependencies>` section in `pom.xml`
2. Use version properties for consistency: `<foo.version>x.y.z</foo.version>`
3. Run `./mvnw clean test` to verify
4. Update this document if it's a major dependency

### Modifying PDF Processing Logic
1. Start with unit test in `StamperTest`
2. Implement in `Stamper.java` or `PDFieldFacade.java`
3. Test with real PDF in `PdfToolTest`
4. Ensure graceful degradation (log warning, don't fail)

### Upgrading Java Version
1. Update `maven.compiler.source` and `maven.compiler.target` in `pom.xml`
2. Update Jib base image: `eclipse-temurin:{VERSION}-jre`
3. Check BouncyCastle compatibility (use jdk18on for Java 18+)
4. Run full test suite: `./mvnw clean test`
5. Build and verify: `./mvnw clean package`

### Adding CLI Options
1. Add option in `PdfToolCli.createOptions()`
2. Parse in `PdfToolCli.parse()`
3. Update `ParseConfiguration` record if needed
4. Add test in `PdfToolCliTest`
5. Update help text (auto-generated by Commons CLI)

## PDFBox Specific Guidelines

### Working with PDFBox
- Always close `PDDocument` (use try-with-resources)
- AcroForms may be null (check before use)
- Not all fields are text fields (check type via facade)
- Form flattening makes fields non-editable (intended behavior)

### Field Types
```java
// Text field (stampable): "Tx"
// Button field (skip): "Btn"
// Signature field (skip): "Sig"
// Choice field (check if needed): "Ch"
```

### Common Pitfalls
```java
// GOOD: Check for null
PDDocument document = Loader.loadPDF(bytes);
PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
if (acroForm == null) {
    throw new IllegalArgumentException("PDF has no form");
}

// BAD: Assume form exists
PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
acroForm.getFields(); // NullPointerException!
```

## Code Quality Standards

### Before Committing
- [ ] All tests pass: `./mvnw clean test`
- [ ] Code builds: `./mvnw clean package`
- [ ] No new deprecation warnings (check compiler output)
- [ ] Appropriate logging added
- [ ] Error handling implemented (fail-safe where possible)

### Code Review Checklist
- [ ] Follows existing code style
- [ ] Unit tests added/updated
- [ ] Integration test if needed
- [ ] No hardcoded values (use constants or configuration)
- [ ] Resource cleanup (try-with-resources)
- [ ] Descriptive variable/method names

## Security Considerations

### PDF Processing
- **Memory**: Large PDFs can cause OOM; consider size limits
- **Malicious PDFs**: PDFBox handles most attacks, but validate inputs
- **Temp Files**: Clean up temporary files (use `deleteOnExit()`)
- **Path Traversal**: Validate file paths from user input

### Dependencies
- BouncyCastle for cryptographic operations
- Keep dependencies updated (Dependabot monitors)
- Trivy scans for vulnerabilities in CI

## Performance Guidelines

### Optimization Tips
- PDFBox is CPU-intensive; one PDF at a time is sufficient
- Memory usage scales with PDF size; larger PDFs need more heap
- Flattening is expensive; expect ~100ms - 1s per PDF depending on complexity

### Don't Prematurely Optimize
- This is a CLI tool, not a high-throughput service
- Readability > Performance for this use case
- Focus on correctness and maintainability

## Troubleshooting

### Common Issues

**Tests fail with "no forms found"**
- Ensure test PDFs in `src/test/resources/` have AcroForms
- Check PDF structure with Adobe Acrobat or similar

**Build fails with "Java version mismatch"**
- Verify Java 21 is installed: `java -version`
- Check JAVA_HOME points to Java 21
- Use Maven wrapper: `./mvnw` not `mvn`

**Docker build fails**
- Ensure base image matches Java version in pom.xml
- Check Jib plugin version is recent
- Verify Docker daemon is running

**Field not being stamped**
- Check field type (only text fields are stamped)
- Look for warnings in logs
- Verify field has a fully qualified name

## Git Workflow

### Branch Naming
- Feature: `feature/description` or `description`
- Bug fix: `fix/description`
- Dependency updates: Dependabot creates `dependabot/maven/...`

### Commit Messages
- Use descriptive commit messages
- Reference issue numbers if applicable: `Fix #123: Description`
- Multi-line format for significant changes:
  ```
  Brief summary (50 chars)

  Detailed explanation of what and why.
  Include breaking changes, migration notes, etc.
  ```

### Pull Requests
- Ensure all CI checks pass
- Include test results in PR description
- Update documentation if behavior changes

## CI/CD

### GitHub Actions
- Runs on every push and PR
- Executes: `./mvnw clean verify`
- Trivy security scanning for vulnerabilities
- Dependabot for dependency updates

### Release Process
- Version in `pom.xml`: `3.5-SNAPSHOT` (development)
- Update version for release (remove SNAPSHOT)
- Build Docker image: `./mvnw jib:build`
- Tag release: `git tag v3.5.0`

## Documentation

### When to Update Docs
- New CLI options → Update help text (auto-generated)
- Architecture changes → Update this file
- API changes → Update JavaDoc
- New features → Consider README update

### JavaDoc Standards
- Public APIs require JavaDoc
- Include `@param`, `@return`, `@throws`
- Explain *why*, not just *what*

## Resources

### Dependencies Documentation
- [PDFBox 3.x](https://pdfbox.apache.org/3.0/)
- [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/)
- [Log4j 2](https://logging.apache.org/log4j/2.x/)
- [BouncyCastle](https://www.bouncycastle.org/java.html)

### Tools
- Maven Wrapper: `./mvnw -h`
- Jib: `./mvnw jib:help`
- GitHub CLI: `gh` (for PR management)

## Questions?

For questions or clarifications:
- Check existing tests for examples
- Review similar code in the codebase
- Consult PDFBox documentation
- Ask the human developer for domain knowledge

---

**Last Updated**: 2025-11-14 (Java 21 migration)
