package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.bulk.CsvApplication;
import net.geant.nmaas.portal.api.bulk.CsvDomain;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface BulkCsvProcessor {

    boolean isCSVFormat(MultipartFile file);

    List<CsvDomain> processDomainSpecs(MultipartFile file) throws IOException;

    List<CsvApplication> processApplicationSpecs(MultipartFile file) throws IOException;

}