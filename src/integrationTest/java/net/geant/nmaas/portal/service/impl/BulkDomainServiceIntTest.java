package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView;
import net.geant.nmaas.portal.api.bulk.CsvBean;
import net.geant.nmaas.portal.api.bulk.CsvDomain;
import net.geant.nmaas.portal.service.BulkDomainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
@Rollback
public class BulkDomainServiceIntTest {

    @Autowired
    private BulkDomainService bulkDomainService;

    @Test
    void shouldHandleBulkCreationOfDomainWithUniqueCodenames() {
        CsvDomain csvDomain1 = new CsvDomain("Test.Domain.100", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain2 = new CsvDomain("Test.Domain.101", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain3 = new CsvDomain("Test.Domain.102", "user1", "user1@test.com", null, "group1");
        List<CsvBean> input = List.of(csvDomain1, csvDomain2, csvDomain3);

        List<BulkDeploymentEntryView> responses = bulkDomainService.handleBulkCreation(input);

        assertEquals(6, responses.size());
        assertEquals("testdomain10", responses.get(0).getDetails().get("domainCodename"));
        assertEquals("testdomain11", responses.get(2).getDetails().get("domainCodename"));
        assertEquals("testdomain12", responses.get(4).getDetails().get("domainCodename"));
    }

    @Test
    void shouldHandleBulkCreationOfDomainWithManyUniqueCodenames() {
        CsvDomain csvDomain1 = new CsvDomain("Test.Domain#User154", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain2 = new CsvDomain("Test.Domain#User324", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain3 = new CsvDomain("Test.Domain#User453", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain4 = new CsvDomain("Test.Domain#User236", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain5 = new CsvDomain("Test.Domain#User753", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain6 = new CsvDomain("Test.Domain#User823", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain7 = new CsvDomain("Test.Domain#User156", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain8 = new CsvDomain("Test.Domain#User754", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain9 = new CsvDomain("Test.Domain#User865", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain10 = new CsvDomain("Test.Domain#User933", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain11 = new CsvDomain("Test.Domain#User944", "user1", "user1@test.com", null, "group1");
        CsvDomain csvDomain12 = new CsvDomain("Test.Domain#User966", "user1", "user1@test.com", null, "group1");
        List<CsvBean> input =
                List.of(csvDomain1, csvDomain2, csvDomain3, csvDomain4, csvDomain5, csvDomain6,
                        csvDomain7, csvDomain8, csvDomain9, csvDomain10, csvDomain11, csvDomain12);

        List<BulkDeploymentEntryView> responses = bulkDomainService.handleBulkCreation(input);

        assertEquals(24, responses.size());
        assertEquals("testdomainus", responses.get(0).getDetails().get("domainCodename"));
        assertEquals("testdomainu1", responses.get(2).getDetails().get("domainCodename"));
        assertEquals("testdomainu2", responses.get(4).getDetails().get("domainCodename"));
        assertEquals("testdomainu3", responses.get(6).getDetails().get("domainCodename"));
        assertEquals("testdomainu4", responses.get(8).getDetails().get("domainCodename"));
        assertEquals("testdomainu5", responses.get(10).getDetails().get("domainCodename"));
        assertEquals("testdomainu6", responses.get(12).getDetails().get("domainCodename"));
        assertEquals("testdomainu7", responses.get(14).getDetails().get("domainCodename"));
        assertEquals("testdomainu8", responses.get(16).getDetails().get("domainCodename"));
        assertEquals("testdomainu9", responses.get(18).getDetails().get("domainCodename"));
        assertEquals("testdomain10", responses.get(20).getDetails().get("domainCodename"));
        assertEquals("testdomain11", responses.get(22).getDetails().get("domainCodename"));
    }

}
