package crap4java;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

interface CommandExecutor {
    int run(List<String> command, Path directory) throws Exception;

    default int runShell(String commandText, Path directory) throws Exception {
        return runShell(commandText, directory, Map.of());
    }

    default int runShell(String commandText, Path directory, Map<String, String> extraEnv) throws Exception {
        return run(List.of("/bin/sh", "-lc", commandText), directory);
    }
}

/* mutate4java-manifest
version=1
moduleHash=7c36597a09c1f3185368298c7e39f28e1b1d2d36b255bdea5b92cf7f234f7c4b
scope.0.id=Y2xhc3M6Q29tbWFuZEV4ZWN1dG9yI0NvbW1hbmRFeGVjdXRvcjo2
scope.0.kind=class
scope.0.startLine=6
scope.0.endLine=8
scope.0.semanticHash=35250ada8d3a2b89a52b4e23b4d3a9fabb1673059a4f17d1a501036158ac87ca
scope.1.id=bWV0aG9kOkNvbW1hbmRFeGVjdXRvciNydW4oMik6Nw
scope.1.kind=method
scope.1.startLine=7
scope.1.endLine=7
scope.1.semanticHash=68f8449c642b534bef8b87213aee84fd1cd5fd880d70f000b3f8e80ae1c0fffc
*/
