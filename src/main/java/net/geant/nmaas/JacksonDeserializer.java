package net.geant.nmaas;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

@Slf4j
public class JacksonDeserializer extends ValueDeserializer<String> {
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        log.info("Launching deserializer");
        JsonNode node = p.getCodec().readTree(p);

        if (node == null || node.isNull()) {
            return null;
        }
        // If the incoming JSON value is a string, store it as plain text (no surrounding quotes).
        if (node.isTextual()) {
            return node.asText();
        }
        // If it's an object/array/number/bool, store the JSON representation.
        return node.toString();
    }

}
