package net.geant.nmaas.portal.api.bulk;


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
@RequestMapping("/api/bulks")
public class BulkController {

    private CsvDeserializer csvDeserializer;

    @PostMapping("/users")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<List<CsvReplay>> uploadUser(@RequestParam("file")MultipartFile file) {

        List<CsvReplay> result = new ArrayList<>();

        if(csvDeserializer.isCSVFormat(file)) {
            try {
                result = this.csvDeserializer.deserializeCSV(file, CsvDomain.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/apps")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<List<CsvReplay>> uploadApp(@RequestParam("file")MultipartFile file) {

        List<CsvReplay> result = new ArrayList<>();

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
