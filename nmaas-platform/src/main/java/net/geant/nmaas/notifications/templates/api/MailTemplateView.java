package net.geant.nmaas.notifications.templates.api;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.notifications.templates.MailType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MailTemplateView {

    private MailType mailType;

    private Map<String, String> globalInformation;

    private List<LanguageMailContentView> templates;

}
