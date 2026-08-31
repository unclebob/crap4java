package crap4java;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

final class ProcessCommandExecutor implements CommandExecutor {

    @Override
    public int run(List<String> command, Path directory) throws Exception {
        return start(command, directory, Map.of()).waitFor();
    }

    @Override
    public int runShell(String commandText, Path directory, Map<String, String> extraEnv) throws Exception {
        return start(List.of("/bin/sh", "-lc", commandText), directory, extraEnv).waitFor();
    }

    private Process start(List<String> command, Path directory, Map<String, String> extraEnv) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(command)
                .directory(directory.toFile())
                .inheritIO();
        builder.environment().putAll(extraEnv);
        return builder.start();
    }
}

/* mutate4java-manifest
version=1
moduleHash=6ea27d2229d3c3ec428dc6c5fff6f319242e088f19dd9369a4f1992ce2a80266
scope.0.id=Y2xhc3M6UHJvY2Vzc0NvbW1hbmRFeGVjdXRvciNQcm9jZXNzQ29tbWFuZEV4ZWN1dG9yOjY
scope.0.kind=class
scope.0.startLine=6
scope.0.endLine=16
scope.0.semanticHash=d49ac5bf424aba3412a65e6fa49929921a017705f4c243d18d5828d46ca74e6e
scope.1.id=bWV0aG9kOlByb2Nlc3NDb21tYW5kRXhlY3V0b3IjY3RvcigwKTo2
scope.1.kind=method
scope.1.startLine=1
scope.1.endLine=16
scope.1.semanticHash=d1a3877e6063504f6423a8dc876b39079a1832a2c7bc3f3145646bd3e99dd29c
scope.2.id=bWV0aG9kOlByb2Nlc3NDb21tYW5kRXhlY3V0b3IjcnVuKDIpOjg
scope.2.kind=method
scope.2.startLine=8
scope.2.endLine=15
scope.2.semanticHash=7be95a2db36c31ff28eb73e5aad6fcc4dbdd9b03cc3ece380c4a6aa13599060c
*/
