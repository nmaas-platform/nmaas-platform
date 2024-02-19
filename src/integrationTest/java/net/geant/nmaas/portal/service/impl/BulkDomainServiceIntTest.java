package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.bulk.BulkDeploymentViewS;
import net.geant.nmaas.portal.api.bulk.BulkType;
import net.geant.nmaas.portal.api.bulk.CsvDomain;
import net.geant.nmaas.portal.api.domain.UserViewMinimal;
import net.geant.nmaas.portal.persistent.entity.BulkDeployment;
import net.geant.nmaas.portal.persistent.entity.BulkDeploymentState;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.BulkDeploymentRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRoleRepository;
import net.geant.nmaas.portal.service.BulkDomainService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
@Rollback
public class BulkDomainServiceIntTest {

    @Autowired
    private BulkDeploymentRepository bulkDeploymentRepository;

    @Autowired
    private BulkDomainService bulkDomainService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRoleRepository userRoleRepository;

    @AfterEach
    void cleanup() {
        bulkDeploymentRepository.deleteAll();
    }

    @Test
    void shouldHandleBulkCreationOfDomainWithUniqueCodenames() {
        CsvDomain csvDomain1 = new CsvDomain("Test.Domain.100", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain2 = new CsvDomain("Test.Domain.101", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain3 = new CsvDomain("Test.Domain.102", "user1", "user1@test.com", null, "group1");
        List<CsvDomain> input = List.of(csvDomain1, csvDomain2, csvDomain3);
        UserViewMinimal creator = new UserViewMinimal();
        creator.setId(1L);
        creator.setUsername("testuser");
        User user = new User("admin");
        user.setId(1L);
        user.setEmail("test@test.com");
        when(userService.findByUsername(any())).thenReturn(Optional.of(user));
        when(userService.registerBulk(any(), any(), any())).thenReturn(user);

        BulkDeploymentViewS result = bulkDomainService.handleBulkCreation(input, creator);

        assertEquals(BulkDeploymentState.COMPLETED, result.getState());
        List<BulkDeployment> bulkDeployments = bulkDeploymentRepository.findAll();
        assertEquals(1, bulkDeployments.size());
        BulkDeployment bulkDeployment = bulkDeployments.get(0);
        assertEquals(1, bulkDeployment.getCreatorId());
        assertEquals(BulkType.DOMAIN, bulkDeployment.getType());
        assertEquals(6, bulkDeployment.getEntries().size());
        assertEquals("testdomain10", bulkDeployment.getEntries().get(0).getDetails().get("domainCodename"));
        assertEquals("testdomain11", bulkDeployment.getEntries().get(2).getDetails().get("domainCodename"));
        assertEquals("testdomain12", bulkDeployment.getEntries().get(4).getDetails().get("domainCodename"));
    }

    @Test
    void shouldHandleBulkCreationOfDomainWithManyUniqueCodenames() {
        CsvDomain csvDomain1 = new CsvDomain("Test2.Domain#User154", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain2 = new CsvDomain("Test2.Domain#User324", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain3 = new CsvDomain("Test2.Domain#User453", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain4 = new CsvDomain("Test2.Domain#User236", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain5 = new CsvDomain("Test2.Domain#User753", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain6 = new CsvDomain("Test2.Domain#User823", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain7 = new CsvDomain("Test2.Domain#User156", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain8 = new CsvDomain("Test2.Domain#User754", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain9 = new CsvDomain("Test2.Domain#User865", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain10 = new CsvDomain("Test2.Domain#User933", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain11 = new CsvDomain("Test2.Domain#User944", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain12 = new CsvDomain("Test2.Domain#User966", "user1", "user1@test.com", null, "group1");
        List<CsvDomain> input =
                List.of(csvDomain1, csvDomain2, csvDomain3, csvDomain4, csvDomain5, csvDomain6,
                        csvDomain7, csvDomain8, csvDomain9, csvDomain10, csvDomain11, csvDomain12);
        User user = new User("admin");
        user.setId(1L);
        user.setEmail("test@test.com");
        when(userService.findByUsername(any())).thenReturn(Optional.of(user));
        when(userService.registerBulk(any(), any(), any())).thenReturn(user);

        bulkDomainService.handleBulkCreation(input, new UserViewMinimal());

        BulkDeployment bulkDeployment = bulkDeploymentRepository.findAll().get(0);
        assertEquals(24, bulkDeployment.getEntries().size());
        assertEquals("test2domainu", bulkDeployment.getEntries().get(0).getDetails().get("domainCodename"));
        assertEquals("test2domain1", bulkDeployment.getEntries().get(2).getDetails().get("domainCodename"));
        assertEquals("test2domain2", bulkDeployment.getEntries().get(4).getDetails().get("domainCodename"));
        assertEquals("test2domain3", bulkDeployment.getEntries().get(6).getDetails().get("domainCodename"));
        assertEquals("test2domain4", bulkDeployment.getEntries().get(8).getDetails().get("domainCodename"));
        assertEquals("test2domain5", bulkDeployment.getEntries().get(10).getDetails().get("domainCodename"));
        assertEquals("test2domain6", bulkDeployment.getEntries().get(12).getDetails().get("domainCodename"));
        assertEquals("test2domain7", bulkDeployment.getEntries().get(14).getDetails().get("domainCodename"));
        assertEquals("test2domain8", bulkDeployment.getEntries().get(16).getDetails().get("domainCodename"));
        assertEquals("test2domain9", bulkDeployment.getEntries().get(18).getDetails().get("domainCodename"));
        assertEquals("test2domai10", bulkDeployment.getEntries().get(20).getDetails().get("domainCodename"));
        assertEquals("test2domai11", bulkDeployment.getEntries().get(22).getDetails().get("domainCodename"));
    }

}
