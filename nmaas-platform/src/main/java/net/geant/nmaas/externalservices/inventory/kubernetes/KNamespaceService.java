package net.geant.nmaas.externalservices.inventory.kubernetes;

public interface KNamespaceService {

    String NMAAS_NAMESPACE_PREFIX = "nmaas-ns-";

    String namespace(String domain);

}
