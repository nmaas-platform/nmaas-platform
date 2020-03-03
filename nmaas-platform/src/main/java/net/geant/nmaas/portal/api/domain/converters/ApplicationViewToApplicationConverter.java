package net.geant.nmaas.portal.api.domain.converters;

import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import net.geant.nmaas.nmservice.configuration.entities.ConfigFileTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.KubernetesChartView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.KubernetesTemplateView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesChart;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.orchestration.entities.AppAccessMethod;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.portal.api.domain.AppAccessMethodView;
import net.geant.nmaas.portal.api.domain.ApplicationView;
import net.geant.nmaas.portal.api.domain.ConfigWizardTemplateView;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ConfigWizardTemplate;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.AbstractConverter;

import java.util.*;
import java.util.stream.Collectors;

public class ApplicationViewToApplicationConverter extends AbstractConverter<ApplicationView, Application> {

    @Override
    protected Application convert(ApplicationView source) {
        Application app = new Application(source.getAppVersionId(), source.getName(), source.getVersion(), source.getOwner());
        app.setState(source.getState());
        app.setConfigWizardTemplate(getConfigWizardTemplate(source.getConfigWizardTemplate()));
        app.setConfigUpdateWizardTemplate(getConfigWizardTemplate(source.getConfigUpdateWizardTemplate()));
        app.setAppDeploymentSpec(getAppDeploymentSpec(source));
        app.setAppConfigurationSpec(getAppConfigurationSpec(source));
        return app;
    }

    private AppConfigurationSpec getAppConfigurationSpec(ApplicationView source){
        return new AppConfigurationSpec(null, source.getAppConfigurationSpec().isConfigFileRepositoryRequired(), getConfigFileTemplates(source));
    }

    private List<ConfigFileTemplate> getConfigFileTemplates(ApplicationView source){
        return Optional.ofNullable(source.getAppConfigurationSpec().getTemplates()).orElse(Collections.emptyList()).stream()
                .map(template -> new ConfigFileTemplate(template.getId(), template.getApplicationId(), template.getConfigFileName(), template.getConfigFileTemplateContent()))
                .collect(Collectors.toList());
    }

    private AppDeploymentSpec getAppDeploymentSpec(ApplicationView source){
        AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
        appDeploymentSpec.setSupportedDeploymentEnvironments(source.getAppDeploymentSpec().getSupportedDeploymentEnvironments());
        appDeploymentSpec.setDefaultStorageSpace(source.getAppDeploymentSpec().getDefaultStorageSpace());
        appDeploymentSpec.setDeployParameters(source.getAppDeploymentSpec().getDeployParameters());
        appDeploymentSpec.setKubernetesTemplate(getKubernetesTemplate(source.getAppDeploymentSpec().getKubernetesTemplate()));
        appDeploymentSpec.setExposesWebUI(source.getAppDeploymentSpec().isExposesWebUI());
        appDeploymentSpec.setAccessMethods(getAppAccessMethods(source.getAppDeploymentSpec().getAccessMethods()));
        return appDeploymentSpec;
    }

    private Set<AppAccessMethod> getAppAccessMethods(List<AppAccessMethodView> views) {
        Set<AppAccessMethod> result = new HashSet<>();
        if(views == null) {
            return result;
        }
        for(AppAccessMethodView av: views) {
            result.add(new AppAccessMethod(av.getType(), av.getName(), av.getTag(), av.getDeployParameters()));
        }
        return result;
    }

    private KubernetesTemplate getKubernetesTemplate(KubernetesTemplateView template){
        if(template == null){
            return null;
        }
        return new KubernetesTemplate(getKubernetesChartView(template.getChart()), template.getArchive());
    }

    private KubernetesChart getKubernetesChartView(KubernetesChartView kubernetesChart){
        if(kubernetesChart == null || StringUtils.isEmpty(kubernetesChart.getName())){
            return null;
        }
        return new KubernetesChart(kubernetesChart.getName(), kubernetesChart.getVersion());
    }

    private ConfigWizardTemplate getConfigWizardTemplate(ConfigWizardTemplateView template){
        if(template == null || StringUtils.isEmpty(template.getTemplate())){
            return null;
        }
        return new ConfigWizardTemplate(template.getTemplate());
    }

}
