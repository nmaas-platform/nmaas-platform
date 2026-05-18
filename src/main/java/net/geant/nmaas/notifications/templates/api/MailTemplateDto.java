package net.geant.nmaas.notifications.templates.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.notifications.templates.MailType;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MailTemplateDto {

    private MailType mailType;

    private Map<String, String> globalInformation;

    private List<LanguageMailContentDto> templates;

}
