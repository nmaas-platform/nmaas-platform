package net.geant.nmaas;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "nmaas Platform API",
                version = "1.10.0",
                description = "REST API for managing nmaas Platform resources"
        )
)
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer tagGroupsCustomizer() {
        return openApi -> {
            List<String> domainManagementTags = List.of(
                    "Domains",
                    "Domain Groups",
                    "Resources Limits"
            );
            List<String> userManagementTags = List.of(
                    "Users",
                    "User Profiles",
                    "User SSH Keys",
                    "Custom Access Tokens"
            );
            List<String> applicationManagementTags = List.of(
                    "Applications",
                    "Application Subscriptions",
                    "Application Tags",
                    "Application Screenshots",
                    "Application Ratings",
                    "Application Comments"
            );
            List<String> applicationInstanceManagementTags = List.of(
                    "Application Instances",
                    "Application Instance Configuration",
                    "Application Instance Logs",
                    "Application Instance Shell"
            );
            List<String> webhookManagementTags = List.of(
                    "Webhooks",
                    "Webhook History",
                    "Webhook Templates"
            );
            List<String> notificationsManagementTags = List.of(
                    "Notifications",
                    "Mail Templates",
                    "Contact Forms"
            );
            List<String> authenticationTags = List.of(
                    "Basic Authentication",
                    "OIDC Authentication",
                    "User Registration"
            );
            List<String> platformManagementTags = List.of(
                    "Platform Configuration",
                    "Kubernetes",
                    "Remote Kubernetes Clusters",
                    "Platform Monitoring",
                    "Software Information"
            );

            List<Map<String, Object>> tagGroups = new ArrayList<>(List.of(
                    Map.of(
                            "name", "Domain Management",
                            "tags", domainManagementTags
                    ),
                    Map.of(
                            "name", "User Management",
                            "tags", userManagementTags
                    ),
                    Map.of(
                            "name", "Application Management",
                            "tags", applicationManagementTags
                    ),
                    Map.of(
                            "name", "Application Instance Management",
                            "tags", applicationInstanceManagementTags
                    ),
                    Map.of(
                            "name", "Webhook Management",
                            "tags", webhookManagementTags
                    ),
                    Map.of(
                            "name", "Notifications Management",
                            "tags", notificationsManagementTags
                    ),
                    Map.of(
                            "name", "Authentication",
                            "tags", authenticationTags
                    ),
                    Map.of(
                            "name", "Platform Management",
                            "tags", platformManagementTags
                    )
            ));

            Set<String> groupedTags = new LinkedHashSet<>();
            groupedTags.addAll(domainManagementTags);
            groupedTags.addAll(userManagementTags);
            groupedTags.addAll(applicationManagementTags);
            groupedTags.addAll(applicationInstanceManagementTags);
            groupedTags.addAll(webhookManagementTags);
            groupedTags.addAll(notificationsManagementTags);
            groupedTags.addAll(authenticationTags);
            groupedTags.addAll(platformManagementTags);

            Set<String> ungroupedTags = new LinkedHashSet<>();
            if (openApi.getTags() != null) {
                openApi.getTags().forEach(tag -> {
                    if (tag.getName() != null && !groupedTags.contains(tag.getName())) {
                        ungroupedTags.add(tag.getName());
                    }
                });
            }
            if (openApi.getPaths() != null) {
                openApi.getPaths().values().forEach(pathItem ->
                        pathItem.readOperations().forEach(operation -> {
                            if (operation.getTags() != null) {
                                operation.getTags().stream()
                                        .filter(tag -> !groupedTags.contains(tag))
                                        .forEach(ungroupedTags::add);
                            }
                        })
                );
            }

            if (!ungroupedTags.isEmpty()) {
                tagGroups.add(Map.of(
                        "name", "Other",
                        "tags", List.copyOf(ungroupedTags)
                ));
            }

            openApi.addExtension("x-tagGroups", tagGroups);
        };
    }
}
