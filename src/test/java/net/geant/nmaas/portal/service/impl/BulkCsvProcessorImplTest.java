package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.bulk.CsvApplication;
import net.geant.nmaas.portal.service.BulkCsvProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BulkCsvProcessorImplTest {

    private final BulkCsvProcessor processor = new BulkCsvProcessorImpl();

    private static final String csvContent =
        "domain,instance,version,param.juiceshop.properties.ctfKey\n" +
        "Customer 1,jsh50,14.5.1,OAXNjGDP9dHASMEH2shSCMFp\n" +
        "Customer 3,jsh51,14.5.1,AOXNjGDP9dHASMEH2shSCMFp";

    @Test
    void shouldProcessApplicationSpec() throws IOException {
        MockMultipartFile multipartFile = new MockMultipartFile("appSpec", "appSpec.csv", "text/csv", csvContent.getBytes());
        assertThat(processor.isCSVFormat(multipartFile)).isTrue();
        List<CsvApplication> parsed = processor.processApplicationSpecs(multipartFile);
        assertThat(parsed.size()).isEqualTo(2);
        assertThat(parsed).extracting("domainName").containsExactly("Customer 1", "Customer 3");
        assertThat(parsed).extracting("applicationInstanceName").containsExactly("jsh50", "jsh51");
        assertThat(parsed).extracting("applicationVersion").containsExactly("14.5.1", "14.5.1");
        assertThat(parsed.get(0).getParameters().get("param.juiceshop.properties.ctfKey")).contains("OAXNjGDP9dHASMEH2shSCMFp");
        assertThat(parsed.get(1).getParameters().get("param.juiceshop.properties.ctfKey")).contains("AOXNjGDP9dHASMEH2shSCMFp");
    }

}
