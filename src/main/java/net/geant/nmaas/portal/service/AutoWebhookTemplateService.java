package net.geant.nmaas.portal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.webhooks.DomainActionDto;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import net.geant.nmaas.api.dto.webhooks.AppDeploymentWebhookDto;
import net.geant.nmaas.api.dto.webhooks.DomainGroupWebhookDto;
import net.geant.nmaas.api.dto.webhooks.UserDomainAssignmentWebhookDto;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutoWebhookTemplateService {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$[A-Z0-9_]+");
    private static final String RAW_VAR_SENTINEL = "__RAW_VAR__";

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private final Map<WebhookEventType, Class<?>> eventToDtoClass = new EnumMap<>(WebhookEventType.class);

    @PostConstruct
    void init() {
        eventToDtoClass.put(WebhookEventType.APPLICATION_DEPLOYMENT, AppDeploymentWebhookDto.class);
        eventToDtoClass.put(WebhookEventType.APPLICATION_REMOVAL, AppDeploymentWebhookDto.class);
        eventToDtoClass.put(WebhookEventType.DOMAIN_ACTION, DomainActionDto.class);
        eventToDtoClass.put(WebhookEventType.DOMAIN_GROUP_ACTION, DomainGroupWebhookDto.class);
        eventToDtoClass.put(WebhookEventType.USER_ASSIGNMENT, UserDomainAssignmentWebhookDto.class);
    }

    /**
     * produce a JSON string based on template and payload
     * @param template
     * @param payload
     * @return
     */
    public String render(String template, Object payload) throws JsonProcessingException {
        if (template == null || template.isBlank()) {
            return getDefaultJson(payload);
        }

        Map<String, Object> context = buildContext(payload);

        // 1. Pre-process the template string to allow Jackson to parse the JSON.
        // Replace all $VAR_NAME with a unique, valid JSON string placeholder.
        String templateForParsing = template;

        Set<String> placeholders = extractPlaceholders(template);
        for (String var : placeholders) {
            // Replace the variable name with a unique quoted marker string.
            templateForParsing = templateForParsing.replace(var, "\"" + RAW_VAR_SENTINEL + var + "\"");
        }
        // 2. Parse the temporary JSON string into a mutable JsonNode tree.
        JsonNode rootNode = mapper.readTree(templateForParsing);
        // 3. Traverse the tree and perform the type-safe substitution.
        // The substitution now looks for the quoted string pattern "RAW_VAR_SENTINEL$VAR_NAME"
        JsonNode resultNode = replaceVariablesInNode(rootNode, context);
        // 4. Serialize the final JsonNode tree back to a String.
        return mapper.writeValueAsString(resultNode);
    }

    // ⚠️ The replaceVariablesInNode must now look for the quoted string token!
    private JsonNode replaceVariablesInNode(JsonNode node, Map<String, Object> context) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;

            // Use an iterator to safely modify the node while traversing
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                JsonNode child = field.getValue();

                JsonNode replacedChild = replaceVariablesInNode(child, context);

                // If the child was replaced (meaning it was a simple variable token),
                // put the new, type-safe value back.
                if (replacedChild != child) {
                    // Remove the old node and add the new one.
                    objectNode.set(field.getKey(), replacedChild);
                }
            }
            return objectNode;
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode replacedItem = replaceVariablesInNode(arrayNode.get(i), context);
                if (replacedItem != arrayNode.get(i)) {
                    arrayNode.set(i, replacedItem);
                }
            }
            return arrayNode;
        } else if (node.isTextual()) {
            String text = node.textValue();

            // Check if the text starts with the placeholder and contains a variable name
            if (text.startsWith(RAW_VAR_SENTINEL) && text.length() > RAW_VAR_SENTINEL.length() + 1) {
                String var = text.substring(RAW_VAR_SENTINEL.length());
                if (context.containsKey(var)) {
                    Object value = context.get(var);
                    return mapper.valueToTree(value); // Type-safe replacement
                } else {
                    return mapper.nullNode();
                }
            }
        }
        return node;
    }

    /**
     * default JSON if not defined configured
     * @param payload
     * @return
     */
    private String getDefaultJson(Object payload) {
        if (payload == null ) {
            return "{}";
        }

        try {
            return mapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return "{}"; // fallback on JSON serialization error
        }
    }

    public Set<String> getAvailableVariables(WebhookEventType eventType) {
        Class<?> clazz = eventToDtoClass.get(eventType);
        if (clazz == null) return Set.of();

        Object dummy = createDummyInstance(clazz);
        return buildContext(dummy).keySet();
    }

    /**
     * get default template based on event type
     * @param eventType
     * @return
     */
    public String getDefaultTemplate(WebhookEventType eventType) {
        Class<?> clazz = eventToDtoClass.get(eventType);
        if (clazz == null) return "{}";

        Object dummy = createDummyInstance(clazz);
        JsonNode structure = mapper.valueToTree(dummy);

        ObjectNode templateRoot = mapper.createObjectNode();
        buildTemplateNode(structure, "", templateRoot);

        try {
            String templateString = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(templateRoot);

            //Use regex to remove quotes and sentinel prefix, leaving raw variable
            // The regex looks for: "UNQUOTED_VAR:$VARIABLE_NAME" and replaces it with $VARIABLE_NAME
            //  Need to escape the sentinel prefix
            String escapedSentinel = Pattern.quote(RAW_VAR_SENTINEL);
            Pattern unquotePattern = Pattern.compile("\"" + escapedSentinel + VARIABLE_PATTERN.pattern() + "\"");
            StringBuffer result = new StringBuffer();
            Matcher matcher = unquotePattern.matcher(templateString);

            while (matcher.find()) {
                String matchedString = matcher.group();
                String variableOnly = matchedString.substring(
                        1 + RAW_VAR_SENTINEL.length(),
                        matchedString.length() - 1
                );
                matcher.appendReplacement(result, Matcher.quoteReplacement(variableOnly));
            }
            matcher.appendTail(result);

            return result.toString();

        } catch (JsonProcessingException e) {
            log.error("Problem generating default template", e);
            return "{}";
        }
    }

    private Object createDummyInstance(Class<?> clazz) {
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();

            // Walk all fields and fill them with realistic dummy data
            for (Field field : clazz.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                if (field.get(instance) != null) {
                    continue;
                }

                Object dummyValue = createDummyValue(field.getType(), field.getGenericType());
                field.set(instance, dummyValue);
            }
            return instance;
        } catch (Exception e) {
            log.error("Problem creating dummy instance",e);
            throw new RuntimeException(e);
        }
    }

    private Object createDummyValue(Class<?> type, Type genericType) {
        if (type == String.class) return "dummy_" + type.getSimpleName().toLowerCase();
        if (type == Long.class || type == long.class) return 999L;
        if (type == Integer.class || type == int.class) return 999;
        if (type == Boolean.class || type == boolean.class) return true;
        if (type == LocalDateTime.class) return LocalDateTime.now();
        if (type.isEnum()) return type.getEnumConstants()[0];
        if (Map.class.isAssignableFrom(type)) return Map.of("key", "value");

        if (List.class.isAssignableFrom(type) || Set.class.isAssignableFrom(type)) {
            if (genericType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                if (typeArguments.length == 1 && typeArguments[0] instanceof Class) {
                    Class<?> itemClass = (Class<?>) typeArguments[0];
                    // Recursively create one dummy item of the correct generic type
                    Object dummyItem = createDummyValue(itemClass, itemClass);

                    if (List.class.isAssignableFrom(type)) {
                        return List.of(dummyItem);
                    } else if (Set.class.isAssignableFrom(type)) {
                        return Set.of(dummyItem);
                    }
                }
            }
            // Fallback for non-parameterized or complex collections
            return List.of("dummy_item");
        }

        // For nested objects: recursively create dummy
        if (type.getPackage() != null && type.getPackage().getName().startsWith("net.geant.nmaas")) {
            try {
                Object nested = type.getDeclaredConstructor().newInstance();
                for (Field f : type.getDeclaredFields()) {
                    if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                    f.setAccessible(true);
                    if (f.get(nested) == null) {
                        f.set(nested, createDummyValue(f.getType(), f.getGenericType()));
                    }
                }
                return nested;
            } catch (Exception e) {
                return null;
            }
        }

        return null; // fallback
    }

    private void buildTemplateNode(JsonNode node, String path, ObjectNode parent) {
        node.fields().forEachRemaining(entry -> {
            String field = entry.getKey();
            String fullPath = path.isEmpty() ? field : path + "_" + field;
            String var = "$" + fullPath.toUpperCase();

            JsonNode value = entry.getValue();

            if (value.isObject()) {
                ObjectNode obj = mapper.createObjectNode();
                parent.set(field, obj);
                buildTemplateNode(value, fullPath, obj);

            } else if (value.isArray()) {
                ArrayNode array = mapper.createArrayNode();
                parent.set(field, array);

                if (value.size() > 0 && value.get(0).isObject()) {
                    ObjectNode item = mapper.createObjectNode();
                    array.add(item);
                    buildTemplateNode(value.get(0), fullPath + "[0]", item);
                } else {
                    // Array of primitives/simple types
                    ArrayNode innerArray = mapper.createArrayNode();
                    innerArray.add(TextNode.valueOf(var));
                    parent.set(field, innerArray);
                }

            } else if (value.isNull() || value.isTextual() || value.isNumber() || value.isBoolean()) {
                parent.put(field, RAW_VAR_SENTINEL + var);
            } else {
                parent.put(field, var);
            }
        });
    }

    private Map<String, Object> buildContext(Object payload) {
        if (payload == null) return Map.of();

        try {
            JsonNode root = mapper.valueToTree(payload);
            Map<String, Object> context = new LinkedHashMap<>();
            flattenForContext(root, "", context);
            return context;
        } catch (Exception e) {
            log.error("Cannot build webhook context",e);
            throw new RuntimeException("Cannot build webhook context", e);
        }
    }

    // Only expose variables for primitive/simple types
    private void flattenForContext(JsonNode node, String path, Map<String, Object> context) {
        node.fields().forEachRemaining(entry -> {
            String fullPath = path.isEmpty() ? entry.getKey() : path + "_" + entry.getKey();
            String var = "$" + fullPath.toUpperCase();
            JsonNode value = entry.getValue();

            if (!value.isObject() && !(value.isArray() && value.size() > 0 && value.get(0).isObject())) {
                context.put(var, mapper.convertValue(value, Object.class));
            }

            // Recursively flatten complex types (objects and arrays of objects)
            if (value.isObject() || (value.isArray() && value.size() > 0 && value.get(0).isObject())) {
                flattenForContext(value, fullPath, context);
            }
        });
    }

    /**
     * Validates a given template string, throwing IllegalArgumentException for any of the possible validation errors.
     * 1. Checks if the template is valid JSON.
     * 2. Checks if all $variables are valid for the given event type.
     * * @param template The user-provided template string.
     * @param eventType The type of webhook event.
     * @return true if the template is valid JSON and contains only valid variables, false otherwise.
     */
    public void validateTemplate(String template, WebhookEventType eventType) {
        if (template != null && !template.isBlank()) {
            // 1. JSON Syntax Check
            try {
                // Since the template uses unquoted variables (e.g., "key": $VAR),
                // it is NOT valid JSON unless all variables are replaced.
                // We must use a temporary substitution to replace all variables with a placeholder
                // that is VALID JSON for any type (e.g., 'null' which is valid JSON, or a dummy string).

                String temporaryJson = extractPlaceholders(template).stream()
                        .reduce(template, (t, var) -> t.replace(var, "null"));
                mapper.readTree(temporaryJson);
            } catch (JsonProcessingException e) {
                log.error("Template JSON Syntax Check failed due to: ", e);
                throw new IllegalArgumentException("Template JSON Syntax Check failed");
            }

            // 2. Variable Validity Check
            // If the template requires any variable that is not in the available set, it is invalid.
            Set<String> availableVariables = getAvailableVariables(eventType);
            extractPlaceholders(template).stream()
                    .filter(var -> !availableVariables.contains(var))
                    .findFirst()
                    .ifPresent(var -> {
                        throw new IllegalArgumentException(
                                "%s is not a valid variable for this webhook".formatted(var)
                        );
                    });
        }
    }

    private Set<String> extractPlaceholders(String template) {
        Set<String> placeholders = new HashSet<>();
        Matcher matcher = VARIABLE_PATTERN.matcher(template);

        while (matcher.find()) {
            placeholders.add(matcher.group());
        }
        return placeholders;
    }
}