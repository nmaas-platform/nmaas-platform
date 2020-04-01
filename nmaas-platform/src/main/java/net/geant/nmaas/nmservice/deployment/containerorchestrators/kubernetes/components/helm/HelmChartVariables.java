package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmChartIngressVariable.INGRESS_CLASS;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmChartIngressVariable.INGRESS_HOSTS;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmChartIngressVariable.INGRESS_LETSENCRYPT;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmChartIngressVariable.INGRESS_TLS_ENABLED;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmChartIngressVariable.INGRESS_WILDCARD_OR_ISSUER;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class HelmChartVariables {

    private static final String PAR_OPEN = "{";
    private static final String PAR_CLOSE = "}";

    static Map<String, String> ingressVariablesMap(Set<ServiceAccessMethod> externalAccessMethods, String ingressClass, Boolean tlsEnabled, String ingressCertOrIssuer, Boolean acme) {
        validateIngressClass(ingressClass);
        Map<String, String> variables = new HashMap<>();
        externalAccessMethods.forEach(m -> {
                variables.put(m.getDeployParameters().get(INGRESS_HOSTS), PAR_OPEN + m.getUrl() + PAR_CLOSE);
                variables.put(m.getDeployParameters().get(INGRESS_CLASS), ingressClass);
                variables.put(m.getDeployParameters().get(INGRESS_TLS_ENABLED), String.valueOf(tlsEnabled));
                if (tlsEnabled) {
                    variables.put(m.getDeployParameters().get(INGRESS_LETSENCRYPT), String.valueOf(acme));
                    variables.put(m.getDeployParameters().get(INGRESS_WILDCARD_OR_ISSUER), ingressCertOrIssuer);
                }
        });
        return variables;
    }

    private static void validateIngressClass(String ingressClass){
        if(StringUtils.isEmpty(ingressClass)){
            throw new IllegalArgumentException("Ingress class is empty");
        }
    }

}
