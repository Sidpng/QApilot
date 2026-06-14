// Project Title: QApilot — Test Stability Tracker
// File Name: TestStabilityTracker.java
// Author: Siddhartha Upadhyay
// GitHub: https://github.com/Sidpng
// Description: Self-contained, zero-dependency test-analytics tool.
//              Reads Surefire/TestNG JUnit XML, appends each run to an
//              append-only NDJSON history, computes per-test flakiness
//              (run-over-run pass/fail toggling), and renders a static
//              HTML dashboard. No build step, no database, no UI driving.
//
// Run (JDK 17+ single-file source-code mode):
//   java tools/test-stability-tracker/TestStabilityTracker.java \
//        target/surefire-reports \
//        tools/test-stability-tracker/history/runs.ndjson \
//        test-reports/dashboard/index.html \
//        --browser chromium --window 30
//
// Re-render only (skip ingestion) by pointing arg[0] at a missing/empty dir.

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TestStabilityTracker {

    // ---------------------------------------------------------------- model

    enum Status { PASS, FAIL, ERROR, SKIP }

    /** One test outcome from one run. */
    record Result(String run, String commit, String browser, String test, Status status, long durationMs) {}

    /** Aggregated stability metrics for one test over the analysis window. */
    static final class Metrics {
        String test;
        int total, pass, fail, skip;     // fail folds ERROR in
        int flips;                       // adjacent pass<->fail transitions (skips ignored)
        boolean flaky;                   // both a pass and a fail inside the window
        double flipRate;                 // flips / max(1, comparable-1)
        double failRate;                 // fail / total
        Status last;                     // most recent outcome
        List<Status> spark = new ArrayList<>(); // chronological window
    }

    // ----------------------------------------------------------------- main

    public static void main(String[] args) throws Exception {
        Map<String, String> opt = new LinkedHashMap<>();
        List<String> pos = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a.startsWith("--")) {
                String key = a.substring(2);
                String val = (i + 1 < args.length && !args[i + 1].startsWith("--")) ? args[++i] : "true";
                opt.put(key, val);
            } else {
                pos.add(a);
            }
        }
        if (pos.size() < 3) {
            System.err.println("Usage: java TestStabilityTracker.java <surefire-dir> <history.ndjson> <out.html> "
                    + "[--browser NAME] [--commit SHA] [--run-id ID] [--window N]");
            System.exit(2);
        }

        Path surefireDir = Paths.get(pos.get(0));
        Path historyFile = Paths.get(pos.get(1));
        Path outHtml     = Paths.get(pos.get(2));

        String browser = firstNonBlank(opt.get("browser"), System.getenv("TST_BROWSER"), "unknown");
        String commit  = shortSha(firstNonBlank(opt.get("commit"), System.getenv("GITHUB_SHA"), "local"));
        int    window  = parseInt(opt.get("window"), 30);

        String runStamp = firstNonBlank(opt.get("run-id"),
                DateTimeFormatter.ISO_INSTANT.format(Instant.now().truncatedTo(ChronoUnit.SECONDS)));

        // 1) Ingest the latest run (if reports exist) and append to history.
        List<Result> fresh = ingest(surefireDir, runStamp, commit, browser);
        if (!fresh.isEmpty()) {
            appendHistory(historyFile, fresh);
            System.out.printf("Ingested %d test results from %s (run=%s, browser=%s)%n",
                    fresh.size(), surefireDir, runStamp, browser);
        } else {
            System.out.printf("No new reports under %s — re-rendering dashboard from history only.%n", surefireDir);
        }

        // 2) Load full history and compute stability metrics.
        List<Result> history = readHistory(historyFile);
        if (history.isEmpty()) {
            System.out.println("History is empty; writing placeholder dashboard.");
        }
        Map<String, Metrics> metrics = analyze(history, window);

        // 3) Render the static dashboard.
        long distinctRuns = history.stream().map(Result::run).distinct().count();
        String html = renderHtml(metrics, distinctRuns, window);
        if (outHtml.getParent() != null) Files.createDirectories(outHtml.getParent());
        Files.writeString(outHtml, html, StandardCharsets.UTF_8);
        System.out.println("Dashboard written to " + outHtml.toAbsolutePath());

        // 4) Console summary so CI logs surface the worst offenders.
        printTopFlaky(metrics);
    }

    // -------------------------------------------------------------- ingest

    /** Parse every Surefire/TestNG JUnit file (TEST-*.xml) in a directory. */
    static List<Result> ingest(Path dir, String run, String commit, String browser) throws Exception {
        List<Result> out = new ArrayList<>();
        if (dir == null || !Files.isDirectory(dir)) return out;

        List<Path> files = new ArrayList<>();
        try (var stream = Files.newDirectoryStream(dir, "TEST-*.xml")) {
            stream.forEach(files::add);
        }
        if (files.isEmpty()) return out;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        try { dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); }
        catch (Exception ignore) {}
        dbf.setExpandEntityReferences(false);

        for (Path f : files) {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc;
            try { doc = db.parse(f.toFile()); }
            catch (Exception e) {
                System.err.println("Skipping unparseable report " + f + ": " + e.getMessage());
                continue;
            }
            NodeList cases = doc.getElementsByTagName("testcase");
            for (int i = 0; i < cases.getLength(); i++) {
                Element tc = (Element) cases.item(i);
                String name = tc.getAttribute("name");
                String cls  = tc.getAttribute("classname");
                if (name.isEmpty()) continue;
                String test = (cls.isEmpty() ? "" : cls + ".") + name;

                long durationMs = 0L;
                try { durationMs = Math.round(Double.parseDouble(tc.getAttribute("time")) * 1000); }
                catch (NumberFormatException ignore) {}

                Status status = Status.PASS;
                if (hasChild(tc, "error"))   status = Status.ERROR;
                else if (hasChild(tc, "failure")) status = Status.FAIL;
                else if (hasChild(tc, "skipped")) status = Status.SKIP;

                out.add(new Result(run, commit, browser, test, status, durationMs));
            }
        }
        return out;
    }

    static boolean hasChild(Element parent, String tag) {
        NodeList kids = parent.getChildNodes();
        for (int i = 0; i < kids.getLength(); i++) {
            Node n = kids.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(tag)) return true;
        }
        return false;
    }

    // ------------------------------------------------------------- history

    static void appendHistory(Path file, List<Result> rows) throws IOException {
        if (file.getParent() != null) Files.createDirectories(file.getParent());
        StringBuilder sb = new StringBuilder();
        for (Result r : rows) {
            sb.append('{')
              .append("\"run\":").append(jq(r.run())).append(',')
              .append("\"commit\":").append(jq(r.commit())).append(',')
              .append("\"browser\":").append(jq(r.browser())).append(',')
              .append("\"test\":").append(jq(r.test())).append(',')
              .append("\"status\":").append(jq(r.status().name())).append(',')
              .append("\"durationMs\":").append(r.durationMs())
              .append("}\n");
        }
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
    }

    static List<Result> readHistory(Path file) throws IOException {
        List<Result> out = new ArrayList<>();
        if (!Files.exists(file)) return out;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.isBlank()) continue;
            Map<String, String> m = parseFlatJson(line);
            String test = m.get("test");
            if (test == null) continue;
            Status status;
            try { status = Status.valueOf(m.getOrDefault("status", "PASS")); }
            catch (IllegalArgumentException e) { status = Status.PASS; }
            long dur = parseLong(m.get("durationMs"), 0L);
            out.add(new Result(
                    m.getOrDefault("run", ""),
                    m.getOrDefault("commit", ""),
                    m.getOrDefault("browser", "unknown"),
                    test, status, dur));
        }
        return out;
    }

    // ------------------------------------------------------------- analyze

    static Map<String, Metrics> analyze(List<Result> history, int window) {
        Map<String, List<Result>> byTest = new TreeMap<>();
        for (Result r : history) byTest.computeIfAbsent(r.test(), k -> new ArrayList<>()).add(r);

        Map<String, Metrics> out = new LinkedHashMap<>();
        for (var e : byTest.entrySet()) {
            List<Result> rs = e.getValue();
            rs.sort(Comparator.comparing(Result::run).thenComparing(Result::browser));
            if (rs.size() > window) rs = rs.subList(rs.size() - window, rs.size());

            Metrics m = new Metrics();
            m.test  = e.getKey();
            m.total = rs.size();
            Boolean prevFail = null;
            boolean sawPass = false, sawFail = false;
            for (Result r : rs) {
                m.spark.add(r.status());
                switch (r.status()) {
                    case PASS        -> { m.pass++; sawPass = true; }
                    case FAIL, ERROR -> { m.fail++; sawFail = true; }
                    case SKIP        -> m.skip++;
                }
                if (r.status() != Status.SKIP) {
                    boolean isFail = (r.status() == Status.FAIL || r.status() == Status.ERROR);
                    if (prevFail != null && prevFail != isFail) m.flips++;
                    prevFail = isFail;
                }
                m.last = r.status();
            }
            int comparable = m.pass + m.fail;
            m.flipRate = comparable > 1 ? (double) m.flips / (comparable - 1) : 0.0;
            m.failRate = m.total > 0    ? (double) m.fail  / m.total           : 0.0;
            m.flaky    = sawPass && sawFail;
            out.put(m.test, m);
        }
        return out;
    }

    static List<Metrics> ranked(Map<String, Metrics> metrics) {
        List<Metrics> list = new ArrayList<>(metrics.values());
        list.sort(Comparator
                .comparing((Metrics m) -> m.flaky).reversed()
                .thenComparing(Comparator.comparingDouble((Metrics m) -> m.flipRate).reversed())
                .thenComparing(Comparator.comparingDouble((Metrics m) -> m.failRate).reversed())
                .thenComparing(m -> m.test));
        return list;
    }

    static void printTopFlaky(Map<String, Metrics> metrics) {
        List<Metrics> flaky = ranked(metrics).stream().filter(m -> m.flaky).toList();
        if (flaky.isEmpty()) {
            System.out.println("No unstable tests detected in the current window. All green.");
            return;
        }
        System.out.println("Most unstable tests (worst first):");
        int n = Math.min(10, flaky.size());
        for (int i = 0; i < n; i++) {
            Metrics m = flaky.get(i);
            System.out.printf("  %2d. %-60s flip=%3.0f%%  fail=%3.0f%%  (%d runs)%n",
                    i + 1, m.test, m.flipRate * 100, m.failRate * 100, m.total);
        }
    }

    // ---------------------------------------------------------------- html

    static String renderHtml(Map<String, Metrics> metrics, long distinctRuns, int window) {
        List<Metrics> rows = ranked(metrics);
        long unstableCount = rows.stream().filter(m -> m.flaky).count();
        long stableCount   = rows.stream().filter(m -> !m.flaky && m.fail == 0).count();
        String updated = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'")
                .withZone(ZoneOffset.UTC).format(Instant.now());

        StringBuilder tbody = new StringBuilder();
        if (rows.isEmpty()) {
            tbody.append("<tr><td colspan=\"8\" class=\"empty\">No runs recorded yet. "
                    + "The first CI run will populate this dashboard automatically.</td></tr>");
        }
        for (Metrics m : rows) {
            String[] parts = splitTest(m.test);
            String rowClass = m.flaky ? "unstable" : (m.fail > 0 ? "failing" : "");
            tbody.append("<tr class=\"").append(rowClass).append("\">")
                 .append("<td class=\"t\">")
                   .append("<span class=\"cls\">").append(esc(parts[0])).append("</span>")
                   .append("<span class=\"mth\">").append(esc(parts[1])).append("</span>")
                 .append("</td>")
                 .append("<td class=\"num\">").append(m.total).append("</td>")
                 .append("<td class=\"num ok\">").append(m.pass).append("</td>")
                 .append("<td class=\"num bad\">").append(m.fail).append("</td>")
                 .append("<td class=\"num mut\">").append(m.skip).append("</td>")
                 .append("<td class=\"spark\">").append(spark(m.spark)).append("</td>")
                 .append("<td class=\"num\">").append(pct(m.flipRate)).append("</td>")
                 .append("<td>").append(stabilityBadge(m)).append("</td>")
                 .append("</tr>");
        }

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                <meta charset="utf-8"/>
                <meta name="viewport" content="width=device-width, initial-scale=1"/>
                <title>QApilot — Test Stability Report</title>
                <style>
                  :root{
                    --bg:#0d1117; --panel:#161b22; --line:#30363d; --fg:#e6edf3;
                    --mut:#8b949e; --ok:#3fb950; --bad:#f85149; --warn:#d29922; --accent:#58a6ff;
                  }
                  *{box-sizing:border-box}
                  body{margin:0;background:var(--bg);color:var(--fg);
                       font-family:ui-monospace,SFMono-Regular,Menlo,Consolas,monospace;font-size:14px}
                  .wrap{max-width:1140px;margin:0 auto;padding:32px 20px 64px}
                  h1{font-size:22px;margin:0 0 4px}
                  .sub{color:var(--mut);margin:0 0 24px;font-size:13px}
                  .cards{display:grid;grid-template-columns:repeat(4,1fr);gap:12px;margin-bottom:24px}
                  .card{background:var(--panel);border:1px solid var(--line);border-radius:10px;padding:16px}
                  .card .k{color:var(--mut);font-size:11px;text-transform:uppercase;letter-spacing:.06em}
                  .card .v{font-size:28px;font-weight:700;margin-top:6px}
                  .card.warn .v{color:var(--warn)}
                  .card.ok .v{color:var(--ok)}
                  table{width:100%;border-collapse:collapse;background:var(--panel);
                        border:1px solid var(--line);border-radius:10px;overflow:hidden}
                  th,td{padding:10px 12px;text-align:left;border-bottom:1px solid var(--line)}
                  th{color:var(--mut);font-weight:600;font-size:11px;text-transform:uppercase;letter-spacing:.06em}
                  tr:last-child td{border-bottom:none}
                  tr.unstable{background:rgba(210,153,34,.07)}
                  tr.failing{background:rgba(248,81,73,.05)}
                  td.num{text-align:right;font-variant-numeric:tabular-nums}
                  td.ok{color:var(--ok)} td.bad{color:var(--bad)} td.mut{color:var(--mut)}
                  .t .cls{display:block;color:var(--mut);font-size:11px}
                  .t .mth{display:block;font-weight:600}
                  .spark{white-space:nowrap}
                  .cell{display:inline-block;width:10px;height:16px;border-radius:2px;margin-right:2px;vertical-align:middle}
                  .cell.PASS{background:var(--ok)}
                  .cell.FAIL,.cell.ERROR{background:var(--bad)}
                  .cell.SKIP{background:var(--line)}
                  .bdg{display:inline-block;padding:2px 10px;border-radius:999px;font-size:11px;font-weight:700;letter-spacing:.03em}
                  .bdg-unstable{background:rgba(210,153,34,.18);color:var(--warn)}
                  .bdg-stable{background:rgba(63,185,80,.15);color:var(--ok)}
                  .bdg-failing{background:rgba(248,81,73,.15);color:var(--bad)}
                  .empty{text-align:center;color:var(--mut);padding:48px}
                  footer{color:var(--mut);margin-top:20px;font-size:12px}
                  a{color:var(--accent)}
                  @media(max-width:700px){.cards{grid-template-columns:1fr 1fr}}
                </style>
                </head>
                <body>
                <div class="wrap">
                  <h1>&#128202; QApilot &mdash; Test Stability Report</h1>
                  <p class="sub">Run-over-run pass/fail stability &middot; rolling window: last %WINDOW% runs &middot; updated %UPDATED%</p>
                  <div class="cards">
                    <div class="card"><div class="k">Runs tracked</div><div class="v">%RUNS%</div></div>
                    <div class="card"><div class="k">Total tests</div><div class="v">%TESTS%</div></div>
                    <div class="card warn"><div class="k">Unstable (flaky)</div><div class="v">%UNSTABLE%</div></div>
                    <div class="card ok"><div class="k">Consistently stable</div><div class="v">%STABLE%</div></div>
                  </div>
                  <table>
                    <thead><tr>
                      <th>Test</th>
                      <th style="text-align:right">Runs</th>
                      <th style="text-align:right">Pass</th>
                      <th style="text-align:right">Fail</th>
                      <th style="text-align:right">Skip</th>
                      <th>History (oldest &rarr; newest)</th>
                      <th style="text-align:right">Flip&nbsp;rate</th>
                      <th>Stability</th>
                    </tr></thead>
                    <tbody>%TBODY%</tbody>
                  </table>
                  <footer>
                    <b>Flip rate</b> = pass&harr;fail transitions &divide; comparable runs.
                    A test is marked <b>Unstable</b> when it both passed and failed inside the window.
                    Data source: <code>tools/test-stability-tracker/history/runs.ndjson</code> &mdash;
                    generated by <a href="https://github.com/Sidpng/QApilot">TestStabilityTracker.java</a>.
                    No database, no UI driving, no manual steps.
                  </footer>
                </div>
                </body>
                </html>
                """
                .replace("%UPDATED%",  esc(updated))
                .replace("%RUNS%",     String.valueOf(distinctRuns))
                .replace("%TESTS%",    String.valueOf(metrics.size()))
                .replace("%UNSTABLE%", String.valueOf(unstableCount))
                .replace("%STABLE%",   String.valueOf(stableCount))
                .replace("%WINDOW%",   String.valueOf(window))
                .replace("%TBODY%",    tbody.toString());
    }

    static String stabilityBadge(Metrics m) {
        if (m.flaky)       return "<span class=\"bdg bdg-unstable\">Unstable</span>";
        if (m.fail > 0)    return "<span class=\"bdg bdg-failing\">Failing</span>";
        return                    "<span class=\"bdg bdg-stable\">Stable</span>";
    }

    static String spark(List<Status> seq) {
        StringBuilder b = new StringBuilder();
        for (Status s : seq)
            b.append("<span class=\"cell ").append(s.name())
             .append("\" title=\"").append(s.name()).append("\"></span>");
        return b.toString();
    }

    static String[] splitTest(String test) {
        int idx = test.lastIndexOf('.');
        if (idx < 0) return new String[]{"", test};
        return new String[]{test.substring(0, idx), test.substring(idx + 1)};
    }

    static String pct(double v) { return Math.round(v * 100) + "%"; }

    // ------------------------------------------------------------- helpers

    static String jq(String s) {
        if (s == null) return "\"\"";
        StringBuilder b = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                default   -> b.append(c);
            }
        }
        return b.append('"').toString();
    }

    static Map<String, String> parseFlatJson(String line) {
        Map<String, String> m = new LinkedHashMap<>();
        String s = line.trim();
        if (s.startsWith("{")) s = s.substring(1);
        if (s.endsWith("}"))   s = s.substring(0, s.length() - 1);
        for (String pair : splitTop(s, ',')) {
            List<String> kv = splitTop(pair, ':');
            if (kv.size() < 2) continue;
            String key = unquote(kv.get(0).trim());
            String val = unquote(String.join(":", kv.subList(1, kv.size())).trim());
            m.put(key, val);
        }
        return m;
    }

    static List<String> splitTop(String s, char delim) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inStr = false, esc = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (esc)  { cur.append(c); esc = false; continue; }
            if (c == '\\') { cur.append(c); esc = true; continue; }
            if (c == '"')  { inStr = !inStr; cur.append(c); continue; }
            if (c == delim && !inStr) { out.add(cur.toString()); cur.setLength(0); continue; }
            cur.append(c);
        }
        out.add(cur.toString());
        return out;
    }

    static String unquote(String s) {
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            String inner = s.substring(1, s.length() - 1);
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < inner.length(); i++) {
                char c = inner.charAt(i);
                if (c == '\\' && i + 1 < inner.length()) {
                    char n = inner.charAt(++i);
                    switch (n) {
                        case 'n'  -> b.append('\n');
                        case 'r'  -> b.append('\r');
                        case 't'  -> b.append('\t');
                        case '"'  -> b.append('"');
                        case '\\' -> b.append('\\');
                        default   -> b.append(n);
                    }
                } else { b.append(c); }
            }
            return b.toString();
        }
        return s;
    }

    static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    static String firstNonBlank(String... vals) {
        for (String v : vals) if (v != null && !v.isBlank()) return v;
        return "";
    }

    static String shortSha(String sha) {
        return (sha != null && sha.length() > 7 && sha.chars().allMatch(c -> Character.digit(c, 16) >= 0))
                ? sha.substring(0, 7) : sha;
    }

    static int  parseInt (String s, int  def) { try { return s == null ? def : Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return def; } }
    static long parseLong(String s, long def) { try { return s == null ? def : Long.parseLong(s.trim());   } catch (NumberFormatException e) { return def; } }
}
