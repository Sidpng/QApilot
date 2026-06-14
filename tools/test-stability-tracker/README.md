# Test Stability Tracker

A self-contained, zero-dependency test analytics tool built into QApilot.

Reads the Surefire/TestNG JUnit XML your suite already produces, appends each
run to a version-controlled NDJSON history file, computes per-test stability
metrics (run-over-run pass/fail toggling), and renders a static HTML dashboard
published to GitHub Pages — no database, no UI driving, no manual steps.

## How it works

```
mvn test
  └── target/surefire-reports/TEST-*.xml
         │
         ▼
   TestStabilityTracker.java        (JDK 17 single-file, stdlib only)
         │
         ├── tools/test-stability-tracker/history/runs.ndjson
         │       append-only run log — committed to git, no DB needed
         │
         └── test-reports/dashboard/index.html
                 static dashboard — committed + deployed to GitHub Pages
```

**Stability definition:** a test is flagged **Unstable** when it both passed
and failed inside the rolling window without a code change that explains it.
The *flip rate* measures how often adjacent runs toggle between pass and fail.

## Dashboard columns

| Column | Meaning |
|--------|---------|
| Test | Fully-qualified class + method |
| Runs | Executions inside the window |
| Pass / Fail / Skip | Counts for the window |
| History | Colour spark — green pass · red fail · grey skip (oldest → newest) |
| Flip rate | Pass↔fail transitions ÷ comparable runs |
| Stability | **Stable** · **Unstable** · **Failing** |

## Running locally

```bash
# After mvn test — ingest latest run and re-render
java tools/test-stability-tracker/TestStabilityTracker.java \
     target/surefire-reports \
     tools/test-stability-tracker/history/runs.ndjson \
     test-reports/dashboard/index.html \
     --browser chromium \
     --window 30

# Re-render dashboard without ingesting (point arg[0] at a non-existent path)
java tools/test-stability-tracker/TestStabilityTracker.java \
     /nonexistent \
     tools/test-stability-tracker/history/runs.ndjson \
     test-reports/dashboard/index.html
```

Open `test-reports/dashboard/index.html` locally after running.

## CI / automation

The `test-stability-tracker.yml` workflow triggers automatically after
`playwright-ci` completes on `main`. It:

1. Re-runs the suite (chromium, headless) to get fresh surefire XML.
2. Runs `TestStabilityTracker.java` to ingest and re-render.
3. Commits `runs.ndjson` + `index.html` back to `main`.
4. Deploys `test-reports/dashboard/` to GitHub Pages.

**One-time GitHub setup:** go to **Settings → Pages → Source** and choose
**GitHub Actions**. After the first run the live dashboard is available at
`https://Sidpng.github.io/QApilot/`.

## File layout

```
tools/test-stability-tracker/
├── TestStabilityTracker.java      aggregator — no deps, no build step needed
├── history/
│   └── runs.ndjson                append-only run log (version-controlled)
└── README.md

test-reports/
└── dashboard/
    └── index.html                 generated stability report (committed + deployed)

.github/workflows/
└── test-stability-tracker.yml     nightly ingestion + Pages deployment
```
