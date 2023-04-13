package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands.HelmInstallCommand;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolume;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolumeType;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmChartIngressVariable.INGRESS_CLASS;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmChartIngressVariable.INGRESS_ENABLED;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmChartIngressVariable.INGRESS_HOSTS;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmChartIngressVariable.INGRESS_LETSENCRYPT;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmChartIngressVariable.INGRESS_TLS_ENABLED;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmChartIngressVariable.INGRESS_TLS_HOSTS;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmChartIngressVariable.INGRESS_WILDCARD_OR_ISSUER;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmChartPersistenceVariable.PERSISTENCE_ENABLED;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmChartPersistenceVariable.PERSISTENCE_NAME;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmChartPersistenceVariable.PERSISTENCE_STORAGE_CLASS;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmChartPersistenceVariable.PERSISTENCE_STORAGE_SPACE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class HelmChartVariables {

    private static final boolean PERSISTENCE_ENABLED_DEFAULT_VALUE = true;

    private static final String PAR_OPEN = "{";
    private static final String PAR_CLOSE = "}";

    static Map<String, String> persistenceVariablesMap(Set<ServiceStorageVolume> storageVolumes,
                                                       Optional<String> storageClass,
                                                       String defaultStorageName) {
        Map<String, String> variables = new HashMap<>();
        storageVolumes.forEach(v -> {
                if (PERSISTENCE_ENABLED_DEFAULT_VALUE) {
                    if (StringUtils.isNotEmpty(v.getDeployParameters().get(PERSISTENCE_ENABLED))) {
                        variables.put(v.getDeployParameters().get(PERSISTENCE_ENABLED), String.valueOf(true));
                    }
                    if (StringUtils.isNotEmpty(v.getDeployParameters().get(PERSISTENCE_NAME))) {
                        if (Arrays.asList(ServiceStorageVolumeType.MAIN, ServiceStorageVolumeType.SHARED).contains(v.getType())) {
                            variables.put(v.getDeployParameters().get(PERSISTENCE_NAME), defaultStorageName);
                        }
                    }
                    if (StringUtils.isNotEmpty(v.getDeployParameters().get(PERSISTENCE_STORAGE_CLASS))) {
                        storageClass.ifPresent(s -> variables.put(v.getDeployParameters().get(PERSISTENCE_STORAGE_CLASS), s));
                    }
                    if (StringUtils.isNotEmpty(v.getDeployParameters().get(PERSISTENCE_STORAGE_SPACE))) {
                        variables.put(v.getDeployParameters().get(PERSISTENCE_STORAGE_SPACE), getStorageSpaceString(v.getSize()));
                    }
                } else {
                    if (StringUtils.isNotEmpty(v.getDeployParameters().get(PERSISTENCE_ENABLED))) {
                        variables.put(v.getDeployParameters().get(PERSISTENCE_ENABLED), String.valueOf(false));
                    }
                }
            }
        );
        return variables;
    }

    private static String getStorageSpaceString(Integer storageSpace){
        return storageSpace.toString() + "Gi";
    }

    static Map<String, String> ingressVariablesMap(boolean enabled,
                                                   Set<ServiceAccessMethod> externalAccessMethods,
                                                   String ingressClass,
                                                   String publicIngressClass,
                                                   boolean tlsEnabled,
                                                   String ingressCertOrIssuer,
                                                   boolean acme) {
        Map<String, String> variables = new HashMap<>();
        externalAccessMethods.forEach(m -> {
                if (enabled) {
                    validateIngressClass(ingressClass);
                    if (StringUtils.isNotEmpty(m.getDeployParameters().get(INGRESS_ENABLED))) {
                        getStreamFromValue(m, INGRESS_ENABLED)
                                .forEach(k -> variables.put(k, String.valueOf(true)));
                    }
                    if (StringUtils.isNotEmpty(m.getDeployParameters().get(INGRESS_HOSTS))) {
                        getStreamFromValue(m, INGRESS_HOSTS)
                                .forEach(k -> {
                                    if (k.contains(HelmInstallCommand.TEXT_TO_REPLACE_WITH_VALUE)) {
                                        // added in order to support different passing of ingress hostname
                                        variables.put(k, m.getUrl());
                                    } else {
                                        // left to support standard case with a list of ingress hosts
                                        variables.put(k, PAR_OPEN + m.getUrl() + PAR_CLOSE);
                                    }
                                });
                    }
                    if (StringUtils.isNotEmpty(m.getDeployParameters().get(INGRESS_CLASS))) {
                        getStreamFromValue(m, INGRESS_CLASS)
                                .forEach(k -> variables.put(k, m.isOfType(ServiceAccessMethodType.PUBLIC) ? publicIngressClass : ingressClass));
                    }
                    if (StringUtils.isNotEmpty(m.getDeployParameters().get(INGRESS_TLS_ENABLED))) {
                        getStreamFromValue(m, INGRESS_TLS_ENABLED)
                                .forEach(k -> variables.put(k, String.valueOf(tlsEnabled)));
                    }
                    if (tlsEnabled) {
                        if (StringUtils.isNotEmpty(m.getDeployParameters().get(INGRESS_TLS_HOSTS))) {
                            getStreamFromValue(m, INGRESS_TLS_HOSTS)
                                    .forEach(k -> {
                                        if (k.contains(HelmInstallCommand.TEXT_TO_REPLACE_WITH_VALUE)) {
                                            // added in order to support different passing of ingress hostname
                                            variables.put(k, m.getUrl());
                                        } else {
                                            // left to support standard case with a list of ingress hosts
                                            variables.put(k, PAR_OPEN + m.getUrl() + PAR_CLOSE);
                                        }
                                    });
                        }
                        if (StringUtils.isNotEmpty(m.getDeployParameters().get(INGRESS_LETSENCRYPT))) {
                            getStreamFromValue(m, INGRESS_LETSENCRYPT)
                                    .forEach(k -> variables.put(k, String.valueOf(acme)));
                        }
                        if (StringUtils.isNotEmpty(m.getDeployParameters().get(INGRESS_WILDCARD_OR_ISSUER))) {
                            getStreamFromValue(m, INGRESS_WILDCARD_OR_ISSUER)
                                    .forEach(k -> variables.put(k, ingressCertOrIssuer));
                        }
                    }
                } else {
                    if (StringUtils.isNotEmpty(m.getDeployParameters().get(INGRESS_ENABLED))) {
                        getStreamFromValue(m, INGRESS_ENABLED)
                                .forEach(k -> variables.put(k, String.valueOf(false)));
                    }
                }
            }
        );
        return variables;
    }

    @NotNull
    private static Stream<String> getStreamFromValue(ServiceAccessMethod m, HelmChartIngressVariable variable) {
        return Arrays.stream(m.getDeployParameters().get(variable).split(","));
    }

    private static void validateIngressClass(String ingressClass){
        if (StringUtils.isEmpty(ingressClass)) {
            throw new IllegalArgumentException("Ingress class is empty");
        }
    }

}
