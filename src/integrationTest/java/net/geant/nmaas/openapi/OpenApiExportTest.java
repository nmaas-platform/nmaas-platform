package net.geant.nmaas.openapi;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
public class OpenApiExportTest {

    private final RestTestClient testClient;

    private final JsonMapper jsonMapper;

    public OpenApiExportTest(@Autowired JsonMapper jsonMapper) {
        this.testClient = RestTestClient.bindToServer()
                .baseUrl("http://localhost:9000")
                .build();
        this.jsonMapper = jsonMapper;
    }

    @Test
    void exportOpenApiAsJson() throws Exception {
        var outputPath = Path.of("build/openapi/openapi.json");
        Files.createDirectories(outputPath.getParent());

        // Call the standard SpringDoc OpenAPI endpoint
        EntityExchangeResult<String> result = testClient.get().uri("/api-docs/spec")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult();

        // Verify the response
        assertEquals(HttpStatus.OK, result.getStatus(), "Expected 200 OK from /api-docs/spec endpoint");
        assertNotNull(result.getResponseBody(), "Response body should not be null");

        // Convert JSON -> Map -> YAML
        Map<String, Object> openApiMap = jsonMapper.readValue(result.getResponseBody(), Map.class);

        // Validate basic OpenAPI structure
        assertNotNull(openApiMap.get("info"), "OpenAPI info section should be present");
        assertNotNull(openApiMap.get("paths"), "OpenAPI paths section should be present");

        // Write to file
        Files.writeString(outputPath, result.getResponseBody());

        log.info("OpenAPI JSON written to: {}", outputPath);
    }

}
