package net.geant.nmaas.portal.api.domain;

import com.google.common.collect.Sets;
import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.InetAddress;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(SpringExtension.class)
@SpringBootTest
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
        ApplicationView appView = modelMapper.map(getDefaultApp(), ApplicationView.class);
        assertNotNull(appView.getConfigWizardTemplate());
        assertNull(appView.getConfigUpdateWizardTemplate());
        assertEquals(getDefaultApp().getAppDeploymentSpec().isExposesWebUI(), appView.getAppDeploymentSpec().isExposesWebUI());
    }

    @Test
    public void testConvertAppViewToAppBase(){
        ApplicationBaseView appView = getDefaultAppBaseView();
        ApplicationBase appBase = modelMapper.map(appView, ApplicationBase.class);
        assertEquals(appView.getId(), appBase.getId());
        assertEquals(appView.getName(), appBase.getName());
        assertNotNull(appBase.getTags());
    }

    @Test
    public void testConvertAppBaseToAppBaseView(){
        ApplicationBase appBase = getDefaultAppBase();
        ApplicationBaseView applicationBaseView = modelMapper.map(appBase, ApplicationBaseView.class);
        assertEquals(appBase.getName(), applicationBaseView.getName());
        assertNotNull(applicationBaseView.getTags());
        assertEquals(1, applicationBaseView.getVersions().size());
        assertTrue(applicationBaseView.getVersions().stream().anyMatch(version -> version.getVersion().equals("0.0.1")));
        assertTrue(applicationBaseView.getVersions().stream().anyMatch(version -> version.getState().equals(ApplicationState.ACTIVE)));
    }

    @Test
    public void testConvertAppViewToApp(){
        ApplicationView appView = getDefaultAppView();
        Application app = modelMapper.map(appView, Application.class);
        assertEquals(appView.getState(), app.getState());
        assertNotNull(app.getConfigWizardTemplate());
        assertNull(app.getConfigUpdateWizardTemplate());
        assertNotNull(app.getAppDeploymentSpec());
        assertEquals(appView.getAppDeploymentSpec().isExposesWebUI(), app.getAppDeploymentSpec().isExposesWebUI());
    }

	@Test
	public void testConvertAppBaseViewToAppBase() {
        tagRepo.save(new Tag("network"));

        ApplicationBaseView appDto = new ApplicationBaseView();
        appDto.setId(1L);
        appDto.setName("myApp");
        appDto.setLicense("GNL");
        appDto.getTags().add(new TagView("monitoring"));
        appDto.getTags().add(new TagView("network"));

        ApplicationBase appEntity = modelMapper.map(appDto, ApplicationBase.class);

        assertEquals(appDto.getId(), appEntity.getId());
        assertEquals(appDto.getName(), appEntity.getName());
        assertEquals(appDto.getLicense(), appEntity.getLicense());
        assertEquals(2, appEntity.getTags().size());
        assertEquals(appDto.getTags().size(), appEntity.getTags().size());
        assertTrue((appEntity.getTags().toArray()[0]) instanceof Tag);

        appDto = modelMapper.map(appEntity, ApplicationBaseView.class);
        assertEquals(2, appDto.getTags().size());
        assertEquals(appEntity.getTags().size(), appDto.getTags().size());
        assertTrue(appDto.getTags().contains(new TagView("network")));
        assertTrue(appDto.getTags().contains(new TagView("monitoring")));
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

	private ApplicationBaseView getDefaultAppBaseView(){
        ApplicationBaseView appView = new ApplicationBaseView();
        appView.setName("testApp");
        appView.setLicense("MIT");
        appView.setLicenseUrl("MIT.org");
        appView.setWwwUrl("default-website.com");
        appView.setSourceUrl("default-website.com");
        appView.setIssuesUrl("default-website.com");
        appView.setId(1L);
        return appView;
    }

    private ApplicationView getDefaultAppView() {
        ApplicationView app = new ApplicationView();
        app.setId(1L);
        app.setName("testApp");
        app.setVersion("0.0.1");
        app.setConfigWizardTemplate(new ConfigWizardTemplateView(2L,"template"));
        app.setAppConfigurationSpec(new AppConfigurationSpecView());
        app.setAppDeploymentSpec(new AppDeploymentSpecView());
        app.getAppDeploymentSpec().setExposesWebUI(true);
        app.setState(ApplicationState.ACTIVE);
        return app;
    }

	private ApplicationBase getDefaultAppBase(){
        ApplicationBase appBase = new ApplicationBase();
        appBase.setName("testApp");
        appBase.setLicense("MIT");
        appBase.setLicenseUrl("MIT.org");
        appBase.setWwwUrl("default-website.com");
        appBase.setSourceUrl("default-website.com");
        appBase.setIssuesUrl("default-website.com");
        appBase.setDescriptions(new ArrayList<>());
        appBase.setLogo(new FileInfo("logo", "png"));
        appBase.setVersions(Sets.newHashSet(new ApplicationVersion(null, "0.0.1", ApplicationState.ACTIVE, 1L)));
        appBase.setOwner("admin");
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
        return app;
    }

}
