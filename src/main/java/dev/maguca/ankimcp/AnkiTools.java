package dev.maguca.ankimcp;

import com.fasterxml.jackson.databind.JsonNode;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Explicit allowlist of collection operations; no arbitrary action passthrough. */
public final class AnkiTools {
    private AnkiTools() { }
    private static final Map<String,Object> STRING = Map.of("type","string","minLength",1);
    private static final Map<String,Object> ID = Map.of("type","integer","minimum",1);
    private static final Map<String,Object> IDS = Map.of("type","array","items",ID,"minItems",1,"maxItems",100);
    private static final Map<String,Object> FIELDS = Map.of("type","object","additionalProperties",Map.of("type","string"),"minProperties",1);

    public static List<SyncToolSpecification> specifications(AnkiGateway gateway) {
        return List.of(
            tool("anki_list_decks", "List deck names in the active Anki profile.", Map.of(), List.of(), a -> gateway.invoke("deckNames", Map.of())),
            tool("anki_list_note_types", "List Anki note types.", Map.of(), List.of(), a -> gateway.invoke("modelNames", Map.of())),
            tool("anki_note_type_fields", "List fields of a note type.", Map.of("noteType",STRING), List.of("noteType"), a -> gateway.invoke("modelFieldNames", Map.of("modelName", str(a,"noteType")))),
            tool("anki_find_notes", "Find note IDs using Anki search syntax.", Map.of("query",STRING), List.of("query"), a -> gateway.invoke("findNotes", Map.of("query",str(a,"query")))),
            tool("anki_get_notes", "Get details for 1 to 100 note IDs.", Map.of("noteIds",IDS), List.of("noteIds"), a -> gateway.invoke("notesInfo", Map.of("notes",ids(a,"noteIds")))),
            tool("anki_find_cards", "Find card IDs using Anki search syntax.", Map.of("query",STRING), List.of("query"), a -> gateway.invoke("findCards", Map.of("query",str(a,"query")))),
            tool("anki_get_cards", "Get details for 1 to 100 card IDs.", Map.of("cardIds",IDS), List.of("cardIds"), a -> gateway.invoke("cardsInfo", Map.of("cards",ids(a,"cardIds")))),
            tool("anki_add_note", "Create a note in a deck; changes the collection.", Map.of("deck",STRING,"noteType",STRING,"fields",FIELDS), List.of("deck","noteType","fields"), a -> gateway.invoke("addNote", Map.of("note",Map.of("deckName",str(a,"deck"),"modelName",str(a,"noteType"),"fields",fields(a))))),
            tool("anki_update_note_fields", "Replace named fields on a note; changes the collection.", Map.of("noteId",ID,"fields",FIELDS), List.of("noteId","fields"), a -> gateway.invoke("updateNoteFields", Map.of("note",Map.of("id",id(a,"noteId"),"fields",fields(a))))),
            tool("anki_add_tags", "Add space-separated tags to 1 to 100 notes.", Map.of("noteIds",IDS,"tags",STRING), List.of("noteIds","tags"), a -> gateway.invoke("addTags", Map.of("notes",ids(a,"noteIds"),"tags",str(a,"tags")))),
            tool("anki_remove_tags", "Remove space-separated tags from 1 to 100 notes.", Map.of("noteIds",IDS,"tags",STRING), List.of("noteIds","tags"), a -> gateway.invoke("removeTags", Map.of("notes",ids(a,"noteIds"),"tags",str(a,"tags"))))
        );
    }
    private static SyncToolSpecification tool(String name, String description, Map<String,Object> properties, List<String> required, Function<Map<String,Object>,JsonNode> action) {
        Map<String,Object> schema = Map.of("type","object","properties",properties,"required",required,"additionalProperties",false);
        return SyncToolSpecification.builder().tool(Tool.builder(name,schema).description(description).build())
            .callHandler((exchange,request) -> {
                try {
                    Map<String,Object> args = request.arguments() == null ? Map.of() : request.arguments();
                    return CallToolResult.builder().content(List.of(new McpSchema.TextContent(action.apply(args).toString()))).build();
                } catch (AnkiException | IllegalArgumentException e) {
                    return CallToolResult.builder().isError(true).content(List.of(new McpSchema.TextContent(e.getMessage()))).build();
                }
            }).build();
    }
    private static String str(Map<String,Object> args, String key) {
        if (!(args.get(key) instanceof String value) || value.isBlank()) throw new IllegalArgumentException(key + " must be a nonempty string");
        return value;
    }
    private static long id(Map<String,Object> args, String key) {
        if (!(args.get(key) instanceof Number n) || n.longValue() <= 0 || n.doubleValue() != n.longValue()) throw new IllegalArgumentException(key + " must be a positive integer");
        return n.longValue();
    }
    private static List<Long> ids(Map<String,Object> args, String key) {
        if (!(args.get(key) instanceof List<?> list) || list.isEmpty() || list.size() > 100) throw new IllegalArgumentException(key + " must have 1 to 100 IDs");
        List<Long> result = new ArrayList<>();
        for (Object value : list) result.add(id(Map.of("id",value),"id"));
        return result;
    }
    private static Map<String,String> fields(Map<String,Object> args) {
        if (!(args.get("fields") instanceof Map<?,?> map) || map.isEmpty()) throw new IllegalArgumentException("fields must be a nonempty object");
        var result = new java.util.LinkedHashMap<String,String>();
        for (var entry : map.entrySet()) {
            if (!(entry.getKey() instanceof String key) || key.isBlank() || !(entry.getValue() instanceof String value)) throw new IllegalArgumentException("fields must map names to strings");
            result.put(key,value);
        }
        return result;
    }
}
