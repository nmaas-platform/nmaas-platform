package net.geant.nmaas.portal.service.impl;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.bulk.CsvBean;
import net.geant.nmaas.portal.service.BulkCsvProcessor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkCsvProcessorImpl implements BulkCsvProcessor {

    public static final String TYPE_CSV = "text/csv";

    /**
     * Read CSV file and map it to given class type
     * @param file an MultipartFile CSV from controller
     * @param givenClass an CSVClass created for reader of CSV file (used to map fields)
     * @throws IOException thrown when provided file is invalid
     */
    public List<CsvBean> process(MultipartFile file, Class givenClass) throws IOException {

        File tempFile = File.createTempFile("Nmaas", "-csv");
        try (OutputStream os = new FileOutputStream(tempFile)) {
            os.write(file.getBytes());
        }

        try {
            return beanBuilderExample(tempFile.toPath(), givenClass);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<CsvBean> beanBuilderExample(Path path, Class clazz) throws Exception {
        try (Reader reader = Files.newBufferedReader(path)) {
            CsvToBean cb = new CsvToBeanBuilder<CsvBean>(reader)
                    .withType(clazz)
                    .build();

            return cb.parse();
        }
    }

    public boolean isCSVFormat(MultipartFile file) {
        return TYPE_CSV.equals(file.getContentType());
    }

}
