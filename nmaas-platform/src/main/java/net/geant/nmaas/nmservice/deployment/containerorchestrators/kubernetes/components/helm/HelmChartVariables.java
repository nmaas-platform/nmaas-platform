package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

class HelmChartVariables {

    private static final String INGRESS_HOSTS_KEY = "ingress.hosts";
    private static final String INGRESS_TLS_KEY = "ingress.tls.enabled";

    private static final String INGRESS_CLASS_KEY = "ingress.class";
    private static final String INGRESS_LETSENCRYPT_KEY = "ingress.tls.acme";
    private static final String INGRESS_WILDCARD_OR_ISSUER = "ingress.tls.certOrIssuer";

    private static final String PAR_OPEN = "{";
    private static final String PAR_CLOSE = "}";

    static Map<String, String> ingressVariablesMap(String ingressHost, String ingressClass, Boolean tls) {
        validateIngressClass(ingressClass);
        Map<String, String> variables = new HashMap<>();
        variables.put(INGRESS_HOSTS_KEY, PAR_OPEN + ingressHost + PAR_CLOSE);
        variables.put(INGRESS_TLS_KEY, String.valueOf(tls));
        variables.put(INGRESS_CLASS_KEY, ingressClass);
        return variables;
    }

    static Map<String, String> ingressVariablesAddTls(String ingressCertOrIssuer, Boolean acme) {
        Map<String, String> variables = new HashMap<>();
        variables.put(INGRESS_LETSENCRYPT_KEY, String.valueOf(acme));
        variables.put(INGRESS_WILDCARD_OR_ISSUER, ingressCertOrIssuer);
        return variables;
    }

    private static void validateIngressClass(String ingressClass){
        if(StringUtils.isEmpty(ingressClass)){
            throw new IllegalArgumentException("Ingress class is empty");
        }
    }

}
