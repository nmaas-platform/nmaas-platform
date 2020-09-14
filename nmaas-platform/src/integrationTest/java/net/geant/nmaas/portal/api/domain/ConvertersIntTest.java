package net.geant.nmaas.portal.api.domain;

import com.google.common.collect.Sets;
import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.portal.ConvertersConfig;
import net.geant.nmaas.portal.PersistentConfig;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import net.geant.nmaas.portal.persistent.entity.ApplicationSubscription;
import net.geant.nmaas.portal.persistent.entity.ApplicationVersion;
import net.geant.nmaas.portal.persistent.entity.ConfigWizardTemplate;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.FileInfo;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.Tag;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistent.repositories.TagRepository;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {ConvertersConfig.class, PersistentConfig.class})
@EnableAutoConfiguration
@Transactional
@Rollback
public class ConvertersIntTest {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    TagRepository tagRepo;

    @Autowired
    ApplicationBaseRepository appBaseRepo;

    @Test
    public void testConvertAppToAppView(){
        ApplicationBase defaultAppBase = getDefaultAppBase();
        appBaseRepo.save(defaultAppBase);
        ApplicationMassiveView appView = modelMapper.map(getDefaultApp(), ApplicationMassiveView.class);
        assertNotNull(appView.getConfigWizardTemplate());
        assertNull(appView.getConfigUpdateWizardTemplate());
        assertEquals(defaultAppBase.getIssuesUrl(), appView.getIssuesUrl());
        assertEquals(getDefaultApp().getAppDeploymentSpec().isExposesWebUI(), appView.getAppDeploymentSpec().isExposesWebUI());
    }

    @Test
    public void testConvertAppViewToAppBase(){
        ApplicationMassiveView appView = getDefaultAppView();
        ApplicationBase appBase = modelMapper.map(appView, ApplicationBase.class);
        assertEquals(appView.getId(), appBase.getId());
        assertEquals(appView.getName(), appBase.getName());
        assertNotNull(appBase.getTags());
    }

    @Test
    public void testConvertAppBaseToAppBriefView(){
        ApplicationBase appBase = getDefaultAppBase();
        ApplicationBaseView applicationBaseView = modelMapper.map(appBase, ApplicationBaseView.class);
        assertEquals(appBase.getName(), applicationBaseView.getName());
        assertNotNull(applicationBaseView.getTags());
        assertEquals(1, applicationBaseView.getAppVersions().size());
        assertTrue(applicationBaseView.getAppVersions().stream().anyMatch(version -> version.getVersion().equals("0.0.1")));
        assertTrue(applicationBaseView.getAppVersions().stream().anyMatch(version -> version.getState().equals(ApplicationState.ACTIVE)));
    }

    @Test
    public void testConvertAppViewToApp(){
        ApplicationMassiveView appView = getDefaultAppView();
        Application app = modelMapper.map(appView, Application.class);
        assertEquals(appView.getState(), app.getState());
        assertNotNull(app.getConfigWizardTemplate());
        assertNull(app.getConfigUpdateWizardTemplate());
        assertNotNull(app.getAppDeploymentSpec());
        assertEquals(appView.getAppDeploymentSpec().isExposesWebUI(), app.getAppDeploymentSpec().isExposesWebUI());
    }

	@Test
	public void testConvertAppBriefViewToAppBase() {
        tagRepo.save(new Tag("network"));

        ApplicationBaseView appDto = new ApplicationBaseView();
        appDto.setId(1L);
        appDto.setName("myApp");
        appDto.setLicense("GNL");
        appDto.getTags().add("monitoring");
        appDto.getTags().add("network");

        ApplicationBase appEntity = modelMapper.map(appDto, ApplicationBase.class);

        assertEquals(appDto.getId(), appEntity.getId());
        assertEquals(appDto.getName(), appEntity.getName());
        assertEquals(appDto.getLicense(), appEntity.getLicense());
        assertEquals(2, appEntity.getTags().size());
        assertEquals(appDto.getTags().size(), appEntity.getTags().size());
        assertTrue((appEntity.getTags().toArray()[0]) instanceof Tag);

        Object[] tags = appEntity.getTags().toArray();

        assertNull( (((Tag) tags[0]).getName().equals("monitoring") ? ((Tag)tags[0]).getId() : ((Tag)tags[1]).getId()));
        assertNotNull((((Tag) tags[1]).getName().equals("network") ? ((Tag)tags[1]).getId() : ((Tag)tags[0]).getId()));

        appDto = modelMapper.map(appEntity, ApplicationBaseView.class);
        assertEquals(2, appDto.getTags().size());
        assertEquals(appEntity.getTags().size(), appDto.getTags().size());
        assertTrue(appDto.getTags().contains("network"));
        assertTrue(appDto.getTags().contains("monitoring"));
	}

	@Test
    void shouldConvertAppSubscriptionToAppSubscriptionBase(){
        Domain domain = new Domain("name", "name");
        ApplicationBase appBase = getDefaultAppBase();
        domain.setId(1L);
        appBase.setId(1L);
        ApplicationSubscription appSub = new ApplicationSubscription(domain, appBase);
        ApplicationSubscriptionBase appSubBase = modelMapper.map(appSub, ApplicationSubscriptionBase.class);
        assertEquals(appBase.getId(), appSubBase.applicationId);
        assertEquals(domain.getId(), appSubBase.domainId);
    }

    @Test
    void shouldConvertStringToInetAddress(){
        InetAddress addr = modelMapper.map("127.0.0.1", InetAddress.class);
        assertNotNull(addr);
    }

    @Test
    void shouldReturnNullWhenStringIsNotCorrectInetAddress(){
        InetAddress addr = modelMapper.map("ip.not.found", InetAddress.class);
        assertNull(addr);
    }

    @Test
    void shouldConvertInetAddressToString() throws Exception {
        String addr = modelMapper.map(InetAddress.getByName("127.0.0.1"), String.class);
        assertTrue(StringUtils.isNotEmpty(addr));
    }

    @Test
    void shouldConvertUserRoleToRole(){
        UserRole userRole = new UserRole(new User("admin", true), new Domain("name", "name"), Role.ROLE_SYSTEM_ADMIN);
        Role role = modelMapper.map(userRole, Role.class);
        assertEquals(Role.ROLE_SYSTEM_ADMIN, role);
    }

	private ApplicationMassiveView getDefaultAppView(){
        ApplicationMassiveView appView = new ApplicationMassiveView();
        appView.setName("testApp");
        appView.setLicense("MIT");
        appView.setLicenseUrl("MIT.org");
        appView.setWwwUrl("default-website.com");
        appView.setSourceUrl("default-website.com");
        appView.setIssuesUrl("default-website.com");
        appView.setId(1L);
        appView.setVersion("0.0.1");
        appView.setConfigWizardTemplate(new ConfigWizardTemplateView(45L, "template"));
        appView.setAppConfigurationSpec(new AppConfigurationSpecView());
        appView.setAppDeploymentSpec(new AppDeploymentSpecView());
        appView.getAppDeploymentSpec().setExposesWebUI(true);
        appView.setState(ApplicationState.ACTIVE);
        appView.setOwner("admin");
        return appView;
    }

	private ApplicationBase getDefaultAppBase(){
        ApplicationBase appBase = new ApplicationBase();
        appBase.setName("testApp");
        appBase.setLicense("MIT");
        appBase.setLicenseUrl("MIT.org");
        appBase.setWwwUrl("default-website.com");
        appBase.setSourceUrl("default-website.com");
        appBase.setIssuesUrl("default-website.com");
        appBase.setLogo(new FileInfo("logo", "png"));
        appBase.setVersions(Sets.newHashSet(new ApplicationVersion(null, "0.0.1", ApplicationState.ACTIVE, 1L)));
        return appBase;
    }

	private Application getDefaultApp(){
        Application app = new Application();
        app.setId(1L);
        app.setName("testApp");
        app.setVersion("0.0.1");
        app.setConfigWizardTemplate(new ConfigWizardTemplate("template"));
        app.setAppConfigurationSpec(new AppConfigurationSpec());
        app.setAppDeploymentSpec(new AppDeploymentSpec());
        app.getAppDeploymentSpec().setExposesWebUI(true);
        app.setState(ApplicationState.ACTIVE);
        app.setOwner("admin");
        return app;
    }

}
