package net.geant.nmaas.portal.api.i18n.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LanguageView {

    private String language;

    private boolean enabled;
}
