package net.geant.nmaas.portal.api.i18n.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InternationalizationView extends InternationalizationBriefView {

    private String content;

    public InternationalizationView(String language, boolean enabled, String content){
        super(enabled, language);
        this.content = content;
    }

}
