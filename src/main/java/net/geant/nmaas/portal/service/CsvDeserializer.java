package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.csv.CsvBean;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CsvDeserializer {
    boolean isCSVFormat(MultipartFile file);

    List<CsvBean> deserializeCSV(MultipartFile file, Class givenClass) throws IOException;
}
