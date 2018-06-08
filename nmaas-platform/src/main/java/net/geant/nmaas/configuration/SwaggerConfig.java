package net.geant.nmaas.configuration;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.service.GrantType;
import springfox.documentation.service.TokenEndpoint;
import springfox.documentation.service.TokenRequestEndpoint;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.OAuthBuilder;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.builders.AuthorizationCodeGrantBuilder;


@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo())
                .securitySchemes(Arrays.asList(securityScheme()))
                .securityContexts(Arrays.asList(securityContext()));
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("NMAAS API")
                .description("NMAAS API documentation")
                .termsOfServiceUrl("none")
                .contact("psnc.pl")
                .license("Prioprietary")
                .licenseUrl("none")
                .version("1.0.0-pre")
                .build();
    }

    @Bean
    public SecurityConfiguration security() {
        return SecurityConfigurationBuilder.builder()
                .clientId("admin")
                .clientSecret("admin")
                .scopeSeparator(" ")
                .useBasicAuthenticationWithAccessCodeGrant(true)
                .build();
    }

    private SecurityScheme securityScheme() {
        GrantType grantType = new AuthorizationCodeGrantBuilder()
                .tokenEndpoint(new TokenEndpoint("/portal/api/auth/basic/token", "token"))
                .tokenRequestEndpoint(
                        new TokenRequestEndpoint("/portal/api/auth/basic/login", "admin", "admin"))
                .build();

        SecurityScheme oauth = new OAuthBuilder().name("BasicLogin")
                .grantTypes(Arrays.asList(grantType))
                .scopes(Arrays.asList(scopes()))
                .build();
        return oauth;
    }

    private AuthorizationScope[] scopes() {
        AuthorizationScope[] scopes = {
                new AuthorizationScope("user", "for basic usage"),
                new AuthorizationScope("admin", "for administrative tasks"),
                new AuthorizationScope("superadmin", "for configuration tasks") };
        return scopes;
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(Arrays.asList(new SecurityReference("BasicLogin", scopes())))
                .forPaths(PathSelectors.regex("/*"))
                .build();
    }
}