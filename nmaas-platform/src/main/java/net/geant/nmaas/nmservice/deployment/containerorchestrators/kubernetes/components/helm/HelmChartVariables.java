package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class HelmChartVariables {

    private static final String INGRESS_ENABLED_KEY = "ingress.enabled";
    private static final String INGRESS_HOSTS_KEY = "ingress.hosts";
    private static final String INGRESS_ANNOTATIONS_KEY = "ingress.annotations";
    private static final String INGRESS_TLS_KEY = "ingress.tls";

    private static final String INGRESS_CLASS_ANNOTATION_KEY = "kubernetes.io/ingress.class";

    private static final String IS = "=";
    private static final String COMMA = ",";
    private static final String PAR_OPEN = "{";
    private static final String PAR_CLOSE = "}";

    public static Map<String, String> ingressVariablesMap(Boolean enabled, String ingressHost, String ingressClass, Boolean tls) {
        Map<String, String> variables = new HashMap<>();
        variables.put(INGRESS_ENABLED_KEY, String.valueOf(enabled));
        variables.put(INGRESS_HOSTS_KEY, PAR_OPEN + ingressHost+ PAR_CLOSE);
        variables.put(INGRESS_ANNOTATIONS_KEY, PAR_OPEN + getIngressAnnotationsValue(ingressClass) + PAR_CLOSE);
        variables.put(INGRESS_TLS_KEY, String.valueOf(tls));
        return variables;
    }

    private static String getIngressAnnotationsValue(String ingressClass) {
        return INGRESS_CLASS_ANNOTATION_KEY + ": " + ingressClass;
    }

}
