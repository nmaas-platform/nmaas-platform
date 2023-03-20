package net.geant.nmaas.portal.api.csv;


import lombok.AllArgsConstructor;
import net.geant.nmaas.portal.service.CsvDeserializer;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/csv")
public class CsvReaderController {

    private CsvDeserializer csvDeserializer;

    @PostMapping("/upload/users")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<List<CsvBean>> uploadUser(@RequestParam("file")MultipartFile file) {

        List<CsvBean> result = new ArrayList<>();

        if(csvDeserializer.isCSVFormat(file)) {
            try {
                result = this.csvDeserializer.deserializeCSV(file, CsvUser.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/upload/app")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<List<CsvBean>> uploadApp(@RequestParam("file")MultipartFile file) {

        List<CsvBean> result = new ArrayList<>();

        if(csvDeserializer.isCSVFormat(file)) {
            try {
                result = this.csvDeserializer.deserializeCSV(file, CsvApplication.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseEntity.ok(result);
    }
}
