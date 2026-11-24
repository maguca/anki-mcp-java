package dev.maguca.ankimcp;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

/** Boundary around AnkiConnect; replaceable in tests and future API migrations. */
public interface AnkiGateway {
    JsonNode invoke(String action, Map<String, ?> params);
}
