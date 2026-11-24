# Architecture

```text
MCP client --stdio JSON-RPC--> Main / official MCP Java SDK
                                 | tool catalog + validation (AnkiTools)
                                 | AnkiGateway interface
                                 v
                            HttpAnkiGateway --loopback HTTP--> AnkiConnect --> active Anki profile
```

- **Transport:** local stdio, legacy MCP server API for clients using the initialization handshake. The official SDK owns framing, capability negotiation, and schema validation.
- **Tool facade:** explicit tools with JSON Schemas, bounded detail requests, and MCP tool errors for expected input or Anki failures.
- **Adapter:** Java `HttpClient` with connect and request timeouts; Jackson decodes the AnkiConnect v6 `result`/`error` envelope. Optional API key is read from the process environment. Only loopback HTTP root URLs are allowed.
- **State:** none in the server. Anki Desktop and its active profile own collection data. The server does not initiate Anki sync.
- **Extensibility:** `AnkiGateway` separates tools from the wire protocol so a v1 adapter can be introduced later without changing the MCP catalog.

Decisions: a focused allowlist avoids exposing arbitrary AnkiConnect operations; no remote MCP listener is opened; writes are named plainly in tool descriptions. Large note and card detail requests are capped at 100 IDs, and HTTP responses at 4 MiB. Search actions can still return large ID lists, so callers should narrow queries.
