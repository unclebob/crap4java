package crap4java;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        System.exit(run(args, Path.of(".").toAbsolutePath().normalize(), System.out, System.err));
    }

    static int run(String[] args, Path projectRoot, PrintStream out, PrintStream err) throws Exception {
        return run(args, projectRoot, out, err, new CoverageRunner(new ProcessCommandExecutor()));
    }

    static int run(String[] args,
                   Path projectRoot,
                   PrintStream out,
                   PrintStream err,
                   CoverageRunner coverageRunner) throws Exception {
        return new CliApplication(projectRoot, out, err, coverageRunner).execute(args);
    }

    static String usage() {
        return """
                Usage:
                  crap4java            Analyze all Java files under src/
                  crap4java --changed  Analyze changed Java files under src/
                  crap4java <path...>  Analyze files, or for directory args analyze <dir>/src/**/*.java
                  crap4java --help     Print this help message

                Options:
                  --test-command CMD   Run CMD instead of the default `mvn test` coverage
                                        command, for projects that don't run tests through
                                        Maven. CMD runs with the JaCoCo agent attached via
                                        JAVA_TOOL_OPTIONS, so any JVM it launches (including
                                        a custom test runner) still contributes coverage;
                                        CMD's exit code determines pass/fail.
                """;
    }

    static double maxCrap(List<MethodMetrics> metrics) {
        double max = 0.0;
        for (MethodMetrics metric : metrics) {
            if (metric.crapScore() != null) {
                max = Math.max(max, metric.crapScore());
            }
        }
        return max;
    }
}

/* mutate4java-manifest
version=1
moduleHash=e33f577606041952eb9ddde846aa921b7b03fa24e487e2a834540f64b992914c
scope.0.id=Y2xhc3M6TWFpbiNNYWluOjc
scope.0.kind=class
scope.0.startLine=7
scope.0.endLine=47
scope.0.semanticHash=4958ec2d89a67c8cf8abbdba7cf1f27c2ed0a96f892b959933533c0dae70c14c
scope.1.id=bWV0aG9kOk1haW4jY3RvcigwKTo5
scope.1.kind=method
scope.1.startLine=9
scope.1.endLine=10
scope.1.semanticHash=2a7894b82bf05c8917e82420e502ab2cc96fef2366eeef066911514202ec3bd1
scope.2.id=bWV0aG9kOk1haW4jbWFpbigxKToxMg
scope.2.kind=method
scope.2.startLine=12
scope.2.endLine=14
scope.2.semanticHash=ad0b8e92af29f222e8dd39931244c11c92fec3df537a67a48b20b5f66ff38418
scope.3.id=bWV0aG9kOk1haW4jbWF4Q3JhcCgxKTozOA
scope.3.kind=method
scope.3.startLine=38
scope.3.endLine=46
scope.3.semanticHash=9dc86a46e1bbd3c811f8c5fac1df658311b061ea8bb0d5d0eb6988e64d2b48a1
scope.4.id=bWV0aG9kOk1haW4jcnVuKDQpOjE2
scope.4.kind=method
scope.4.startLine=16
scope.4.endLine=18
scope.4.semanticHash=cc7423c4434171101ff7f748e37f11409b415f102f126ddf5a1f9df77c2b278f
scope.5.id=bWV0aG9kOk1haW4jcnVuKDUpOjIw
scope.5.kind=method
scope.5.startLine=20
scope.5.endLine=26
scope.5.semanticHash=026c69ed083265062059d1580ebc97d39024947bd7dc82141053916115a169ef
scope.6.id=bWV0aG9kOk1haW4jdXNhZ2UoMCk6Mjg
scope.6.kind=method
scope.6.startLine=28
scope.6.endLine=36
scope.6.semanticHash=e96b9f53b2599d50ee02f75be6e027bc903b4bd00a18a0ee334a1f9a36b56e0f
*/
