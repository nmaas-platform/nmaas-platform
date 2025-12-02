package net.geant.nmaas.kubernetes;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.kubernetes.api.model.KClusterView;
import net.geant.nmaas.kubernetes.remote.entities.NamespaceConfigOption;
import net.geant.nmaas.orchestration.entities.DomainTechDetails;
import net.geant.nmaas.orchestration.repositories.DomainTechDetailsRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

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

    @Value("${kubernetes.deployment.smtpFromDefaultDomain}")
    private String smtpFromDefaultDomain;

    @Value("${kubernetes.deployment.forceDedicatedWorkers:false}")
    private Boolean forceDedicatedWorkers;

    private DomainTechDetailsRepository domainTechDetailsRepository;

    @Autowired
    public KubernetesClusterDeploymentManager(DomainTechDetailsRepository domainTechDetailsRepository) {
        this.domainTechDetailsRepository = domainTechDetailsRepository;
    }

    public Optional<String> getStorageClass(String domain) {
        Optional<DomainTechDetails> foundDomain = domainTechDetailsRepository.findByDomainCodename(domain);
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

    public String getSMTPFromDefaultDomain() {
        return smtpFromDefaultDomain;
    }

    public KClusterView.KClusterDeploymentView getKClusterDeploymentView() {
        KClusterView.KClusterDeploymentView view = new KClusterView.KClusterDeploymentView();
        view.setNamespaceConfigOption(this.namespaceConfigOption);
        view.setDefaultNamespace(this.defaultNamespace);
        view.setDefaultStorageClass(this.defaultStorageClass);
        view.setSmtpServerHostname(this.smtpServerHostname);
        view.setSmtpServerPort(this.smtpServerPort);
        view.setSmtpServerUsername(this.smtpServerUsername);
        view.setSmtpServerPassword(this.smtpServerPassword);
        view.setSmtpFromDefaultDomain(this.smtpFromDefaultDomain);
        view.setForceDedicatedWorkers(this.forceDedicatedWorkers);
        return view;
    }

    @PostConstruct
    public void validateConfig() {
        Validate.isTrue(this.getNamespaceConfigOption() != null, "NamespaceConfigOption property can't be null");
        this.getNamespaceConfigOption().validate(this.getKClusterDeploymentView());
    }

}
