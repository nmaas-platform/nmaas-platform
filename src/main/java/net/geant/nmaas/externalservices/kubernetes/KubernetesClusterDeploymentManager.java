package net.geant.nmaas.externalservices.kubernetes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.externalservices.kubernetes.model.KClusterView;
import net.geant.nmaas.externalservices.kubernetes.model.NamespaceConfigOption;
import net.geant.nmaas.orchestration.entities.DomainTechDetails;
import net.geant.nmaas.orchestration.repositories.DomainTechDetailsRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

@Component
@NoArgsConstructor
@Getter
@Setter
public class KubernetesClusterDeploymentManager implements KubernetesClusterNamespaceService {

    private static final String NMAAS_NAMESPACE_PREFIX = "nmaas-ns-";

    @Value("${kubernetes.deployment.namespaceConfigOption}")
    private NamespaceConfigOption namespaceConfigOption;

    @Value("${kubernetes.deployment.defaultNamespace}")
    private String defaultNamespace;

    @Value("${kubernetes.deployment.defaultStorageClass}")
    private String defaultStorageClass;

    @Value("${kubernetes.deployment.smtpServerHostname}")
    private String smtpServerHostname;

    @Value("${kubernetes.deployment.smtpServerPort}")
    private Integer smtpServerPort;

    @Value("${kubernetes.deployment.smtpServerUsername:}")
    private String smtpServerUsername;

    @Value("${kubernetes.deployment.smtpServerPassword:}")
    private String smtpServerPassword;

    @Value("${kubernetes.deployment.forceDedicatedWorkers:false}")
    private Boolean forceDedicatedWorkers;

    private DomainTechDetailsRepository domainTechDetailsRepository;

    @Autowired
    public KubernetesClusterDeploymentManager(DomainTechDetailsRepository domainTechDetailsRepository) {
        this.domainTechDetailsRepository = domainTechDetailsRepository;
    }

    public Optional<String> getStorageClass(String domain) {
        Optional <DomainTechDetails> foundDomain = domainTechDetailsRepository.findByDomainCodename(domain);
        if (foundDomain.isPresent() && StringUtils.isNotEmpty(foundDomain.get().getKubernetesStorageClass())) {
            return Optional.of(foundDomain.get().getKubernetesStorageClass());
        }
        if (this.getDefaultStorageClass() != null && !this.getDefaultStorageClass().isEmpty()) {
            return Optional.of(this.getDefaultStorageClass());
        }
        return Optional.empty();
    }

    public String namespace(String domain) {
        switch (this.getNamespaceConfigOption()) {
            case CREATE_NAMESPACE:
                //dynamic creation of namespace will be added
                return NMAAS_NAMESPACE_PREFIX + domain;
            case USE_DEFAULT_NAMESPACE:
                return this.getDefaultNamespace();
            case USE_DOMAIN_NAMESPACE:
                Optional<DomainTechDetails> foundDomain = this.domainTechDetailsRepository.findByDomainCodename(domain);
                if (foundDomain.isPresent()) {
                    return foundDomain.get().getKubernetesNamespace();
                }
                return NMAAS_NAMESPACE_PREFIX + domain;
            default:
                return NMAAS_NAMESPACE_PREFIX + domain;
        }
    }

    public String getSMTPServerHostname() {
        return smtpServerHostname;
    }

    public Integer getSMTPServerPort() {
        return smtpServerPort;
    }

    public Optional<String> getSMTPServerUsername() {
        return Optional.ofNullable(smtpServerUsername);
    }

    public Optional<String> getSMTPServerPassword() {
        return Optional.ofNullable(smtpServerPassword);
    }

    KClusterView.KClusterDeploymentView getKClusterDeploymentView() {
        KClusterView.KClusterDeploymentView kClusterDeploymentView = new KClusterView.KClusterDeploymentView();
        kClusterDeploymentView.setNamespaceConfigOption(this.namespaceConfigOption);
        kClusterDeploymentView.setDefaultNamespace(this.defaultNamespace);
        kClusterDeploymentView.setDefaultStorageClass(this.defaultStorageClass);
        kClusterDeploymentView.setSmtpServerHostname(this.smtpServerHostname);
        kClusterDeploymentView.setSmtpServerPort(this.smtpServerPort);
        kClusterDeploymentView.setSmtpServerUsername(this.smtpServerUsername);
        kClusterDeploymentView.setSmtpServerPassword(this.smtpServerPassword);
        kClusterDeploymentView.setForceDedicatedWorkers(this.forceDedicatedWorkers);
        return kClusterDeploymentView;
    }

    @PostConstruct
    public void validateConfig() {
        checkArgument(this.getNamespaceConfigOption() != null, "NamespaceConfigOption property can't be null");
        this.getNamespaceConfigOption().validate(this.getKClusterDeploymentView());
    }

}
