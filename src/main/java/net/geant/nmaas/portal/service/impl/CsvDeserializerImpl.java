package net.geant.nmaas.portal.service.impl;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.csv.CsvApplication;
import net.geant.nmaas.portal.api.csv.CsvBean;
import net.geant.nmaas.portal.service.CsvDeserializer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class CsvDeserializerImpl implements CsvDeserializer {

    public static String TYPE_CSV = "text/csv";


    public List<CsvBean> deserializeCSV(MultipartFile file, Class givenClass) throws IOException {

        CSVParser csvParser = new CSVParserBuilder().withSeparator(';').build();
        File fileLocal = new File("src/tmp.csv");
        try(OutputStream os = new FileOutputStream(fileLocal)) {
            os.write(file.getBytes());
        }
        log.error("Transfered file " +fileLocal.getName() + " with " + fileLocal.getAbsolutePath() + " " + fileLocal.canRead());

        try {
            List<CsvBean> resultInClass = beanBuilderExample(fileLocal.toPath(), givenClass);
            resultInClass.forEach(x -> System.out.println(x.toString()));
            return resultInClass;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

//        try(CSVReader reader = new CSVReaderBuilder(
//                new FileReader(fileLocal))
//                .withCSVParser(csvParser)   // custom CSV parser
//                .withSkipLines(0)           // skip the first line, header info
//                .build()){
//            List<String[]> r = reader.readAll();
//            r.forEach(x -> System.out.println(Arrays.toString(x)));
//        } catch (CsvException e) {
//            throw new RuntimeException(e);
//        }
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
