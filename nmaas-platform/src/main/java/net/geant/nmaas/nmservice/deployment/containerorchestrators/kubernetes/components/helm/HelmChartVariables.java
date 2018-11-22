package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import java.util.HashMap;
import java.util.Map;

class HelmChartVariables {

    private static final String INGRESS_HOSTS_KEY = "ingress.hosts";
    private static final String INGRESS_ANNOTATIONS_KEY = "ingress.annotations";
    private static final String INGRESS_TLS_KEY = "ingress.tls.enabled";

    private static final String INGRESS_CLASS_ANNOTATION_KEY = "kubernetes.io/ingress.class";
    private static final String INGRESS_LETSENCRYPT_KEY = "ingress.tls.acme";
    private static final String INGRESS_WILDCARD_OR_ISSUER = "ingress.tls.certOrIssuer";

    private static final String PAR_OPEN = "{";
    private static final String PAR_CLOSE = "}";
    private static final String QUOTE = "\"";

    static Map<String, String> ingressVariablesMap(String ingressHost, String ingressClass, Boolean tls) {
        Map<String, String> variables = new HashMap<>();
        variables.put(INGRESS_HOSTS_KEY, PAR_OPEN + ingressHost + PAR_CLOSE);
        variables.put(INGRESS_ANNOTATIONS_KEY, PAR_OPEN + getIngressAnnotationsValue(ingressClass) + PAR_CLOSE);
        variables.put(INGRESS_TLS_KEY, String.valueOf(tls));
        return variables;
    }

    static Map<String, String> ingressVariablesAddTls(String ingressCertOrIssuer, Boolean acme) {
        Map<String, String> variables = new HashMap<>();
        variables.put(INGRESS_LETSENCRYPT_KEY, String.valueOf(acme));
        variables.put(INGRESS_WILDCARD_OR_ISSUER, PAR_OPEN + ingressCertOrIssuer + PAR_CLOSE);
        return variables;
    }

    private static String getIngressAnnotationsValue(String ingressClass) {
        return QUOTE + INGRESS_CLASS_ANNOTATION_KEY + ": " + ingressClass + QUOTE;
    }

}
