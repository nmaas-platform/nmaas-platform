package net.geant.nmaas.portal.api.domain;

public enum RejectionReason {
    GLOBAL_INSTANCES_LIMIT_REACHED("Global instances limit reached"),
    GLOBAL_CONTAINERS_LIMIT_REACHED("Global containers limit reached"),
    DOMAIN_INSTANCES_LIMIT_REACHED("Domain instances limit reached"),
    DOMAIN_CONTAINERS_LIMIT_REACHED("Domain containers limit reached"),
    GLOBAL_CPU_LIMIT_REACHED("Global cpu limit reached"),
    DOMAIN_CPU_LIMIT_REACHED("Domain cpu limit reached"),
    GLOBAL_MEMORY_LIMIT_REACHED("Global memory limit reached"),
    DOMAIN_MEMORY_LIMIT_REACHED("Domain memory limit reached");


    private final String description;

    RejectionReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
