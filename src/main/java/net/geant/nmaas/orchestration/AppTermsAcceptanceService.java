package net.geant.nmaas.orchestration;

import lombok.AllArgsConstructor;
import net.geant.nmaas.orchestration.entities.AppTermsAcceptance;
import net.geant.nmaas.orchestration.repositories.AppTermsAcceptanceRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@AllArgsConstructor
public class AppTermsAcceptanceService {

    private final AppTermsAcceptanceRepository repository;

    public void addTermsAcceptanceEntry(String applicationName, Long applicationId, String username, String termsContent, String termsAcceptanceStatement, OffsetDateTime date) {
        AppTermsAcceptance entry = AppTermsAcceptance.builder()
                .appName(applicationName)
                .username(username)
                .termsAcceptanceStatement(termsAcceptanceStatement)
                .termsContent(termsContent)
                .acceptanceDate(date)
                .build();

        repository.save(entry);
    }
}
