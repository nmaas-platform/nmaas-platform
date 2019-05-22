package net.geant.nmaas.portal.service;

import java.util.List;
import net.geant.nmaas.portal.api.i18n.api.InternationalizationBriefView;
import net.geant.nmaas.portal.api.i18n.api.InternationalizationView;

public interface InternationalizationService {
    void addNewLanguage(InternationalizationView newLanguage);
    void updateLanguage(String language, String content);
    List<InternationalizationBriefView> getAllSupportedLanguages();
    InternationalizationView getLanguage(String language);
    void changeLanguageState(InternationalizationBriefView language);
    String getLanguageContent(String language);
    List<String> getEnabledLanguages();
}
