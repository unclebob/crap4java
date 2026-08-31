# crap4java

`crap4java` is a standalone CRAP metric tool for Java projects, modeled after `crap4clj`.

It combines method cyclomatic complexity with JaCoCo method coverage and reports CRAP scores.
On each run it deletes stale coverage artifacts, runs coverage, then analyzes the selected files.

## Formula

`CRAP = CC^2 * (1 - coverage)^3 + CC`

- `CC` is cyclomatic complexity.
- `coverage` is method coverage fraction from JaCoCo `INSTRUCTION` counters.

## Coverage Pipeline

For each invocation:

1. Delete stale coverage artifacts:
   - `target/site/jacoco/`
   - `target/jacoco.exec`
2. Run `mvn -q org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent test org.jacoco:jacoco-maven-plugin:0.8.12:report`
3. Read `target/site/jacoco/jacoco.xml`
4. Analyze selected Java files

### Projects That Don't Run Tests Through Maven

Pass `--test-command <cmd>` to run `<cmd>` instead of step 2's `mvn ... test`. This
is for projects whose tests aren't run via `mvn test` (a dedicated test runner
invoked directly with `java`, for example). `crap4java` still resolves and attaches
the JaCoCo runtime agent to `<cmd>` via `JAVA_TOOL_OPTIONS` (so any JVM it launches
contributes real coverage, not just an `mvn test`-driven one), then runs the JaCoCo
`report` goal afterward. `<cmd>`'s exit code still determines pass/fail.

## Build and Test

```bash
mvn test
```

## Run

Build the jar:

```bash
mvn -DskipTests package
```

From the project root you want to analyze:

```bash
java -jar target/crap4java-0.1.0-SNAPSHOT.jar
```

## CLI

```text
--help                Print usage to stdout
(no args)             Analyze all Java files under src/
--changed             Analyze changed Java files under src/
<file ...>            Analyze only these files
<directory ...>       Analyze all Java files under each directory's src/ subtree
--test-command CMD    Run CMD instead of `mvn test` (combinable with the forms above)
```

Examples:

```bash
java -jar target/crap4java-0.1.0-SNAPSHOT.jar --help
java -jar target/crap4java-0.1.0-SNAPSHOT.jar
java -jar target/crap4java-0.1.0-SNAPSHOT.jar --changed
java -jar target/crap4java-0.1.0-SNAPSHOT.jar src/main/java/demo/Sample.java
java -jar target/crap4java-0.1.0-SNAPSHOT.jar module-a module-b
java -jar target/crap4java-0.1.0-SNAPSHOT.jar --test-command "scripts/run-unit-tests.sh"
```

## Exit codes

- `0` success, threshold respected
- `1` invalid CLI usage
- `2` CRAP threshold exceeded (`> 8.0`)

## Notes

- If JaCoCo XML is missing, coverage is reported as `N/A`.
- Report output is sorted by CRAP descending, with `N/A` at the bottom.
