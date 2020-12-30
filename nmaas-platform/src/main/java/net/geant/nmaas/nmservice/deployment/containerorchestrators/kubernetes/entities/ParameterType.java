package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities;

import java.util.Arrays;

public enum ParameterType {
    SMTP_HOSTNAME,
    SMTP_PORT,
    SMTP_USERNAME,
    SMTP_PASSWORD,
    DOMAIN_CODENAME,
    BASE_URL,
    RELEASE_NAME,
    APP_INSTANCE_NAME;

    public static ParameterType fromValue(String value) {
        return Arrays.stream(ParameterType.values())
                .filter(p -> value.contains(p.toString()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

}
