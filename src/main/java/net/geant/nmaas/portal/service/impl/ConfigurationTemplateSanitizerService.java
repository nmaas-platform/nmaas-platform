package net.geant.nmaas.portal.service.impl;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigurationTemplateSanitizerService {

    private static final String OLD_KEY = "#";
    private static final String NEW_KEY = "_dot_";

    private final ObjectMapper objectMapper;

    public String sanitizeConfigurationJson(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            sanitizeKeysRecursively(root);
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing configuration template", e);
        }
    }

    /**
     * Iterate over JSON and change # to _dot_ in key fields
     */
    private void sanitizeKeysRecursively(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objNode = (ObjectNode) node;
            if (objNode.has("key")) {
                String key = objNode.get("key").asText();
                if (key.contains(OLD_KEY)) {
                    objNode.put("key", key.replace(OLD_KEY, NEW_KEY));
                }
            }

            objNode.fields().forEachRemaining(entry -> {
                sanitizeKeysRecursively(entry.getValue());
            });

        } else if (node.isArray()) {
            for (JsonNode item : node) {
                sanitizeKeysRecursively(item);
            }
        }
    }

}
