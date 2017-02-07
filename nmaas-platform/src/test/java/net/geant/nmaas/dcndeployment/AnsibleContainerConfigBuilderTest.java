package net.geant.nmaas.dcndeployment;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class AnsibleContainerConfigBuilderTest {

    private static final String SERVICE_ID = "3vnhgwcn95ngcj5eogx";

    private static final String EXAMPLE_COMPLETE_DOCKER_RUN_COMMAND =
            "docker run " +
                    "--name=nmaas-ansible " +
                    "--volume=/home/docker/ansible-docker/ansible.cfg:/etc/ansible/ansible.cfg " +
                    "--volume=/home/docker/ansible-docker/pb-nmaas-vpn-client-test-config.yml:/ansible/pb-nmaas-vpn-client-test-config.yml " +
                    "--volume=/home/docker/ansible-docker/working-dir/config-set:/ansible-config-set " +
                    "--volume=/home/docker/ansible-docker/:/ansible-playbook-dir " +
                    "--volume=/home/docker/.ssh/id_rsa:/root/.ssh/id_rsa " +
                    "a4aea6924d2d " +
                    "ansible-playbook " +
                        "-v -i /ansible-playbook-dir/hosts " +
                        "-v /ansible-playbook-dir/pb-nmaas-vpn-client-test-config.yml " +
                        "--limit=R4 " +
                        "--extra-vars=" +
                            "\"NMAAS_CUSTOMER_VRF_ID=NMAAS-C-AS65538 " +
                            "NMAAS_CUSTOMER_LOGICAL_INTERFACE=ge-0/0/3.8 " +
                            "NMAAS_CUSTOMER_VRF_RD=182.16.4.4:8 " +
                            "NMAAS_CUSTOMER_VRF_RT=65525L:8 " +
                            "NMAAS_CUSTOMER_BGP_GROUP_ID=INET-VPN-NMAAS-C-65538 " +
                            "NMAAS_CUSTOMER_BGP_NEIGHBOR_IP=192.168.48.8 " +
                            "NMAAS_CUSTOMER_ASN=65538 " +
                            "NMAAS_CUSTOMER_PHYSICAL_INTERFACE=ge-0/0/3 " +
                            "NMAAS_CUSTOMER_ID=8 " +
                            "NMAAS_CUSTOMER_BGP_LOCAL_IP=192.168.48.4 " +
                            "NMAAS_CUSTOMER_BGP_LOCAL_CIDR=24\"" +
                            "NMAAS_CUSTOMER_SERVICE_ID=" + SERVICE_ID + "\"";

}
