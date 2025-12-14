# Rently Showings Monitor (Kotlin + Spring Boot + Playwright)

This is a complete Spring Boot app that polls the **Rently Manager → Leasing Activities → Showings** table and prints an alert when a **new showing** appears.

Why Playwright?
- The Showings table is often rendered by JavaScript; a plain HTTP fetch may return no table rows.

## What it scrapes
- Name
- Showing date
- Feedback
- Source

A stable fingerprint is generated per row:
`name + "|" + showingDate + "|" + source`

Fingerprints are stored in an embedded **H2** DB (file-backed), so you won’t get duplicate alerts across restarts.

## Prereqs
- Java 17+
- Internet access on first run so Playwright can download its browser runtime

## Configure
Edit: `src/main/resources/application.yml`

Key settings:
- `rently.url` (required): your activity log URL
- `rently.poll-delay-ms`: poll interval (default 300000 = 5 minutes)
- `rently.headless`: true/false
- `rently.storage-state-path`: optional (persist cookies/session)

## Run (with installed Gradle)
```bash
gradle bootRun
```

## Or build a jar
```bash
gradle clean bootJar
java -jar build/libs/rently-showings-monitor-0.0.1-SNAPSHOT.jar
```

## Output
When new showings are detected, it prints:
- name | showing date | feedback | source

You can replace the `ConsoleNotifier` with Email/SMS/Slack later.
