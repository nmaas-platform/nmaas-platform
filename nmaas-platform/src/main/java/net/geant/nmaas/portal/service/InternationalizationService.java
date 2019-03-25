package net.geant.nmaas.portal.service;

import java.util.List;
import net.geant.nmaas.portal.api.i18n.api.InternationalizationBriefView;
import net.geant.nmaas.portal.api.i18n.api.InternationalizationView;

public interface InternationalizationService {
    void addNewLanguage(InternationalizationView newLanguage);
    List<InternationalizationBriefView> getAllSupportedBriefLanguages();
    List<InternationalizationView> getAllSupportedLanguages();
    void changeLanguageState(InternationalizationBriefView language);
    String getLanguage(String language);
    List<String> getEnabledLanguages();
}
