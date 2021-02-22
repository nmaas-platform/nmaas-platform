package net.geant.nmaas.notifications.templates.api;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LanguageMailContentView {

    private String language;

    private String subject;

    private Map<String, String> template;
}
