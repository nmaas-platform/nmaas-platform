package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.bulk.CsvApplication;
import net.geant.nmaas.portal.api.bulk.CsvDomain;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BulkCsvProcessor {

    boolean isCSVFormat(MultipartFile file);

    List<CsvDomain> processDomainSpecs(MultipartFile file);

    List<CsvApplication> processApplicationSpecs(MultipartFile file);

}