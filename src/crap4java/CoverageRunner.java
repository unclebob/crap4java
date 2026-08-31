package crap4java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

final class CoverageRunner {

    private static final String JACOCO_VERSION = "0.8.12";

    private final CommandExecutor executor;

    CoverageRunner(CommandExecutor executor) {
        this.executor = executor;
    }

    void generateCoverage(Path projectRoot, String testCommand) throws Exception {
        deleteIfExists(projectRoot.resolve("target/site/jacoco"));
        deleteIfExists(projectRoot.resolve("target/jacoco.exec"));

        if (testCommand == null) {
            run(List.of(
                    "mvn", "-q",
                    "org.jacoco:jacoco-maven-plugin:" + JACOCO_VERSION + ":prepare-agent",
                    "test",
                    "org.jacoco:jacoco-maven-plugin:" + JACOCO_VERSION + ":report"
            ), projectRoot, "Coverage command failed with exit ");
            return;
        }

        runWithCustomTestCommand(projectRoot, testCommand);
    }

    private void runWithCustomTestCommand(Path projectRoot, String testCommand) throws Exception {
        Path agentJar = resolveJacocoAgentJar(projectRoot);
        Path destFile = projectRoot.resolve("target/jacoco.exec").toAbsolutePath();
        Map<String, String> env = Map.of("JAVA_TOOL_OPTIONS",
                "-javaagent:" + agentJar + "=destfile=" + destFile + ",append=true");

        int exit = executor.runShell(testCommand, projectRoot, env);
        if (exit != 0) {
            throw new IllegalStateException("Coverage command failed with exit " + exit);
        }

        run(List.of("mvn", "-q", "org.jacoco:jacoco-maven-plugin:" + JACOCO_VERSION + ":report"),
                projectRoot, "Coverage report command failed with exit ");
    }

    private Path resolveJacocoAgentJar(Path projectRoot) throws Exception {
        Path agentJar = projectRoot.resolve("target/jacoco-agent/org.jacoco.agent-runtime.jar");
        if (Files.exists(agentJar)) {
            return agentJar.toAbsolutePath();
        }
        run(List.of("mvn", "-q", "dependency:copy",
                "-Dartifact=org.jacoco:org.jacoco.agent:" + JACOCO_VERSION + ":jar:runtime",
                "-DoutputDirectory=target/jacoco-agent",
                "-Dmdep.stripVersion=true"
        ), projectRoot, "Unable to resolve the JaCoCo agent jar, exit ");
        if (!Files.exists(agentJar)) {
            throw new IllegalStateException("JaCoCo agent jar not found after resolution: " + agentJar);
        }
        return agentJar.toAbsolutePath();
    }

    private void run(List<String> command, Path directory, String failureMessage) throws Exception {
        int exit = executor.run(command, directory);
        if (exit != 0) {
            throw new IllegalStateException(failureMessage + exit);
        }
    }

    private void deleteIfExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        if (Files.isDirectory(path)) {
            try (var walk = Files.walk(path)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException ex) {
                                throw new IllegalStateException("Failed deleting stale coverage: " + p, ex);
                            }
                        });
            }
            return;
        }
        Files.deleteIfExists(path);
    }
}

/* mutate4java-manifest
version=1
moduleHash=080f89894e26acfa200b3c2dca10a0349ee9996687565dbfc43572b1d90a8eb1
scope.0.id=Y2xhc3M6Q292ZXJhZ2VSdW5uZXIjQ292ZXJhZ2VSdW5uZXI6OQ
scope.0.kind=class
scope.0.startLine=9
scope.0.endLine=51
scope.0.semanticHash=222115fc5fd871d8ad974cf4c94cd0e52e88ee06fe1e89a8e2c6416610e667ec
scope.1.id=ZmllbGQ6Q292ZXJhZ2VSdW5uZXIjZXhlY3V0b3I6MTE
scope.1.kind=field
scope.1.startLine=11
scope.1.endLine=11
scope.1.semanticHash=c4eedf9e7c0e6dffb225b9db0a0ca739c26698121741ae20349559e2bc48602a
scope.2.id=bWV0aG9kOkNvdmVyYWdlUnVubmVyI2N0b3IoMSk6MTM
scope.2.kind=method
scope.2.startLine=13
scope.2.endLine=15
scope.2.semanticHash=24bd1d89a71e4c966e1f894918fbada2a751dbd894339d3832764a5b9ddf2608
scope.3.id=bWV0aG9kOkNvdmVyYWdlUnVubmVyI2RlbGV0ZUlmRXhpc3RzKDEpOjMy
scope.3.kind=method
scope.3.startLine=32
scope.3.endLine=50
scope.3.semanticHash=3e9c1df528853970581858951a8747028bd49974850a38b37d4b10b3527140c5
scope.4.id=bWV0aG9kOkNvdmVyYWdlUnVubmVyI2dlbmVyYXRlQ292ZXJhZ2UoMSk6MTc
scope.4.kind=method
scope.4.startLine=17
scope.4.endLine=30
scope.4.semanticHash=2370d9f69b2da165bd3ec40585384649f6d75eb3193c5a09baff7d25762fa826
*/
