# Deployment and operation

This is a local process, not a hosted service. No server infrastructure or cloud deployment is required.

1. Install Anki Desktop and the AnkiConnect add-on (`2055492159`), restart Anki, and open the intended profile.
2. Build with `mvn verify` using Java 17 or newer.
3. Configure the MCP client to launch `java -jar /absolute/path/to/target/anki-mcp-java-0.1.0-all.jar` over stdio.
4. Optionally set `ANKI_CONNECT_URL` (default `http://127.0.0.1:8765`) and `ANKI_CONNECT_API_KEY` in the client's environment.
5. Restart the client, list tools, and call `anki_list_decks` as a live smoke check.

The endpoint must be a loopback HTTP root URL. Keep Anki running; its active profile is the target. API keys belong in local environment configuration, never in Git. The process needs no inbound network port. If Anki is unavailable, tools return a connection error; verify add-on installation, listener settings, and profile state. The jar is built locally or by CI; no release publication is configured.
