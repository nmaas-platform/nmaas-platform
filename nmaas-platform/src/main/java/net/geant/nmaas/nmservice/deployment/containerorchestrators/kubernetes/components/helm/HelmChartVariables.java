package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import java.util.HashMap;
import java.util.Map;

class HelmChartVariables {

    private static final String INGRESS_HOSTS_KEY = "ingress.hosts";
    private static final String INGRESS_ANNOTATIONS_KEY = "ingress.annotations";
    private static final String INGRESS_TLS_KEY = "ingress.tls";

    private static final String INGRESS_CLASS_ANNOTATION_KEY = "kubernetes.io/ingress.class";

    private static final String PAR_OPEN = "{";
    private static final String PAR_CLOSE = "}";
    private static final String QUOTE = "\"";

    static Map<String, String> ingressVariablesMap(String ingressHost, String ingressClass, Boolean tls) {
        Map<String, String> variables = new HashMap<>();
        variables.put(INGRESS_HOSTS_KEY, PAR_OPEN + ingressHost + PAR_CLOSE);
        //TODO: Uncomment this and pass basic auth as custom deployment setting
        //variables.put(INGRESS_ANNOTATIONS_KEY, PAR_OPEN + getIngressAnnotationsValue(ingressClass) + PAR_CLOSE);
        variables.put(INGRESS_TLS_KEY, String.valueOf(tls));
        return variables;
    }

    private static String getIngressAnnotationsValue(String ingressClass) {
        return QUOTE + INGRESS_CLASS_ANNOTATION_KEY + ": " + ingressClass + QUOTE;
    }

}
