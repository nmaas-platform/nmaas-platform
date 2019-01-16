package net.geant.nmaas.notifications.templates.api;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.notifications.templates.api.LanguageMailContentView;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MailTemplateView {

    private MailType mailType;

    private List<LanguageMailContentView> templates;

}
