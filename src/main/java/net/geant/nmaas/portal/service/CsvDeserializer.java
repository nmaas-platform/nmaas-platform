package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.bulk.CsvBean;
import net.geant.nmaas.portal.api.bulk.CsvReplay;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CsvDeserializer {
    boolean isCSVFormat(MultipartFile file);

    List<CsvReplay> deserializeCSV(MultipartFile file, Class givenClass) throws IOException;
}
