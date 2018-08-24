package net.geant.nmaas.portal.api.security;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.impl.crypto.MacProvider;

@Component
@Getter
public class SSOSettings {
    @Value("${sso.key}")
    private String key;

    @Value("${sso.timeout}")
    private int timeout;
}