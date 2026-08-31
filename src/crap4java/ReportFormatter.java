package crap4java;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

final class ReportFormatter {

    private ReportFormatter() {
    }

    static String format(List<MethodMetrics> entries) {
        List<MethodMetrics> sorted = new ArrayList<>(entries);
        sorted.sort(Comparator
                .comparing((MethodMetrics e) -> e.crapScore() == null)
                .thenComparing(e -> e.crapScore() == null ? 0.0 : -e.crapScore()));

        String header = String.format(Locale.ROOT, "%-30s %-35s %4s %7s %8s", "Method", "Class", "CC", "Cov%", "CRAP");
        String separator = "-".repeat(header.length());
        StringBuilder builder = new StringBuilder();
        builder.append("CRAP Report\n");
        builder.append("===========\n");
        builder.append(header).append('\n');
        builder.append(separator).append('\n');

        for (MethodMetrics entry : sorted) {
            builder.append(String.format(Locale.ROOT, "%-30s %-35s %4d %7s %8s%n",
                    entry.methodName(),
                    entry.className(),
                    entry.complexity(),
                    formatCoverage(entry.coveragePercent()),
                    formatCrap(entry.crapScore())));
        }

        return builder.toString();
    }

    private static String formatCoverage(Double coverage) {
        if (coverage == null) {
            return "  N/A ";
        }
        return String.format(Locale.ROOT, "%5.1f%%", coverage);
    }

    private static String formatCrap(Double score) {
        if (score == null) {
            return "     N/A";
        }
        return String.format(Locale.ROOT, "%8.1f", score);
    }
}

/* mutate4java-manifest
version=1
moduleHash=234cae2941192c749c70d48c3471a374171a410c6b742f86d5d4421740a57d87
scope.0.id=Y2xhc3M6UmVwb3J0Rm9ybWF0dGVyI1JlcG9ydEZvcm1hdHRlcjo3
scope.0.kind=class
scope.0.startLine=7
scope.0.endLine=51
scope.0.semanticHash=8c1c8f1d2f2db26e60e42029c46d35b4a57c10aad221fb0a0356ff61cd3ac220
scope.1.id=bWV0aG9kOlJlcG9ydEZvcm1hdHRlciNjdG9yKDApOjk
scope.1.kind=method
scope.1.startLine=9
scope.1.endLine=10
scope.1.semanticHash=c56635cb154ff589ba6ac24da4e4c4a2db58eca3647b903e9b2ab77eba09d75a
scope.2.id=bWV0aG9kOlJlcG9ydEZvcm1hdHRlciNmb3JtYXQoMSk6MTI
scope.2.kind=method
scope.2.startLine=12
scope.2.endLine=36
scope.2.semanticHash=e3a5b649082a7c17a62ad89f11eb9a55b8c5036a76da5dcd9826e87a8cd29f3c
scope.3.id=bWV0aG9kOlJlcG9ydEZvcm1hdHRlciNmb3JtYXRDb3ZlcmFnZSgxKTozOA
scope.3.kind=method
scope.3.startLine=38
scope.3.endLine=43
scope.3.semanticHash=652fd86d3d77e080ecc589ecc894f712d33d3d4da5305491a168c4a85ff416b1
scope.4.id=bWV0aG9kOlJlcG9ydEZvcm1hdHRlciNmb3JtYXRDcmFwKDEpOjQ1
scope.4.kind=method
scope.4.startLine=45
scope.4.endLine=50
scope.4.semanticHash=813ed619e148f5a4b581e58e800b15e24f5f3b15f06804f22cb7df9fe7d32309
*/
