# Anki MCP Java

A local [Model Context Protocol](https://modelcontextprotocol.io/) server that gives MCP clients focused tools for an active Anki Desktop collection through [AnkiConnect](https://github.com/FooSoft/anki-connect). Written in Java 17 with the official MCP Java SDK. The server uses stdio and makes HTTP requests only to loopback.

## Requirements

- Java 17 or newer and Maven 3.9 or newer to build
- Anki Desktop running with the AnkiConnect add-on (`2055492159`) installed, and the intended profile open

## Build

```sh
mvn verify
```

The executable jar is `target/anki-mcp-java-0.1.0-all.jar`.

## Configure an MCP client

Add a stdio server with command `java` and arguments `-jar`, then the **absolute path** to the executable jar. For example:

```json
{
  "mcpServers": {
    "anki": {
      "command": "java",
      "args": ["-jar", "/absolute/path/to/anki-mcp-java/target/anki-mcp-java-0.1.0-all.jar"],
      "env": {"ANKI_CONNECT_URL": "http://127.0.0.1:8765"}
    }
  }
}
```

Client configuration formats vary. Restart the client after adding the server. The server starts without Anki, but tool calls require a running AnkiConnect listener. If AnkiConnect requires an API key, set `ANKI_CONNECT_API_KEY` in the MCP client's environment. Do not commit that key. Output on stdout is reserved for MCP messages; diagnostics use stderr.

## Tools

| Tool | Purpose |
| --- | --- |
| `anki_list_decks` | List deck names |
| `anki_list_note_types` | List note types |
| `anki_note_type_fields` | List fields for a note type |
| `anki_find_notes` / `anki_get_notes` | Search notes, then fetch up to 100 at a time |
| `anki_find_cards` / `anki_get_cards` | Search cards, then fetch up to 100 at a time |
| `anki_add_note` | Create a note from deck, note type, and field map |
| `anki_update_note_fields` | Update specified fields of a note |
| `anki_add_tags` / `anki_remove_tags` | Change tags on up to 100 notes |

Search queries use [Anki search syntax](https://docs.ankiweb.net/searching.html). Note and card IDs are positive integers. `anki_add_note` requires field names matching the selected note type; use `anki_note_type_fields` first. AnkiConnect errors are returned as MCP tool errors. Changes affect the active Anki profile immediately; Anki's normal sync behavior applies.

## Design and limits

The adapter uses the established AnkiConnect version 6 action API. AnkiConnect now also offers a newer v1 API, which is not implemented here. The server intentionally exposes a small action allowlist; it has no generic action executor, deletion, scheduling, media, or sync tools. The configurable endpoint is restricted to loopback HTTP. Responses larger than 4 MiB are rejected. Large search results should be narrowed before fetching details.

See [ARCHITECTURE.md](ARCHITECTURE.md), [DEPLOYMENT.md](DEPLOYMENT.md), and [PROJECT_STATE.md](PROJECT_STATE.md). Contributions: run `mvn verify`, keep tools narrow, and add tests for protocol or input handling changes.

## License

MIT. See [LICENSE](LICENSE).
