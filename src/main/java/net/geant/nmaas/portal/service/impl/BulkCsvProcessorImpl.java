package net.geant.nmaas.portal.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.bulk.CsvApplication;
import net.geant.nmaas.portal.api.bulk.CsvDomain;
import net.geant.nmaas.portal.api.exception.ProcessingException;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkCsvProcessorImpl implements BulkCsvProcessor {

    public static final String TYPE_CSV = "text/csv";

    private static final List<String> requiredDomainHeader = List.of("domain", "username", "email");

    private static final List<String> requireApplicationHeader = List.of("domain", "instance", "version");


    @Override
    public List<CsvDomain> processDomainSpecs(MultipartFile file) throws IOException {
        List<CsvDomain> result = process(file, CsvDomain.class).stream().map(d -> (CsvDomain) d).collect(Collectors.toList());
        if (result.isEmpty()) {
            throw new ProcessingException("CSV file contains 0 entries");
        }
        return result;
    }

    @Override
    public List<CsvApplication> processApplicationSpecs(MultipartFile file) throws IOException {
        List<CsvApplication> result = process(file, CsvApplication.class).stream().map(d -> (CsvApplication) d).collect(Collectors.toList());
        if (result.isEmpty()) {
            throw new ProcessingException("CSV file contains 0 entries");
        }
        return result;
    }

    /**
     * Read CSV file and map it to given class type
     *
     * @param file       an MultipartFile CSV from controller
     * @param outputType an CSVClass created for reader of CSV file (used to map fields)
     * @throws IOException thrown when provided file is invalid
     */
    private static List<CsvBean> process(MultipartFile file, Class outputType) throws IOException {

        File tempFile = File.createTempFile("nmaas", "-csv");
        try (OutputStream os = new FileOutputStream(tempFile)) {
            os.write(file.getBytes());
        }

        try {
            return beanBuilderExample(tempFile.toPath(), outputType);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<CsvBean> beanBuilderExample(Path path, Class clazz) throws Exception {

        try (Reader reader = Files.newBufferedReader(path)) {
            CSVReader csvReader = new CSVReader(reader);
            validateHeaderColumns(csvReader.readNext());
        }

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

    public interface CsvBean {
    }

    private static void validateHeaderColumns(String[] headers) {
        log.debug("List = {}", List.of(headers));

        if (headers.length == 0) {
            throw new ProcessingException("CSV headers are empty");
        }

        if (Arrays.asList(headers).containsAll(requiredDomainHeader) || Arrays.asList(headers).containsAll(requireApplicationHeader)) {
            log.debug("Nagłówki są poprawne");
        } else {
            throw new ProcessingException("CSV headers missing required column");
        }
    }

}
