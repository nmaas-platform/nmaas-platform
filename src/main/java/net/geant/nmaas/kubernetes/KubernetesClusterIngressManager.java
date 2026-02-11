package net.geant.nmaas.kubernetes;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.api.dto.kubernetes.IngressCertificateConfigOptionDto;
import net.geant.nmaas.api.dto.kubernetes.IngressControllerConfigOptionDto;
import net.geant.nmaas.api.dto.kubernetes.IngressResourceConfigOptionDto;
import net.geant.nmaas.api.dto.kubernetes.KClusterDto.KClusterIngressView;
import net.geant.nmaas.kubernetes.remote.entities.IngressCertificateConfigOption;
import net.geant.nmaas.kubernetes.remote.entities.IngressControllerConfigOption;
import net.geant.nmaas.kubernetes.remote.entities.IngressResourceConfigOption;
import net.geant.nmaas.orchestration.repositories.DomainTechDetailsRepository;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

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
        if (Boolean.TRUE.equals(this.getIngressPerDomain())) {
            return domainTechDetailsRepository.findByDomainCodename(codename)
                    .orElseThrow(() -> new IllegalArgumentException("Domain not found")).getExternalServiceDomain();
        }
        return this.getExternalServiceDomain();
    }

    public KClusterIngressView getKClusterIngressView() {
        KClusterIngressView view = new KClusterIngressView();
        if (Objects.nonNull(controllerConfigOption)) {
            view.setControllerConfigOption(IngressControllerConfigOptionDto.valueOf(this.controllerConfigOption.name()));
        }
        view.setSupportedIngressClass(this.supportedIngressClass);
        view.setPublicIngressClass(this.publicIngressClass);
        view.setControllerChartName(this.controllerChartName);
        view.setControllerChartArchive(this.controllerChartArchive);
        if (Objects.nonNull(resourceConfigOption)) {
            view.setResourceConfigOption(IngressResourceConfigOptionDto.valueOf(this.resourceConfigOption.name()));
        }
        view.setExternalServiceDomain(this.externalServiceDomain);
        view.setPublicServiceDomain(this.publicServiceDomain);
        view.setTlsSupported(this.tlsSupported);
        if (Objects.nonNull(certificateConfigOption)) {
            view.setCertificateConfigOption(IngressCertificateConfigOptionDto.valueOf(this.certificateConfigOption.name()));
        }
        view.setIssuerOrWildcardName(this.issuerOrWildcardName);
        view.setIngressPerDomain(this.ingressPerDomain);
        return view;
    }

    @PostConstruct
    public void validateConfig() {
        Validate.isTrue(this.getControllerConfigOption() != null, "ControllerConfigOption property can't be null");
        Validate.isTrue(this.getResourceConfigOption() != null, "ResourceConfigOption property can't be null");
        if (this.getTlsSupported()) {
            Validate.isTrue(this.getCertificateConfigOption() != null, "CertificateConfigOption property can't be null if TLS is supported");
        }
        KClusterIngressView view = this.getKClusterIngressView();
        this.getControllerConfigOption().validate(view);
        this.getResourceConfigOption().validate(view);
        if (this.getCertificateConfigOption() != null) {
            this.getCertificateConfigOption().validate(view);
        }
    }

}
