package net.geant.nmaas.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.AntPathMatcher;

import java.util.Arrays;
import java.util.List;

public class SkipPathRequestMatcher implements RequestMatcher {

    private final List<String> patterns;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public SkipPathRequestMatcher(String[] skippedPaths) {
        this.patterns = Arrays.asList(skippedPaths);
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        String path = request.getRequestURI();
        return patterns.stream().anyMatch(p -> matcher.match(p, path));
    }
}
