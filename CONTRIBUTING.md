# Contributing to Spring by Example V2

Thank you for contributing to **Spring by Example V2**.

The project focuses on building **small but extensive examples** across the Spring ecosystem.

---

## Quick Start

Clone the repository

```bash
git clone https://github.com/<your-username>/spring-by-example-v2.git
```

Navigate to an example

```bash
cd spring-security/*
```

Run

```bash
./mvnw spring-boot:run
```

Explore the code and README.

---

## Development Principles

Examples should be:

- Small and focused.
- Easy to run.
- Easy to understand.
- Well tested.
- Clearly documented.
- Consistent with the repository structure.

Avoid unnecessary complexity just to demonstrate a feature.

---

## Before You Start

Check the [ROADMAP.md](ROADMAP.md) to check the status and avoid working on the already finished steps.

Please check existing issues and pull requests before creating a new example.

If you plan to add a large feature or multiple examples, consider opening an issue first to discuss the idea.

---

## Module Structure

Modules should use a numbered directory:

```text
01-spring-security/
02-spring-data/
03-reactive-spring/
```

A typical module should contain:

```text
01-spring-security/
├── pom.xml
├── src/
│   ├── main/
│   └── test/
└── README.md
```

Each completed module should include a README explaining:

- What the technology is.
- Why it is useful.
- The concepts demonstrated.
- How the example works.
- How to run it.
- How it is tested.
- Important takeaways.

---

## Code Style

- Use Java 21 unless a module specifically requires another language.
- Follow standard Java naming conventions.
- Prefer clear, descriptive names.
- Keep examples focused.
- Add tests for meaningful behavior.
- Avoid unnecessary abstractions.

---

## Verification

Before submitting a change, run:

```bash
mvn clean verify
```

---

## Branch Naming Convention

To keep the repository organized, please use the following naming conventions when creating branches.

Branches should follow this format:

Use lowercase letters and separate words with hyphens (`-`).

### Feature branches

For adding new examples, features, or learning material:

Examples:
```
feature/hello-bean
feature/dependency-injection
```

### Documentation branches

For documentation changes:

Examples:
```
docs/improve-readme
docs/add-contributing-guide
```

### Bug fix branches

For fixing issues:

Examples:
```
fix/maven-buid
fix/broken-example-test
```

----

## Good Branch Name Examples

```
feature/add-bean-lifecycle-example
docs/update/learning-path
refactor/maven-parent-structure
fix/update-spring-version
```

---

## Commit Message Convention

Use clear, descriptive commit messages.

Examples:

```text
(feature): Added Spring Security authentication example
(feature): Added Spring Data repository example
(fix): Fixed Maven parent configuration
(docs): Updated Spring Security module README
```

---

## Pull Requests

Pull requests should explain:

- What was added or changed.
- Which concepts are demonstrated.
- How the change was tested.
- Any important implementation decisions.

Keep pull requests focused on one module or closely related change.

---

## After

Update the [ROADMAP.md](ROADMAP.md) to update the status and to prevent other contributors from working on the already.
finished steps.

Thank you for helping make Spring by Example V2 better!

---

## Reporting Issues

When reporting an issue, include:

- The module affected.
- The example affected.
- Steps to reproduce the problem.
- Expected behavior.
- Actual behavior.
- Relevant error messages or stack traces.
- Java and Maven versions when relevant.

---

## Learning First

This repository is primarily a learning project.

When adding an example, prefer an implementation that makes the underlying concept easy to understand over one that hides everything behind framework defaults.


#### [Back To Top ⬆️](#contributing-to-spring-by-example-v2)
