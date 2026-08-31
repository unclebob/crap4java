# crap4java Specification

## 1. Purpose

`crap4java` is a CRAP metric analyzer for Java projects.

It shall:

- locate Java source files to analyze
- generate JaCoCo coverage for the owning Maven module of each analyzed file set
- parse Java methods and estimate cyclomatic complexity
- combine complexity and coverage into CRAP scores
- print a tabular report sorted by worst score first
- fail when the maximum CRAP score exceeds the configured threshold

`crap4java` is intended as a project-quality gate rather than a mutation tool.

## 2. Scope

This specification defines:

- the command-line contract
- source file selection rules
- coverage generation behavior
- method parsing behavior
- CRAP score computation
- report ordering and exit codes

This specification does not define:

- non-Maven execution
- support for non-Java source files
- a machine-readable report format
- configurable thresholds through the CLI

## 3. Terminology

- `project root`
  The working root from which `crap4java` is invoked.

- `module root`
  The nearest ancestor directory of an analyzed file that contains `pom.xml`. If none exists below the project root, the project root is the module root.

- `method metric`
  A single report row consisting of method identity, cyclomatic complexity, coverage, and CRAP score.

- `coverage N/A`
  The state where no JaCoCo XML was found for the module and therefore coverage could not be assigned to a method.

## 4. Command-Line Interface

### 4.1 Supported Forms

The tool shall support these forms:

- `crap4java`
- `crap4java --changed`
- `crap4java <path...>`
- `crap4java --help`

### 4.2 Mode Semantics

- no arguments
  Analyze all Java source files under `src/`.

- `--changed`
  Analyze changed Java source files under `src/`.

- `<path...>`
  For each explicit path:
  - if it is a file, analyze that file
  - if it is a directory, analyze all Java files under that directory's `src/` subtree

- `--help`
  Print usage text and exit successfully.

### 4.3 Invalid Usage

The tool shall exit with usage error when argument parsing fails.

The tool shall print usage text on CLI usage failure.

### 4.4 Test Command Override

The tool shall support an optional `--test-command <cmd>` flag, combinable with any form in §4.1 except `--help`.

When present, `<cmd>` replaces the test-execution step described in §7.2 as the mechanism that exercises the module's tests. Coverage instrumentation and JaCoCo report generation remain Maven-based; only test execution itself is substituted. This exists for projects whose tests are not run through `mvn test` (see §7.2.1).

`--test-command` shall require a value; omitting one is a usage error per §4.3.

## 5. File Selection Rules

### 5.1 Default Source Discovery

In default mode, the tool shall analyze all `.java` files under:

- `<project-root>/src/**`

### 5.2 Changed-File Discovery

In `--changed` mode, the tool shall:

- invoke `git status --porcelain`
- interpret modified, added, and untracked Java files
- retain only `.java` files under `<project-root>/src/`
- sort the resulting file list in path order

### 5.3 Explicit Paths

When explicit paths are supplied:

- file paths shall be analyzed directly
- directory paths shall be expanded to `.java` files under `<dir>/src/**`
- duplicates shall be removed
- the final list shall be sorted in path order

### 5.4 Empty Selection

If no Java files are selected after expansion and filtering:

- the tool shall print `No Java files to analyze.`
- the tool shall exit successfully

## 6. Module Grouping

The tool shall group selected files by module root before coverage generation.

The tool shall determine the module root for a file by walking upward from the file's directory until:

- a `pom.xml` file is found, or
- the walk leaves the project root

If no nearer `pom.xml` is found, the project root shall be used as the module root.

Coverage generation and JaCoCo XML lookup shall occur once per module group.

## 7. Coverage Pipeline

For each module group, the tool shall:

1. delete stale coverage artifacts
2. run Maven tests with JaCoCo report generation
3. read the resulting JaCoCo XML report
4. analyze the selected Java files in that module

### 7.1 Stale Artifact Cleanup

Before coverage generation, the tool shall delete stale module-local coverage artifacts, including:

- `target/site/jacoco/`
- `target/jacoco.exec`

### 7.2 Maven Coverage Command

Coverage generation shall invoke Maven against the module root and generate JaCoCo XML for that module.

#### 7.2.1 Overridden Test Command

When `--test-command` (§4.4) is supplied, the tool shall:

1. resolve the JaCoCo runtime agent jar for the module (caching it under the module's `target/` so repeated runs do not re-resolve it)
2. run `<cmd>` with the JaCoCo agent attached via the `JAVA_TOOL_OPTIONS` environment variable, so any JVM `<cmd>` launches contributes coverage
3. invoke Maven's JaCoCo report goal against the module root to produce the JaCoCo XML report

A non-zero exit from `<cmd>` shall fail the run per §14 before the report goal runs.

### 7.3 Missing Coverage XML

If the expected JaCoCo XML file does not exist after coverage generation:

- the tool shall print a warning to stderr
- coverage for methods in that module shall be reported as `N/A`

## 8. Java Method Parsing

The tool shall parse Java source using the JDK compiler tree APIs.

The parser shall identify concrete method declarations and their basic attributes, including:

- class name
- method name
- source location
- cyclomatic complexity

The parser shall not require full semantic resolution of sibling or external symbols in order to extract methods.

### 8.1 Exclusions

The method parser shall ignore:

- constructors
- abstract methods
- anonymous-class methods

### 8.2 Complexity Counting

Cyclomatic complexity shall be computed from method bodies using Java syntax structure rather than regex-only parsing.

The resulting complexity shall be an integer `CC >= 1` for concrete methods.

## 9. Coverage Attribution

Coverage shall be attributed to methods by matching parsed methods to JaCoCo coverage data.

If an exact coverage entry for a method cannot be found, the tool may use the nearest appropriate available coverage entry according to its implemented lookup rules.

If no usable coverage data is available for a method:

- coverage shall be reported as `N/A`
- CRAP score shall be reported as `N/A`

## 10. CRAP Formula

For methods with known coverage, CRAP shall be computed as:

`CRAP = CC^2 * (1 - coverage)^3 + CC`

Where:

- `CC` is cyclomatic complexity
- `coverage` is the method coverage fraction in the range `0.0..1.0`

Coverage shall be derived from JaCoCo `INSTRUCTION` counters.

## 11. Report

The tool shall print a tabular report containing, at minimum:

- method name
- class name
- cyclomatic complexity
- coverage percentage or `N/A`
- CRAP score or `N/A`

The report shall be sorted by CRAP descending.

Methods with `N/A` CRAP shall appear after methods with numeric CRAP.

## 12. Threshold

The CRAP threshold shall be `8.0`.

The tool shall determine the maximum numeric CRAP value in the result set.

If the maximum numeric CRAP value is greater than `8.0`:

- the tool shall print `CRAP threshold exceeded: <max> > 8.0` to stderr
- the tool shall exit with threshold-failure status

If no numeric CRAP values exist:

- the maximum shall be treated as `0.0`
- the threshold shall not be considered exceeded

## 13. Exit Codes

The tool shall use these exit codes:

- `0`
  Successful analysis, including empty selection or all scores at or below threshold.

- `1`
  CLI usage error.

- `2`
  CRAP threshold exceeded.

## 14. Error Handling

The tool shall fail fast on:

- invalid command-line usage
- coverage command failure
- unreadable source files
- parser failures that prevent analysis

Warnings about missing JaCoCo XML shall not by themselves fail the run.

## 15. Non-Goals

The current implementation is not required to support:

- configurable thresholds via CLI
- non-Maven builds
- directory recursion outside `src/` discovery rules
- mutation analysis
- machine-readable output formats

## 16. Conformance

An implementation conforms to this specification if it satisfies the CLI, file selection, module grouping, coverage generation, method analysis, CRAP computation, reporting, and exit-code rules above for Java projects built with Maven.
