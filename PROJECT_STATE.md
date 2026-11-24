# Project state

## Implemented

- Java 17 Maven project with official MCP SDK stdio server and shaded executable jar.
- Eleven focused Anki tools for decks, note types, search, details, note creation and updates, and tag changes.
- AnkiConnect v6 HTTP adapter with loopback restriction, optional API key, timeouts, envelope checks, and 4 MiB response limit.
- CI workflow and adapter tests.

## Verification

- `mvn verify`: passed locally (3 adapter tests).
- MCP initialize and tools/list passed; read-only `anki_list_decks` passed against the running local AnkiConnect listener (40 decks, names not recorded).
- GitHub repository: public at https://github.com/maguca/anki-mcp-java; main branch pushed.

## Next steps

- First GitHub Actions build passed. Future options:
- Future options: v1 API adapter, pagination for broad searches, more explicit note creation options, distribution releases.
