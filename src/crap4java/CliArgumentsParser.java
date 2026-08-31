package crap4java;

import java.util.ArrayList;
import java.util.List;

final class CliArgumentsParser {

    private CliArgumentsParser() {
    }

    static CliArguments parse(String[] rawArgs) {
        if (rawArgs.length == 0) {
            return new CliArguments(CliMode.ALL_SRC, List.of(), null);
        }

        if (containsFlag(rawArgs, "--help")) {
            return new CliArguments(CliMode.HELP, List.of(), null);
        }

        String testCommand = extractTestCommand(rawArgs);
        String[] args = withoutTestCommand(rawArgs);

        if (args.length == 0) {
            return new CliArguments(CliMode.ALL_SRC, List.of(), testCommand);
        }

        boolean changed = containsFlag(args, "--changed");
        List<String> values = nonFlagArgs(args);
        ensureChangedIsNotCombined(changed, values);
        if (changed) {
            return new CliArguments(CliMode.CHANGED_SRC, List.of(), testCommand);
        }
        return new CliArguments(CliMode.EXPLICIT_FILES, List.copyOf(values), testCommand);
    }

    private static String extractTestCommand(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("--test-command".equals(args[i])) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("--test-command requires a value");
                }
                return args[i + 1];
            }
        }
        return null;
    }

    private static String[] withoutTestCommand(String[] args) {
        List<String> remaining = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if ("--test-command".equals(args[i])) {
                i++;
                continue;
            }
            remaining.add(args[i]);
        }
        return remaining.toArray(new String[0]);
    }

    private static boolean containsFlag(String[] args, String flag) {
        for (String arg : args) {
            if (flag.equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> nonFlagArgs(String[] args) {
        List<String> values = new ArrayList<>();
        for (String arg : args) {
            if (arg.startsWith("--")) {
                continue;
            }
            values.add(arg);
        }
        return values;
    }

    private static void ensureChangedIsNotCombined(boolean changed, List<String> values) {
        if (changed && !values.isEmpty()) {
            throw new IllegalArgumentException("--changed cannot be combined with file arguments");
        }
    }
}

/* mutate4java-manifest
version=1
moduleHash=c601a3269ac203989827cc53ee454fc10939fa53e57696dd51933588e9ffdd3c
scope.0.id=Y2xhc3M6Q2xpQXJndW1lbnRzUGFyc2VyI0NsaUFyZ3VtZW50c1BhcnNlcjo2
scope.0.kind=class
scope.0.startLine=6
scope.0.endLine=54
scope.0.semanticHash=e1a985a18c271d3f33ed005b3954e9933a6b6a51a06728ba9f8015b2ed148726
scope.1.id=bWV0aG9kOkNsaUFyZ3VtZW50c1BhcnNlciNjb250YWluc0ZsYWcoMik6Mjk
scope.1.kind=method
scope.1.startLine=29
scope.1.endLine=36
scope.1.semanticHash=e222c83f83779c94d458d47525f0e6d676f4c6be6b74c198d8b91c8bf3b95544
scope.2.id=bWV0aG9kOkNsaUFyZ3VtZW50c1BhcnNlciNjdG9yKDApOjg
scope.2.kind=method
scope.2.startLine=8
scope.2.endLine=9
scope.2.semanticHash=c9502e5b2d38c24ae05d67ebe8ddde01d9ecd2bf449a91ac80aa7ba16421ee7d
scope.3.id=bWV0aG9kOkNsaUFyZ3VtZW50c1BhcnNlciNlbnN1cmVDaGFuZ2VkSXNOb3RDb21iaW5lZCgyKTo0OQ
scope.3.kind=method
scope.3.startLine=49
scope.3.endLine=53
scope.3.semanticHash=5cb2beb93b0a2fcc0eea65e584c43ac03abac646a4a1eb153dd697193f33f5c4
scope.4.id=bWV0aG9kOkNsaUFyZ3VtZW50c1BhcnNlciNub25GbGFnQXJncygxKTozOA
scope.4.kind=method
scope.4.startLine=38
scope.4.endLine=47
scope.4.semanticHash=c552c2037213247b8fc707727d39c9091f857462f0e836d19bd293eca2ce05da
scope.5.id=bWV0aG9kOkNsaUFyZ3VtZW50c1BhcnNlciNwYXJzZSgxKToxMQ
scope.5.kind=method
scope.5.startLine=11
scope.5.endLine=27
scope.5.semanticHash=77f58a4ff446f7980fab2ce25b35a52a9dc246e814ae59e19e19a883f3d79d50
*/
