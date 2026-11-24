package dev.maguca.ankimcp;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

public final class Main {
    private Main() { }

    public static void main(String[] args) throws Exception {
        URI endpoint = URI.create(System.getenv().getOrDefault("ANKI_CONNECT_URL", "http://127.0.0.1:8765"));
        AnkiGateway gateway = new HttpAnkiGateway(endpoint, System.getenv("ANKI_CONNECT_API_KEY"));
        var transport = new StdioServerTransportProvider(McpJsonDefaults.getMapper());
        var server = McpServer.sync(transport)
                .serverInfo("anki-mcp-java", "0.1.0")
                .tools(AnkiTools.specifications(gateway))
                .build();
        Runtime.getRuntime().addShutdownHook(new Thread(server::close));
        new CountDownLatch(1).await();
    }
}
