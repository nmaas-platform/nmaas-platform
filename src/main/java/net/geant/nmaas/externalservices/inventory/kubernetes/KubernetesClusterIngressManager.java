package net.geant.nmaas.externalservices.inventory.kubernetes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.externalservices.inventory.kubernetes.model.IngressCertificateConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.model.IngressControllerConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.model.IngressResourceConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.model.KClusterView;
import net.geant.nmaas.orchestration.repositories.DomainTechDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.google.common.base.Preconditions.checkArgument;

@Component
@NoArgsConstructor
@Getter
@Setter
public class KubernetesClusterIngressManager {

    @Value("${kubernetes.ingress.controllerConfigOption}")
    private IngressControllerConfigOption controllerConfigOption;

    @Value("${kubernetes.ingress.supportedIngressClass}")
    private String supportedIngressClass;

    @Value("${kubernetes.ingress.publicIngressClass}")
    private String publicIngressClass;

    @Value("${kubernetes.ingress.controllerChartName}")
    private String controllerChartName;

    @Value("${kubernetes.ingress.controllerChartArchive}")
    private String controllerChartArchive;

    @Value("${kubernetes.ingress.resourceConfigOption}")
    private IngressResourceConfigOption resourceConfigOption;

    @Value("${kubernetes.ingress.externalServiceDomain}")
    private String externalServiceDomain;

    @Value("${kubernetes.ingress.publicServiceDomain}")
    private String publicServiceDomain;

    @Value("${kubernetes.ingress.tlsSupported}")
    private Boolean tlsSupported;

    @Value("${kubernetes.ingress.certificateConfigOption}")
    private IngressCertificateConfigOption certificateConfigOption;

    @Value("${kubernetes.ingress.issuerOrWildcardName}")
    private String issuerOrWildcardName;

    @Value("${kubernetes.ingress.ingressPerDomain}")
    private Boolean ingressPerDomain;

    private DomainTechDetailsRepository domainTechDetailsRepository;

    @Autowired
    public KubernetesClusterIngressManager(DomainTechDetailsRepository domainTechDetailsRepository) {
        this.domainTechDetailsRepository = domainTechDetailsRepository;
    }

    public String getExternalServiceDomain(String codename) {
        if(Boolean.TRUE.equals(this.getIngressPerDomain())){
            return domainTechDetailsRepository.findByDomainCodename(codename)
                    .orElseThrow(()-> new IllegalArgumentException("Domain not found")).getExternalServiceDomain();
        }
        return this.getExternalServiceDomain();
    }

    KClusterView.KClusterIngressView getKClusterIngressView() {
        KClusterView.KClusterIngressView kClusterIngressView = new KClusterView.KClusterIngressView();
        kClusterIngressView.setControllerConfigOption(this.controllerConfigOption);
        kClusterIngressView.setSupportedIngressClass(this.supportedIngressClass);
        kClusterIngressView.setPublicIngressClass(this.publicIngressClass);
        kClusterIngressView.setControllerChartName(this.controllerChartName);
        kClusterIngressView.setControllerChartArchive(this.controllerChartArchive);
        kClusterIngressView.setResourceConfigOption(this.resourceConfigOption);
        kClusterIngressView.setExternalServiceDomain(this.externalServiceDomain);
        kClusterIngressView.setPublicServiceDomain(this.publicServiceDomain);
        kClusterIngressView.setTlsSupported(this.tlsSupported);
        kClusterIngressView.setCertificateConfigOption(this.certificateConfigOption);
        kClusterIngressView.setIssuerOrWildcardName(this.issuerOrWildcardName);
        kClusterIngressView.setIngressPerDomain(this.ingressPerDomain);
        return kClusterIngressView;
    }

    @PostConstruct
    public void validateConfig() {
        checkArgument(this.getControllerConfigOption() != null, "ControllerConfigOption property can't be null");
        checkArgument(this.getResourceConfigOption() != null, "ResourceConfigOption property can't be null");
        checkArgument(this.getCertificateConfigOption() != null, "CertificateConfigOption property can't be null");
        KClusterView.KClusterIngressView view = this.getKClusterIngressView();
        this.getControllerConfigOption().validate(view);
        this.getResourceConfigOption().validate(view);
        this.getCertificateConfigOption().validate(view);
    }

}
