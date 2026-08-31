package crap4java;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class CliApplication {

    private final Path projectRoot;
    private final PrintStream out;
    private final PrintStream err;
    private final CoverageRunner coverageRunner;

    CliApplication(Path projectRoot, PrintStream out, PrintStream err, CoverageRunner coverageRunner) {
        this.projectRoot = projectRoot;
        this.out = out;
        this.err = err;
        this.coverageRunner = coverageRunner;
    }

    int execute(String[] args) throws Exception {
        ParseOutcome parse = parseArguments(args);
        if (parse.exitCode >= 0) {
            return parse.exitCode;
        }
        CliArguments parsed = parse.arguments;
        List<Path> filesToAnalyze = filesForMode(parsed);
        if (filesToAnalyze.isEmpty()) {
            out.println("No Java files to analyze.");
            return 0;
        }

        List<MethodMetrics> metrics = analyzeByModule(filesToAnalyze, parsed.testCommand());
        metrics.sort(Comparator.comparing(MethodMetrics::crapScore,
                Comparator.nullsLast(Comparator.reverseOrder())));
        out.print(ReportFormatter.format(metrics));

        double max = Main.maxCrap(metrics);
        if (thresholdExceeded(max)) {
            err.printf(Locale.ROOT, "CRAP threshold exceeded: %.1f > 8.0%n", max);
            return 2;
        }
        return 0;
    }

    private List<MethodMetrics> analyzeByModule(List<Path> filesToAnalyze, String testCommand) throws Exception {
        List<MethodMetrics> metrics = new ArrayList<>();
        for (Map.Entry<Path, List<Path>> entry : groupByModuleRoot(filesToAnalyze).entrySet()) {
            Path moduleRoot = entry.getKey();
            Path jacocoXml = moduleRoot.resolve("target/site/jacoco/jacoco.xml");
            coverageRunner.generateCoverage(moduleRoot, testCommand);
            if (!Files.exists(jacocoXml)) {
                err.println("Warning: JaCoCo XML not found at " + jacocoXml + ". Coverage will be N/A.");
            }
            metrics.addAll(CrapAnalyzer.analyze(moduleRoot, entry.getValue(), jacocoXml));
        }
        return metrics;
    }

    static boolean thresholdExceeded(double max) {
        return Double.compare(max, 8.0) > 0;
    }

    private ParseOutcome parseArguments(String[] args) {
        try {
            CliArguments parsed = CliArgumentsParser.parse(args);
            if (parsed.mode() == CliMode.HELP) {
                out.println(Main.usage());
                return ParseOutcome.exit(0);
            }
            return ParseOutcome.ok(parsed);
        } catch (IllegalArgumentException ex) {
            err.println(ex.getMessage());
            out.println(Main.usage());
            return ParseOutcome.exit(1);
        }
    }

    private List<Path> filesForMode(CliArguments parsed) throws Exception {
        return switch (parsed.mode()) {
            case ALL_SRC -> SourceFileFinder.findAllJavaFilesUnderSrc(projectRoot);
            case CHANGED_SRC -> ChangedFileDetector.changedJavaFilesUnderSrc(projectRoot);
            case EXPLICIT_FILES -> explicitFiles(parsed.fileArgs());
            case HELP -> List.of();
        };
    }

    private List<Path> explicitFiles(List<String> args) throws Exception {
        Set<Path> files = new LinkedHashSet<>();
        for (String arg : args) {
            Path path = projectRoot.resolve(arg).normalize();
            if (Files.isDirectory(path)) {
                files.addAll(SourceFileFinder.findAllJavaFilesUnderSrc(path));
            } else {
                files.add(path);
            }
        }
        List<Path> sorted = new ArrayList<>(files);
        sorted.sort(Comparator.naturalOrder());
        return sorted;
    }

    static Path moduleRootFor(Path workspaceRoot, Path file) {
        Path normalizedWorkspaceRoot = workspaceRoot.normalize();
        Path current = Files.isDirectory(file) ? file.normalize() : file.normalize().getParent();
        while (current != null && current.startsWith(normalizedWorkspaceRoot)) {
            if (Files.exists(current.resolve("pom.xml"))) {
                return current;
            }
            current = current.getParent();
        }
        return normalizedWorkspaceRoot;
    }

    private Map<Path, List<Path>> groupByModuleRoot(List<Path> filesToAnalyze) {
        Map<Path, List<Path>> grouped = new LinkedHashMap<>();
        for (Path file : filesToAnalyze) {
            Path moduleRoot = moduleRootFor(projectRoot, file);
            grouped.computeIfAbsent(moduleRoot, ignored -> new ArrayList<>()).add(file);
        }
        return grouped;
    }

    private static final class ParseOutcome {
        private final CliArguments arguments;
        private final int exitCode;

        private ParseOutcome(CliArguments arguments, int exitCode) {
            this.arguments = arguments;
            this.exitCode = exitCode;
        }

        private static ParseOutcome ok(CliArguments arguments) {
            return new ParseOutcome(arguments, -1);
        }

        private static ParseOutcome exit(int code) {
            return new ParseOutcome(null, code);
        }
    }
}

/* mutate4java-manifest
version=1
moduleHash=5e28beaff6bbfa47827aba63f26186be35ab0a7211b3d96c9cffa37b93f2f64f
scope.0.id=Y2xhc3M6Q2xpQXBwbGljYXRpb24jQ2xpQXBwbGljYXRpb246MTQ
scope.0.kind=class
scope.0.startLine=14
scope.0.endLine=148
scope.0.semanticHash=72e3edb9c173f66282605ffd23efb26af90afc903be0c61f28b0d1bed8fdde20
scope.1.id=Y2xhc3M6Q2xpQXBwbGljYXRpb24uUGFyc2VPdXRjb21lI1BhcnNlT3V0Y29tZToxMzE
scope.1.kind=class
scope.1.startLine=131
scope.1.endLine=147
scope.1.semanticHash=45ed2bfb66d7822af10a2384e04bae542509fb8fb1a46052b2bf49795e404530
scope.2.id=ZmllbGQ6Q2xpQXBwbGljYXRpb24jY292ZXJhZ2VSdW5uZXI6MTk
scope.2.kind=field
scope.2.startLine=19
scope.2.endLine=19
scope.2.semanticHash=d92a1ba1476f655cf1babf2fbbc9b36f71fd57e400859cf09e46a1253a04c184
scope.3.id=ZmllbGQ6Q2xpQXBwbGljYXRpb24jZXJyOjE4
scope.3.kind=field
scope.3.startLine=18
scope.3.endLine=18
scope.3.semanticHash=0f12a462a677e93faaa05787bc46db2cace278490b3a801f721c167aedea712a
scope.4.id=ZmllbGQ6Q2xpQXBwbGljYXRpb24jb3V0OjE3
scope.4.kind=field
scope.4.startLine=17
scope.4.endLine=17
scope.4.semanticHash=b98df4fbf291f7cd01ba32f6b30c169fc64c08011a73e48a271561a4fcdd0a52
scope.5.id=ZmllbGQ6Q2xpQXBwbGljYXRpb24jcHJvamVjdFJvb3Q6MTY
scope.5.kind=field
scope.5.startLine=16
scope.5.endLine=16
scope.5.semanticHash=967df8631e20dcf5fe7b1534d2b220568e0e1c2f48d3990f2701b87b204eaad0
scope.6.id=ZmllbGQ6Q2xpQXBwbGljYXRpb24uUGFyc2VPdXRjb21lI2FyZ3VtZW50czoxMzI
scope.6.kind=field
scope.6.startLine=132
scope.6.endLine=132
scope.6.semanticHash=661f16ad226990eadabf31eb84854855268c8a11339b14e3255dd5bba3147187
scope.7.id=ZmllbGQ6Q2xpQXBwbGljYXRpb24uUGFyc2VPdXRjb21lI2V4aXRDb2RlOjEzMw
scope.7.kind=field
scope.7.startLine=133
scope.7.endLine=133
scope.7.semanticHash=9b365df939989346da53099623aa9608e776c5c7fa71f7e9a96132e7df1bbedf
scope.8.id=bWV0aG9kOkNsaUFwcGxpY2F0aW9uI2FuYWx5emVCeU1vZHVsZSgxKTo1Mw
scope.8.kind=method
scope.8.startLine=53
scope.8.endLine=65
scope.8.semanticHash=9e87960a5cb9616913a7fbe79fbb8ae27e79b3ca100d365c1fba9c9e51d549bc
scope.9.id=bWV0aG9kOkNsaUFwcGxpY2F0aW9uI2N0b3IoNCk6MjE
scope.9.kind=method
scope.9.startLine=21
scope.9.endLine=26
scope.9.semanticHash=f0fc877ed56783fd79a8d2c8590b731e35c0b16a80be0be16d30494d186d0b05
scope.10.id=bWV0aG9kOkNsaUFwcGxpY2F0aW9uI2V4ZWN1dGUoMSk6Mjg
scope.10.kind=method
scope.10.startLine=28
scope.10.endLine=51
scope.10.semanticHash=e834a9410d6c93bdcfb4ccd07064562674c2afb34c8fb73d853070615253cd50
scope.11.id=bWV0aG9kOkNsaUFwcGxpY2F0aW9uI2V4cGxpY2l0RmlsZXMoMSk6OTU
scope.11.kind=method
scope.11.startLine=95
scope.11.endLine=108
scope.11.semanticHash=e371db323f2ca72e6f886e53697121802f1d5f29e878d1fcec396241d98c0cd9
scope.12.id=bWV0aG9kOkNsaUFwcGxpY2F0aW9uI2ZpbGVzRm9yTW9kZSgxKTo4Ng
scope.12.kind=method
scope.12.startLine=86
scope.12.endLine=93
scope.12.semanticHash=dcf1caca27fab7b7478c3f57fd0a2ab6036931b44af0a8b39691615b3f76d831
scope.13.id=bWV0aG9kOkNsaUFwcGxpY2F0aW9uI2dyb3VwQnlNb2R1bGVSb290KDEpOjEyMg
scope.13.kind=method
scope.13.startLine=122
scope.13.endLine=129
scope.13.semanticHash=b162fe08460eea66f1c87678860bc8be1e90d225b5ae07016a4f0aa3103b5b20
scope.14.id=bWV0aG9kOkNsaUFwcGxpY2F0aW9uI21vZHVsZVJvb3RGb3IoMik6MTEw
scope.14.kind=method
scope.14.startLine=110
scope.14.endLine=120
scope.14.semanticHash=a180b2afd49b317ec0ef05c2dbfaf083e513097e268f36ca534a50a27a886e8c
scope.15.id=bWV0aG9kOkNsaUFwcGxpY2F0aW9uI3BhcnNlQXJndW1lbnRzKDEpOjcx
scope.15.kind=method
scope.15.startLine=71
scope.15.endLine=84
scope.15.semanticHash=899bc3c351cfb3d2ad575d8fe4ba812948e282104bb60fb1412ba2c3861305f2
scope.16.id=bWV0aG9kOkNsaUFwcGxpY2F0aW9uI3RocmVzaG9sZEV4Y2VlZGVkKDEpOjY3
scope.16.kind=method
scope.16.startLine=67
scope.16.endLine=69
scope.16.semanticHash=00b761a57b4a4a66733929fb784fbe41d9760841f7ed75fb61ce54edf4f20ed7
scope.17.id=bWV0aG9kOkNsaUFwcGxpY2F0aW9uLlBhcnNlT3V0Y29tZSNjdG9yKDIpOjEzNQ
scope.17.kind=method
scope.17.startLine=135
scope.17.endLine=138
scope.17.semanticHash=bac767bb702fe8b4a04ddc028c794d85ecb0968a4357fe90c0c152d76966a315
scope.18.id=bWV0aG9kOkNsaUFwcGxpY2F0aW9uLlBhcnNlT3V0Y29tZSNleGl0KDEpOjE0NA
scope.18.kind=method
scope.18.startLine=144
scope.18.endLine=146
scope.18.semanticHash=10b36de9f002c3f16736f379c7754defec224dccb1cb16006d03a5e72aadcfab
scope.19.id=bWV0aG9kOkNsaUFwcGxpY2F0aW9uLlBhcnNlT3V0Y29tZSNvaygxKToxNDA
scope.19.kind=method
scope.19.startLine=140
scope.19.endLine=142
scope.19.semanticHash=a557a67f31a114feacb3bd934fb922508d7e77088f8afc84a424948584fa0c4f
*/
