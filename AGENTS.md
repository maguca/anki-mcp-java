# Project instructions

- Java 17 minimum, Maven build, official MCP Java SDK.
- Keep MCP protocol wiring in `Main`, tool definitions and validation in `AnkiTools`, and AnkiConnect HTTP handling in `HttpAnkiGateway` behind `AnkiGateway`.
- Preserve stdio stdout exclusively for protocol messages; diagnostics go to stderr.
- Expose only named, documented Anki actions. Validate arguments and bound collection reads. Keep the HTTP endpoint on loopback.
- Add focused tests for transport, envelope, and validation behavior. Run `mvn verify` before committing.
- Maintain README, PROJECT_STATE, ARCHITECTURE, and DEPLOYMENT as behavior changes. Never record secrets or private card content.
- Before each commit verify effective Git author and committer are `maguca <11622773+maguca@users.noreply.github.com>` including environment overrides. Do not rewrite history.
