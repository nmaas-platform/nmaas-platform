package net.geant.nmaas.portal.api.domain.converters;

import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import net.geant.nmaas.nmservice.configuration.entities.ConfigFileTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.KubernetesChartView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.KubernetesTemplateView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesChart;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.orchestration.entities.AppAccessMethod;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.AppStorageVolume;
import net.geant.nmaas.portal.api.domain.AppAccessMethodView;
import net.geant.nmaas.portal.api.domain.AppStorageVolumeView;
import net.geant.nmaas.portal.api.domain.ApplicationMassiveView;
import net.geant.nmaas.portal.api.domain.ConfigWizardTemplateView;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ConfigWizardTemplate;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.AbstractConverter;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ApplicationViewToApplicationConverter extends AbstractConverter<ApplicationMassiveView, Application> {

    @Override
    protected Application convert(ApplicationMassiveView source) {
        Application app = new Application(source.getAppVersionId(), source.getName(), source.getVersion(), source.getOwner());
        app.setState(source.getState());
        app.setConfigWizardTemplate(getConfigWizardTemplate(source.getConfigWizardTemplate()));
        app.setConfigUpdateWizardTemplate(getConfigWizardTemplate(source.getConfigUpdateWizardTemplate()));
        app.setAppDeploymentSpec(getAppDeploymentSpec(source));
        app.setAppConfigurationSpec(getAppConfigurationSpec(source));
        return app;
    }

    private AppConfigurationSpec getAppConfigurationSpec(ApplicationMassiveView source) {
        return new AppConfigurationSpec(
                source.getAppConfigurationSpec().getId(),
                source.getAppConfigurationSpec().isConfigFileRepositoryRequired(),
                getConfigFileTemplates(source),
                source.getAppConfigurationSpec().isConfigUpdateEnabled()
        );
    }

    private List<ConfigFileTemplate> getConfigFileTemplates(ApplicationMassiveView source) {
        return Optional.ofNullable(source.getAppConfigurationSpec().getTemplates()).orElse(Collections.emptyList()).stream()
                .map(template ->
                        new ConfigFileTemplate(
                                template.getId(),
                                template.getApplicationId(),
                                template.getConfigFileName(),
                                template.getConfigFileDirectory(),
                                template.getConfigFileTemplateContent()
                        )
                )
                .collect(Collectors.toList());
    }

    private AppDeploymentSpec getAppDeploymentSpec(ApplicationMassiveView source) {
        AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
        appDeploymentSpec.setId(source.getAppDeploymentSpec().getId());
        appDeploymentSpec.setSupportedDeploymentEnvironments(source.getAppDeploymentSpec().getSupportedDeploymentEnvironments());
        appDeploymentSpec.setDeployParameters(source.getAppDeploymentSpec().getDeployParameters());
        appDeploymentSpec.setGlobalDeployParameters(source.getAppDeploymentSpec().getGlobalDeployParameters());
        appDeploymentSpec.setKubernetesTemplate(getKubernetesTemplate(source.getAppDeploymentSpec().getKubernetesTemplate()));
        appDeploymentSpec.setExposesWebUI(source.getAppDeploymentSpec().isExposesWebUI());
        appDeploymentSpec.setStorageVolumes(getAppStorageVolumes(source.getAppDeploymentSpec().getStorageVolumes()));
        appDeploymentSpec.setAccessMethods(getAppAccessMethods(source.getAppDeploymentSpec().getAccessMethods()));
        appDeploymentSpec.setAllowSshAccess(source.getAppDeploymentSpec().isAllowSshAccess());
        return appDeploymentSpec;
    }

    private Set<AppStorageVolume> getAppStorageVolumes(List<AppStorageVolumeView> views) {
        Set<AppStorageVolume> result = new HashSet<>();
        if (views == null) {
            return result;
        }
        for (AppStorageVolumeView sv : views) {
            result.add(
                    new AppStorageVolume(
                            sv.getId(),
                            sv.getType(),
                            sv.getDefaultStorageSpace(),
                            sv.getDeployParameters()
                    )
            );
        }
        return result;
    }

    private Set<AppAccessMethod> getAppAccessMethods(List<AppAccessMethodView> views) {
        Set<AppAccessMethod> result = new HashSet<>();
        if (views == null) {
            return result;
        }
        for (AppAccessMethodView av : views) {
            result.add(
                    new AppAccessMethod(
                            av.getId(),
                            av.getType(),
                            av.getName(),
                            av.getTag(),
                            av.getDeployParameters()
                    )
            );
        }
        return result;
    }

    private KubernetesTemplate getKubernetesTemplate(KubernetesTemplateView template) {
        if (template == null) {
            return null;
        }
        return new KubernetesTemplate(template.getId(),
                getKubernetesChartView(template.getChart()),
                template.getArchive(),
                template.getMainDeploymentName());
    }

    private KubernetesChart getKubernetesChartView(KubernetesChartView kubernetesChart) {
        if (kubernetesChart == null || StringUtils.isEmpty(kubernetesChart.getName())) {
            return null;
        }
        return new KubernetesChart(kubernetesChart.getId(),
                kubernetesChart.getName(),
                kubernetesChart.getVersion());
    }

    private ConfigWizardTemplate getConfigWizardTemplate(ConfigWizardTemplateView template) {
        if (template == null || StringUtils.isEmpty(template.getTemplate())) {
            return null;
        }
        return new ConfigWizardTemplate(template.getId(), template.getTemplate());
    }

}
