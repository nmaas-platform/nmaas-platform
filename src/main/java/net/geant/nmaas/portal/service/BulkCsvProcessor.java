package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.bulk.CsvBean;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface BulkCsvProcessor {

    boolean isCSVFormat(MultipartFile file);

    List<CsvBean> process(MultipartFile file, Class givenClass) throws IOException;

}