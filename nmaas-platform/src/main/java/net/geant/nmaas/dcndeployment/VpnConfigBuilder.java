package net.geant.nmaas.dcndeployment;

import net.geant.nmaas.dcndeployment.exceptions.ConfigNotValidException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Singleton;

@Component
@Singleton
public class VpnConfigBuilder {

    private static final String DEFAULT_NMAAS_CUSTOMER_VRF_ID = "NMAAS-C-AS65537";
    private static final String DEFAULT_NMAAS_CUSTOMER_LOGICAL_INTERFACE = "ge-0/0/3.7";
    private static final String DEFAULT_NMAAS_CUSTOMER_VRF_RD = "172.16.4.4:7";
    private static final String DEFAULT_NMAAS_CUSTOMER_VRF_RT = "65525L:7";
    private static final String DEFAULT_NMAAS_CUSTOMER_BGP_GROUP_ID = "INET-VPN-NMAAS-C-65537";
    private static final String DEFAULT_NMAAS_CUSTOMER_BGP_NEIGHBOR_IP = "192.168.47.7";
    private static final String DEFAULT_NMAAS_CUSTOMER_ASN = "65537";
    private static final String DEFAULT_NMAAS_CUSTOMER_PHYSICAL_INTERFACE = "ge-0/0/3";
    private static final String DEFAULT_NMAAS_CUSTOMER_ID = "7";
    private static final String DEFAULT_NMAAS_CUSTOMER_BGP_LOCAL_IP = "192.168.47.4";
    private static final String DEFAULT_NMAAS_CUSTOMER_BGP_LOCAL_CIDR = "24";

    private static final String VALUE_TAG_NMAAS_CUSTOMER_VRF_ID = "<VALUE_VRF_ID>";
    private static final String VALUE_TAG_NMAAS_CUSTOMER_LOGICAL_INTERFACE = "<VALUE_LOGICAL_INTERFACE>";
    private static final String VALUE_TAG_NMAAS_CUSTOMER_VRF_RD = "<VALUE_VRF_RD>";
    private static final String VALUE_TAG_NMAAS_CUSTOMER_VRF_RT = "<VALUE_VRF_RT>";
    private static final String VALUE_TAG_NMAAS_CUSTOMER_BGP_GROUP_ID = "<VALUE_BGP_GROUP_ID>";
    private static final String VALUE_TAG_NMAAS_CUSTOMER_BGP_NEIGHBOR_IP = "<VALUE_BGP_NEIGHBOR_IP>";
    private static final String VALUE_TAG_NMAAS_CUSTOMER_ASN = "<VALUE_ASN>";
    private static final String VALUE_TAG_NMAAS_CUSTOMER_PHYSICAL_INTERFACE = "<VALUE_PHYSICAL_INTERFACE>";
    private static final String VALUE_TAG_NMAAS_CUSTOMER_ID = "<VALUE_ID>";
    private static final String VALUE_TAG_NMAAS_CUSTOMER_BGP_LOCAL_IP = "<VALUE_BGP_LOCAL_IP>";
    private static final String VALUE_TAG_NMAAS_CUSTOMER_BGP_LOCAL_CIDR = "<VALUE_BGP_LOCAL_CIDR>";

    @Value("${ansible.var.file.prefix}")
    String varFilePrefix;

    @Value("${ansible.var.file.suffix}")
    String varFileSuffix;

    @Value("${ansible.var.file.directory}")
    String varFileDirectory;

    public void buildAnsibleVarFile(String uniqueName, VpnConfig config) throws ConfigNotValidException {
        config.validate();
        //TODO
    }

}
