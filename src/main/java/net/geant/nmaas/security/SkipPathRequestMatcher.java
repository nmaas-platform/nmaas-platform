package net.geant.nmaas.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.AntPathMatcher;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class SkipPathRequestMatcher implements RequestMatcher {

    private final Map<String, List<String>> pathMap;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public SkipPathRequestMatcher(Map<String, List<String>> skippedPathsByMethod) {
        this.pathMap = skippedPathsByMethod;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        List<String> methodPaths = pathMap.getOrDefault(method, Collections.emptyList());
        List<String> anyPaths = pathMap.getOrDefault("ANY", Collections.emptyList());
        //TODO look at this on comback to brach
        return !matchesAny(methodPaths, path) || matchesAny(anyPaths, path);
    }

    private boolean matchesAny(List<String> patterns, String path) {
        return patterns.stream().anyMatch(p -> matcher.match(p, path));
    }
}
