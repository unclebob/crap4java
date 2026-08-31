package crap4java;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CliArgumentsParserTest {

    @Test
    void noArgsMeansAllSrcFiles() {
        CliArguments args = CliArgumentsParser.parse(new String[]{});
        assertEquals(CliMode.ALL_SRC, args.mode());
    }

    @Test
    void changedFlagMeansChangedSrcFiles() {
        CliArguments args = CliArgumentsParser.parse(new String[]{"--changed"});
        assertEquals(CliMode.CHANGED_SRC, args.mode());
    }

    @Test
    void fileNamesMeanExplicitFiles() {
        CliArguments args = CliArgumentsParser.parse(new String[]{"src/main/java/demo/A.java", "src/main/java/demo/B.java"});
        assertEquals(CliMode.EXPLICIT_FILES, args.mode());
        assertEquals(List.of("src/main/java/demo/A.java", "src/main/java/demo/B.java"), args.fileArgs());
    }

    @Test
    void unknownFlagsAreIgnoredWhenCollectingExplicitFiles() {
        CliArguments args = CliArgumentsParser.parse(new String[]{"src/main/java/demo/A.java", "--bogus", "src/main/java/demo/B.java"});

        assertEquals(CliMode.EXPLICIT_FILES, args.mode());
        assertEquals(List.of("src/main/java/demo/A.java", "src/main/java/demo/B.java"), args.fileArgs());
    }

    @Test
    void helpPrintsUsageMode() {
        CliArguments args = CliArgumentsParser.parse(new String[]{"--help"});
        assertEquals(CliMode.HELP, args.mode());
    }

    @Test
    void changedCannotBeCombinedWithFiles() {
        assertThrows(IllegalArgumentException.class,
                () -> CliArgumentsParser.parse(new String[]{"--changed", "src/main/java/demo/A.java"}));
    }

    @Test
    void plainFilesDoNotTriggerChangedMode() {
        CliArguments args = CliArgumentsParser.parse(new String[]{"src/main/java/demo/A.java"});

        assertEquals(CliMode.EXPLICIT_FILES, args.mode());
        assertEquals(List.of("src/main/java/demo/A.java"), args.fileArgs());
    }

    @Test
    void noTestCommandByDefault() {
        CliArguments args = CliArgumentsParser.parse(new String[]{});
        assertEquals(null, args.testCommand());
    }

    @Test
    void testCommandFlagCapturesItsValue() {
        CliArguments args = CliArgumentsParser.parse(
                new String[]{"--test-command", "scripts/run-unit-tests.sh"});

        assertEquals("scripts/run-unit-tests.sh", args.testCommand());
        assertEquals(CliMode.ALL_SRC, args.mode());
    }

    @Test
    void testCommandCanCombineWithChangedAndExplicitFiles() {
        CliArguments changed = CliArgumentsParser.parse(
                new String[]{"--changed", "--test-command", "scripts/run-unit-tests.sh"});
        assertEquals(CliMode.CHANGED_SRC, changed.mode());
        assertEquals("scripts/run-unit-tests.sh", changed.testCommand());

        CliArguments explicit = CliArgumentsParser.parse(
                new String[]{"src/main/java/demo/A.java", "--test-command", "scripts/run-unit-tests.sh"});
        assertEquals(CliMode.EXPLICIT_FILES, explicit.mode());
        assertEquals(List.of("src/main/java/demo/A.java"), explicit.fileArgs());
        assertEquals("scripts/run-unit-tests.sh", explicit.testCommand());
    }

    @Test
    void testCommandRequiresAValue() {
        assertThrows(IllegalArgumentException.class,
                () -> CliArgumentsParser.parse(new String[]{"--test-command"}));
    }
}
