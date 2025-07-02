package net.geant.nmaas.portal.service.impl;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

@Service
public class FormioSanitizerService {

    private final ObjectMapper objectMapper;

    public FormioSanitizerService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String sanitizeFormioJson(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            sanitizeKeysRecursively(root);
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing formio", e);
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
                if (key.contains("#")) {
                    objNode.put("key", key.replace("_", "_dot_"));
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
