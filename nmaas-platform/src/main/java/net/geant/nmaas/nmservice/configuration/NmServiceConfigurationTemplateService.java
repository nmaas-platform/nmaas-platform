package net.geant.nmaas.nmservice.configuration;

import java.util.List;
import net.geant.nmaas.nmservice.configuration.model.NmServiceConfigurationTemplateView;

public interface NmServiceConfigurationTemplateService {
    List<NmServiceConfigurationTemplateView> findAll();
    List<NmServiceConfigurationTemplateView> findAllByAppId(Long appId);
    void validateTemplates(List<NmServiceConfigurationTemplateView> templates);
    void validateSubmittedTemplates(Long appId);
    void addTemplate(NmServiceConfigurationTemplateView configurationTemplate);
}
