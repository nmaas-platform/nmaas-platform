package net.geant.nmaas.portal.service;

import java.util.List;
import net.geant.nmaas.portal.api.i18n.api.I18nBaseDto;
import net.geant.nmaas.portal.api.i18n.api.I18nDto;

public interface InternationalizationService {
    void addNewLanguage(I18nDto newLanguage, Boolean force);
    void updateLanguage(String language, String content);
    List<I18nBaseDto> getAllSupportedLanguages();
    I18nDto getLanguage(String language);
    void changeLanguageState(I18nBaseDto language);
    String getLanguageContent(String language);
    List<String> getEnabledLanguages();
}
