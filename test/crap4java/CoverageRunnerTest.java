package crap4java;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoverageRunnerTest {

    @TempDir
    Path tempDir;

    @Test
    void deletesStaleCoverageAndRunsMavenCoverageCommand() throws Exception {
        Path jacocoDir = tempDir.resolve("target/site/jacoco");
        Files.createDirectories(jacocoDir);
        Files.writeString(jacocoDir.resolve("old.xml"), "stale");
        Path exec = tempDir.resolve("target/jacoco.exec");
        Files.createDirectories(exec.getParent());
        Files.writeString(exec, "stale");

        RecordingExecutor executor = new RecordingExecutor(0);
        CoverageRunner runner = new CoverageRunner(executor);

        runner.generateCoverage(tempDir, null);

        assertFalse(Files.exists(jacocoDir));
        assertFalse(Files.exists(exec));
        assertEquals(List.of(
                "mvn", "-q",
                "org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent",
                "test",
                "org.jacoco:jacoco-maven-plugin:0.8.12:report"
        ), executor.commands.get(0));
        assertEquals(tempDir, executor.directories.get(0));
    }

    @Test
    void failsWhenCoverageCommandFails() {
        RecordingExecutor executor = new RecordingExecutor(2);
        CoverageRunner runner = new CoverageRunner(executor);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> runner.generateCoverage(tempDir, null));

        assertEquals("Coverage command failed with exit 2", ex.getMessage());
    }

    @Test
    void testCommandRunsUnderTheJacocoAgentAndGeneratesAReport() throws Exception {
        RecordingExecutor executor = new RecordingExecutor(0);
        CoverageRunner runner = new CoverageRunner(executor);

        runner.generateCoverage(tempDir, "scripts/run-unit-tests.sh");

        assertEquals("scripts/run-unit-tests.sh", executor.shellCommand);
        assertEquals(tempDir, executor.shellDirectory);
        String javaToolOptions = executor.shellEnv.get("JAVA_TOOL_OPTIONS");
        assertTrue(javaToolOptions.startsWith("-javaagent:"), javaToolOptions);
        assertTrue(javaToolOptions.contains("org.jacoco.agent-runtime.jar"), javaToolOptions);
        assertTrue(javaToolOptions.endsWith("=destfile=" + tempDir.resolve("target/jacoco.exec") + ",append=true"),
                javaToolOptions);

        assertEquals(List.of("mvn", "-q", "dependency:copy",
                "-Dartifact=org.jacoco:org.jacoco.agent:0.8.12:jar:runtime",
                "-DoutputDirectory=target/jacoco-agent",
                "-Dmdep.stripVersion=true"), executor.commands.get(0));
        assertEquals(List.of("mvn", "-q", "org.jacoco:jacoco-maven-plugin:0.8.12:report"), executor.commands.get(1));
    }

    @Test
    void reusesAnAlreadyResolvedJacocoAgentJar() throws Exception {
        Path agentJar = tempDir.resolve("target/jacoco-agent/org.jacoco.agent-runtime.jar");
        Files.createDirectories(agentJar.getParent());
        Files.writeString(agentJar, "already-resolved");

        RecordingExecutor executor = new RecordingExecutor(0);
        CoverageRunner runner = new CoverageRunner(executor);

        runner.generateCoverage(tempDir, "scripts/run-unit-tests.sh");

        assertEquals(List.of("mvn", "-q", "org.jacoco:jacoco-maven-plugin:0.8.12:report"), executor.commands.get(0));
    }

    @Test
    void failsWhenTestCommandFails() {
        RecordingExecutor executor = new RecordingExecutor(1);
        CoverageRunner runner = new CoverageRunner(executor);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> runner.generateCoverage(tempDir, "scripts/run-unit-tests.sh"));

        assertEquals("Coverage command failed with exit 1", ex.getMessage());
        assertEquals(1, executor.commands.size(), "the report goal must not run after a failed test command");
    }

    private static final class RecordingExecutor implements CommandExecutor {
        private final int exitCode;
        private final List<List<String>> commands = new ArrayList<>();
        private final List<Path> directories = new ArrayList<>();
        private String shellCommand;
        private Path shellDirectory;
        private Map<String, String> shellEnv;

        private RecordingExecutor(int exitCode) {
            this.exitCode = exitCode;
        }

        @Override
        public int run(List<String> command, Path directory) throws java.io.IOException {
            commands.add(command);
            directories.add(directory);
            if (command.contains("dependency:copy")) {
                Path agentJar = directory.resolve("target/jacoco-agent/org.jacoco.agent-runtime.jar");
                Files.createDirectories(agentJar.getParent());
                Files.writeString(agentJar, "fake-agent-jar");
                return 0;
            }
            return exitCode;
        }

        @Override
        public int runShell(String commandText, Path directory, Map<String, String> extraEnv) {
            shellCommand = commandText;
            shellDirectory = directory;
            shellEnv = extraEnv;
            return exitCode;
        }
    }
}
