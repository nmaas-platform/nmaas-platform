package net.geant.nmaas.notifications.templates;

import static com.google.common.base.Preconditions.checkArgument;
import java.util.stream.Collectors;
import net.geant.nmaas.notifications.templates.api.LanguageMailContentView;
import net.geant.nmaas.notifications.templates.api.MailTemplateView;
import net.geant.nmaas.notifications.templates.entities.LanguageMailContent;
import net.geant.nmaas.notifications.templates.entities.MailTemplate;
import net.geant.nmaas.notifications.templates.repository.MailTemplateRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TemplateService {

    private ModelMapper modelMapper;

    private MailTemplateRepository repository;

    @Autowired
    public TemplateService(MailTemplateRepository repository, ModelMapper modelMapper){
        this.modelMapper = modelMapper;
        this.repository = repository;
    }

    @Transactional
    public MailTemplateView getMailTemplate(MailType mailType){
        MailTemplate mailTemplate = repository.findByMailType(mailType).orElseThrow(() -> new IllegalArgumentException("Mail template not found"));
        return modelMapper.map(mailTemplate, MailTemplateView.class);
    }

    void saveMailTemplate(MailTemplateView mailTemplate){
        checkArgument(!repository.existsByMailType(mailTemplate.getMailType()),"Mail template already exists");
        checkArgument(mailTemplate.getTemplates() != null && !mailTemplate.getTemplates().isEmpty(), "Mail template cannot be null or empty");
        repository.save(modelMapper.map(mailTemplate, MailTemplate.class));
    }

    void updateMailTemplate(MailTemplateView mailTemplate){
        MailTemplate mailTemplateEntity = repository.findByMailType(mailTemplate.getMailType()).orElseThrow(() -> new IllegalArgumentException("Mail template not found"));
        checkArgument(mailTemplate.getTemplates() != null && !mailTemplate.getTemplates().isEmpty(), "Mail template cannot be null or empty");
        mailTemplateEntity.setTemplates(mailTemplate.getTemplates().stream().map(template -> modelMapper.map(template, LanguageMailContent.class)).collect(Collectors.toList()));
        repository.save(mailTemplateEntity);
    }

}
