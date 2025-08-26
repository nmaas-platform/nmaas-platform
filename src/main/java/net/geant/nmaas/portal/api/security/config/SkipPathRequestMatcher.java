package net.geant.nmaas.portal.api.security.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.AntPathMatcher;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * Custom {@link RequestMatcher} that determines whether a given HTTP request
 * should be excluded from authentication based on predefined path patterns per method.
 *
 * If a path matches any of the method-specific or "ANY" patterns, the matcher returns false,
 * meaning the request should be skipped (not authenticated).
 */
@Slf4j
public class SkipPathRequestMatcher implements RequestMatcher {

    private final Map<String, List<String>> pathMap;
    private final AntPathMatcher matcher = new AntPathMatcher();

    /**
     * Constructs a matcher with a map of HTTP method names to a list of Ant-style path patterns
     * that should be skipped by authentication filters.
     *
     * @param skippedPathsByMethod map of HTTP methods to skip patterns
     */
    public SkipPathRequestMatcher(Map<String, List<String>> skippedPathsByMethod) {
        this.pathMap = skippedPathsByMethod;
    }

    /**
     * Determines whether the given request should be processed by the security filter.
     *
     * @param request the incoming HTTP request
     * @return true if the request does not match any skip patterns and should be authenticated;
     *         false if the request should be excluded from filtering
     */
    @Override
    public boolean matches(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        List<String> methodPaths = pathMap.getOrDefault(method, Collections.emptyList());
        List<String> anyPaths = pathMap.getOrDefault("ANY", Collections.emptyList());
        return !(matchesAny(methodPaths, path) || matchesAny(anyPaths, path));
    }

    /**
     * Checks if the request path matches any of the provided Ant-style patterns.
     *
     * @param patterns list of patterns to match
     * @param path     request URI path
     * @return true if any pattern matches the path
     */
    private boolean matchesAny(List<String> patterns, String path) {
        return patterns.stream().anyMatch(p -> matcher.match(p, path));
    }
}
