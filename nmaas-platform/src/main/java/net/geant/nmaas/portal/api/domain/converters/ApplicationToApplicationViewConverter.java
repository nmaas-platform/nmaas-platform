package net.geant.nmaas.portal.api.domain.converters;

import lombok.AllArgsConstructor;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.KubernetesChartView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.KubernetesTemplateView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesChart;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.orchestration.entities.AppAccessMethod;
import net.geant.nmaas.orchestration.entities.AppStorageVolume;
import net.geant.nmaas.portal.api.domain.AppAccessMethodView;
import net.geant.nmaas.portal.api.domain.AppConfigurationSpecView;
import net.geant.nmaas.portal.api.domain.AppDeploymentSpecView;
import net.geant.nmaas.portal.api.domain.AppDescriptionView;
import net.geant.nmaas.portal.api.domain.AppStorageVolumeView;
import net.geant.nmaas.portal.api.domain.ApplicationMassiveView;
import net.geant.nmaas.portal.api.domain.ConfigFileTemplateView;
import net.geant.nmaas.portal.api.domain.ConfigWizardTemplateView;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ConfigWizardTemplate;
import net.geant.nmaas.portal.persistent.entity.Tag;
import net.geant.nmaas.portal.persistent.repositories.ApplicationBaseRepository;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.AbstractConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ApplicationToApplicationViewConverter extends AbstractConverter<Application, ApplicationMassiveView> {

    private ApplicationBaseRepository appBaseRepository;

    @Override
    protected ApplicationMassiveView convert(Application source) {
        ApplicationBase appBase = getAppBase(source.getName());
        return ApplicationMassiveView.builder()
                .id(appBase.getId())
                .appVersionId(source.getId())
                .name(source.getName())
                .license(appBase.getLicense())
                .licenseUrl(appBase.getLicenseUrl())
                .wwwUrl(appBase.getWwwUrl())
                .sourceUrl(appBase.getSourceUrl())
                .issuesUrl(appBase.getIssuesUrl())
                .nmaasDocumentationUrl(appBase.getNmaasDocumentationUrl())
                .descriptions(getDescriptions(appBase))
                .tags(getTags(appBase))
                .state(source.getState())
                .owner(source.getOwner())
                .version(source.getVersion())
                .configWizardTemplate(getConfigWizardTemplateView(source.getConfigWizardTemplate()))
                .configUpdateWizardTemplate(getConfigWizardTemplateView(source.getConfigUpdateWizardTemplate()))
                .appDeploymentSpec(getAppDeploymentSpec(source))
                .appConfigurationSpec(getAppConfigurationSpec(source))
                .build();
    }

    private AppConfigurationSpecView getAppConfigurationSpec(Application source){
        return new AppConfigurationSpecView( source.getAppConfigurationSpec().getId(),
                getConfigFileTemplates(source),
                source.getAppConfigurationSpec().isConfigFileRepositoryRequired(),
                source.getAppConfigurationSpec().isConfigUpdateEnabled());
    }

    private List<ConfigFileTemplateView> getConfigFileTemplates(Application source){
        return Optional.ofNullable(source.getAppConfigurationSpec().getTemplates()).orElse(Collections.emptyList()).stream()
                .map(template -> new ConfigFileTemplateView(template.getId(), template.getApplicationId(), template.getConfigFileName(), template.getConfigFileDirectory(), template.getConfigFileTemplateContent()))
                .collect(Collectors.toList());
    }

    private AppDeploymentSpecView getAppDeploymentSpec(Application source){
        AppDeploymentSpecView appDeploymentSpec = new AppDeploymentSpecView();
        appDeploymentSpec.setId(source.getAppDeploymentSpec().getId());
        appDeploymentSpec.setKubernetesTemplate(getKubernetesTemplateView(source.getAppDeploymentSpec().getKubernetesTemplate()));
        appDeploymentSpec.setDeployParameters(source.getAppDeploymentSpec().getDeployParameters());
        appDeploymentSpec.setGlobalDeployParameters(source.getAppDeploymentSpec().getGlobalDeployParameters());
        appDeploymentSpec.setSupportedDeploymentEnvironments(source.getAppDeploymentSpec().getSupportedDeploymentEnvironments());
        appDeploymentSpec.setExposesWebUI(source.getAppDeploymentSpec().isExposesWebUI());
        appDeploymentSpec.setStorageVolumes(getAppStorageVolumes(source.getAppDeploymentSpec().getStorageVolumes()));
        appDeploymentSpec.setAccessMethods(getAppAccessMethods(source.getAppDeploymentSpec().getAccessMethods()));
        appDeploymentSpec.setAllowSshAccess(source.getAppDeploymentSpec().isAllowSshAccess());
        return appDeploymentSpec;
    }

    private List<AppStorageVolumeView> getAppStorageVolumes(Set<AppStorageVolume> storageVolumes) {
        List<AppStorageVolumeView> result = new ArrayList<>();
        if(storageVolumes == null) {
            return result;
        }
        for(AppStorageVolume v: storageVolumes) {
            result.add(new AppStorageVolumeView(v.getId(), v.getType(), v.getDefaultStorageSpace(), v.getDeployParameters()));
        }
        return result;
    }

    private List<AppAccessMethodView> getAppAccessMethods(Set<AppAccessMethod> appAccessMethods) {
        List<AppAccessMethodView> result = new ArrayList<>();
        if(appAccessMethods == null) {
            return result;
        }
        for(AppAccessMethod a: appAccessMethods) {
            result.add(new AppAccessMethodView(a.getId(), a.getType(), a.getName(), a.getTag(), a.getDeployParameters()));
        }
        return result;
    }

    private KubernetesTemplateView getKubernetesTemplateView(KubernetesTemplate template){
        if(template == null){
            return null;
        }
        return new KubernetesTemplateView(
                template.getId(),
                getKubernetesChartView(template.getChart()),
                template.getArchive(),
                template.getMainDeploymentName());
    }

    private KubernetesChartView getKubernetesChartView(KubernetesChart kubernetesChart){
        if(kubernetesChart == null){
            return null;
        }
        return new KubernetesChartView(kubernetesChart.getId(), kubernetesChart.getName(), kubernetesChart.getVersion());
    }

    private ConfigWizardTemplateView getConfigWizardTemplateView(ConfigWizardTemplate template){
        if(template == null || StringUtils.isEmpty(template.getTemplate())){
            return null;
        }
        return new ConfigWizardTemplateView(template.getId(), template.getTemplate());
    }

    private List<AppDescriptionView> getDescriptions(ApplicationBase source){
        return Optional.ofNullable(source.getDescriptions()).orElse(Collections.emptyList()).stream()
                .map(description -> new AppDescriptionView(description.getLanguage(), description.getBriefDescription(), description.getFullDescription()))
                .collect(Collectors.toList());
    }

    private Set<String> getTags(ApplicationBase source){
        return Optional.ofNullable(source.getTags()).orElse(Collections.emptySet()).stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());
    }

    private ApplicationBase getAppBase(String name){
        return appBaseRepository.findByName(name).orElseThrow(() -> new MissingElementException("Base app is not found"));
    }
}
