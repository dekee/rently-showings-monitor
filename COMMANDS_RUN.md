# Commands run during this Cursor session

This file documents the **shell commands** executed by the AI assistant while working in this repository, along with a brief explanation of what each command does.

> Note: Some commands were run multiple times (e.g., restarts). When the same command appears repeatedly, it’s because we were restarting or waiting for scheduled polls/log output.

## App start / restart commands

```bash
cd /Users/derrick/Downloads/rently-showings-monitor && ./gradlew bootRun
```
- **What it does**: Changes into the project directory and starts the Spring Boot application via Gradle (`bootRun`).
- **Why it was run**: To run the app so it could scrape Rently, persist results into H2, and emit notifications.

```bash
cd /Users/derrick/Downloads/rently-showings-monitor && ./gradlew bootRun
```
(same command, run again with elevated permissions in the tool runner)
- **What it does**: Same as above, but executed with broader permissions so Gradle/Playwright could access required files.

## Stop the app (free up port 8080)

```bash
cd /Users/derrick/Downloads/rently-showings-monitor && (lsof -ti tcp:8080 | xargs -r kill -TERM) && sleep 1 && (lsof -ti tcp:8080 | xargs -r kill -KILL) || true
```
- **What it does**:
  - `lsof -ti tcp:8080`: finds the PID(s) listening on TCP port 8080.
  - `xargs -r kill -TERM`: sends a graceful termination signal.
  - `sleep 1`: waits briefly.
  - `kill -KILL`: force-kills anything still bound to the port.
  - `|| true`: prevents a non-zero exit from failing the whole command if nothing was running.
- **Why it was run**: To stop the currently running Spring Boot server before restarting.

```bash
kill -TERM 22374 2>/dev/null || true; sleep 1; kill -KILL 22374 2>/dev/null || true
```
- **What it does**: Attempts to stop a specific PID (example from logs), first gracefully then forcefully.
- **Why it was run**: To stop the currently running Spring Boot process when we had a known PID from logs.

## “Wait for logs / scheduled poll” commands

```bash
sleep 35
sleep 50
sleep 60
sleep 70
sleep 95
sleep 140
sleep 200
```
- **What they do**: Pause execution for the specified number of seconds.
- **Why they were run**: To give the app time to start and for the scheduled poll to run so we could read the resulting log output.

## Wipe the database (reset “seen showings”)

```bash
cd /Users/derrick/Downloads/rently-showings-monitor && rm -f data/rently.mv.db data/rently.trace.db
```
- **What it does**: Deletes the H2 file-backed database files (if present).
- **Why it was run**: To reset the DB so the app treats existing showings as “new” again (useful for testing notifications).

```bash
cd /Users/derrick/Downloads/rently-showings-monitor && rm -f data/rently.mv.db data/rently.trace.db data/rently.lock.db
```
- **What it does**: Same as above, also removing the H2 lock file (if present).
- **Why it was run**: Full DB reset before restarting the app.

## Quick environment checks / no-op commands

```bash
cd /Users/derrick/Downloads/rently-showings-monitor && sqlite3 --version || true
```
- **What it does**: Prints the installed `sqlite3` version (or does nothing if `sqlite3` is unavailable).
- **Why it was run**: Quick check of local tooling availability (not required for H2).

```bash
cd /Users/derrick/Downloads/rently-showings-monitor && ./gradlew -q tasks --all > /dev/null 2>&1 || true
```
- **What it does**: Lists Gradle tasks quietly and discards output.
- **Why it was run**: A harmless command used as a quick Gradle sanity check.


