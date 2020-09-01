package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolume;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolumeType;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    private static final Boolean PERSISTENCE_ENABLED_DEFAULT_VALUE = true;

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

    static Map<String, String> ingressVariablesMap(Boolean enabled,
                                                   Set<ServiceAccessMethod> externalAccessMethods,
                                                   String ingressClass,
                                                   String publicIngressClass,
                                                   Boolean tlsEnabled,
                                                   String ingressCertOrIssuer,
                                                   Boolean acme) {
        Map<String, String> variables = new HashMap<>();
        externalAccessMethods.forEach(m -> {
                if (enabled) {
                    validateIngressClass(ingressClass);
                    if (StringUtils.isNotEmpty(m.getDeployParameters().get(INGRESS_ENABLED))) {
                        variables.put(m.getDeployParameters().get(INGRESS_ENABLED), String.valueOf(true));
                    }
                    if (StringUtils.isNotEmpty(m.getDeployParameters().get(INGRESS_HOSTS))) {
                        if (m.getDeployParameters().get(INGRESS_HOSTS).contains(HelmInstallCommand.TEXT_TO_REPLACE_WITH_VALUE)) {
                            // added in order to support different passing of ingress hostname
                            variables.put(m.getDeployParameters().get(INGRESS_HOSTS), m.getUrl());
                        } else {
                            // left to support standard case with a list of ingress hosts
                            variables.put(m.getDeployParameters().get(INGRESS_HOSTS), PAR_OPEN + m.getUrl() + PAR_CLOSE);
                        }
                    }
                    if (StringUtils.isNotEmpty(m.getDeployParameters().get(INGRESS_CLASS))) {
                        variables.put(
                                m.getDeployParameters().get(INGRESS_CLASS),
                                m.isOfType(ServiceAccessMethodType.PUBLIC) ? publicIngressClass : ingressClass
                        );
                    }
                    if (StringUtils.isNotEmpty(m.getDeployParameters().get(INGRESS_TLS_ENABLED))) {
                        variables.put(m.getDeployParameters().get(INGRESS_TLS_ENABLED), String.valueOf(tlsEnabled));
                    }
                    if (tlsEnabled) {
                        if (StringUtils.isNotEmpty(m.getDeployParameters().get(INGRESS_TLS_HOSTS))) {
                            if (m.getDeployParameters().get(INGRESS_TLS_HOSTS).contains(HelmInstallCommand.TEXT_TO_REPLACE_WITH_VALUE)) {
                                // added in order to support different passing of ingress hostname
                                variables.put(m.getDeployParameters().get(INGRESS_TLS_HOSTS), m.getUrl());
                            } else {
                                // left to support standard case with a list of ingress hosts
                                variables.put(m.getDeployParameters().get(INGRESS_TLS_HOSTS), PAR_OPEN + m.getUrl() + PAR_CLOSE);
                            }
                        }
                        if (StringUtils.isNotEmpty(m.getDeployParameters().get(INGRESS_LETSENCRYPT))) {
                            variables.put(m.getDeployParameters().get(INGRESS_LETSENCRYPT), String.valueOf(acme));
                        }
                        if (StringUtils.isNotEmpty(m.getDeployParameters().get(INGRESS_WILDCARD_OR_ISSUER))) {
                            variables.put(m.getDeployParameters().get(INGRESS_WILDCARD_OR_ISSUER), ingressCertOrIssuer);
                        }
                    }
                } else {
                    if (StringUtils.isNotEmpty(m.getDeployParameters().get(INGRESS_ENABLED))) {
                        variables.put(m.getDeployParameters().get(INGRESS_ENABLED), String.valueOf(false));
                    }
                }
            }
        );
        return variables;
    }

    private static void validateIngressClass(String ingressClass){
        if(StringUtils.isEmpty(ingressClass)){
            throw new IllegalArgumentException("Ingress class is empty");
        }
    }

}
