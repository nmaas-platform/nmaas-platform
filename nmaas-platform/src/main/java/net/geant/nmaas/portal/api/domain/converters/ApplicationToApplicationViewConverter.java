package net.geant.nmaas.portal.api.domain.converters;

import lombok.AllArgsConstructor;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.KubernetesChartView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.KubernetesTemplateView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesChart;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.portal.api.domain.AppConfigurationSpecView;
import net.geant.nmaas.portal.api.domain.AppDeploymentSpecView;
import net.geant.nmaas.portal.api.domain.AppDescriptionView;
import net.geant.nmaas.portal.api.domain.ApplicationView;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ApplicationToApplicationViewConverter extends AbstractConverter<Application, ApplicationView> {

    private ApplicationBaseRepository appBaseRepository;

    @Override
    protected ApplicationView convert(Application source) {
        ApplicationBase appBase = getAppBase(source.getName());
        return ApplicationView.builder()
                .id(appBase.getId())
                .appVersionId(source.getId())
                .name(source.getName())
                .license(appBase.getLicense())
                .licenseUrl(appBase.getLicenseUrl())
                .wwwUrl(appBase.getWwwUrl())
                .sourceUrl(appBase.getSourceUrl())
                .issuesUrl(appBase.getIssuesUrl())
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
        return new AppConfigurationSpecView(getConfigFileTemplates(source), source.getAppConfigurationSpec().isConfigFileRepositoryRequired());
    }

    private List<ConfigFileTemplateView> getConfigFileTemplates(Application source){
        return Optional.ofNullable(source.getAppConfigurationSpec().getTemplates()).orElse(Collections.emptyList()).stream()
                .map(template -> new ConfigFileTemplateView(template.getId(), template.getApplicationId(), template.getConfigFileName(), template.getConfigFileTemplateContent()))
                .collect(Collectors.toList());
    }

    private AppDeploymentSpecView getAppDeploymentSpec(Application source){
        AppDeploymentSpecView appDeploymentSpec = new AppDeploymentSpecView();
        appDeploymentSpec.setDefaultStorageSpace(source.getAppDeploymentSpec().getDefaultStorageSpace());
        appDeploymentSpec.setKubernetesTemplate(getKubernetesTemplateView(source.getAppDeploymentSpec().getKubernetesTemplate()));
        appDeploymentSpec.setDeployParameters(source.getAppDeploymentSpec().getDeployParameters());
        appDeploymentSpec.setSupportedDeploymentEnvironments(source.getAppDeploymentSpec().getSupportedDeploymentEnvironments());
        appDeploymentSpec.setExposesWebUI(source.getAppDeploymentSpec().isExposesWebUI());
        return appDeploymentSpec;
    }

    private KubernetesTemplateView getKubernetesTemplateView(KubernetesTemplate template){
        if(template == null){
            return null;
        }
        return new KubernetesTemplateView(getKubernetesChartView(template.getChart()), template.getArchive());
    }

    private KubernetesChartView getKubernetesChartView(KubernetesChart kubernetesChart){
        if(kubernetesChart == null){
            return null;
        }
        return new KubernetesChartView(kubernetesChart.getName(), kubernetesChart.getVersion());
    }

    private ConfigWizardTemplateView getConfigWizardTemplateView(ConfigWizardTemplate template){
        if(template == null || StringUtils.isEmpty(template.getTemplate())){
            return null;
        }
        return new ConfigWizardTemplateView(template.getTemplate());
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