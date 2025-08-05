package net.geant.nmaas.openapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OpenApiExportTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void exportOpenApiAsYaml() throws Exception {
        var outputPath = Path.of("build/openapi/openapi.yaml");
        Files.createDirectories(outputPath.getParent());

        // Call the standard SpringDoc OpenAPI endpoint
        ResponseEntity<String> response = restTemplate.getForEntity("/api-docs/spec", String.class);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected 200 OK from /api-docs/spec endpoint");
        assertNotNull(response.getBody(), "Response body should not be null");

        // Convert JSON -> Map -> YAML
        Map<String, Object> openApiMap = objectMapper.readValue(response.getBody(), Map.class);

        // Validate basic OpenAPI structure
        assertNotNull(openApiMap.get("info"), "OpenAPI info section should be present");
        assertNotNull(openApiMap.get("paths"), "OpenAPI paths section should be present");

        // Configure YAML output
        var options = new DumperOptions();
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        var yaml = new Yaml(options);

        // Write to file
        try (var writer = Files.newBufferedWriter(outputPath)) {
            yaml.dump(openApiMap, writer);
        }

        System.out.println("✅ OpenAPI YAML written to: " + outputPath);
    }
}