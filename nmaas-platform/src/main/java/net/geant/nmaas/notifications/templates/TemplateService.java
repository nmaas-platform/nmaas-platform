package net.geant.nmaas.notifications.templates;

import freemarker.template.Configuration;
import freemarker.template.Template;
import net.geant.nmaas.notifications.MailTemplateElements;
import net.geant.nmaas.notifications.templates.api.MailTemplateView;
import net.geant.nmaas.notifications.templates.entities.LanguageMailContent;
import net.geant.nmaas.notifications.templates.entities.MailTemplate;
import net.geant.nmaas.notifications.templates.repository.MailTemplateRepository;
import net.geant.nmaas.portal.persistent.entity.FileInfo;
import net.geant.nmaas.portal.service.impl.LocalFileStorageService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

@Service
public class TemplateService {

    private ModelMapper modelMapper;

    private MailTemplateRepository repository;

    private LocalFileStorageService fileStorageService;

    @Autowired
    public TemplateService(MailTemplateRepository repository, LocalFileStorageService fileStorageService, ModelMapper modelMapper){
        this.modelMapper = modelMapper;
        this.repository = repository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public MailTemplateView getMailTemplate(MailType mailType){
        MailTemplate mailTemplate = repository.findByMailType(mailType).orElseThrow(() -> new IllegalArgumentException("Mail template not found"));
        return modelMapper.map(mailTemplate, MailTemplateView.class);
    }

    List<MailTemplateView> getMailTemplates(){
        return this.repository.findAll().stream()
                .map(mailTemplate -> modelMapper.map(mailTemplate, MailTemplateView.class))
                .collect(Collectors.toList());
    }

    void saveMailTemplate(MailTemplateView mailTemplate){
        checkArgument(!repository.existsByMailType(mailTemplate.getMailType()),"Mail template already exists");
        checkArgument(mailTemplate.getTemplates() != null && !mailTemplate.getTemplates().isEmpty(), "Mail template cannot be null or empty");
        repository.save(modelMapper.map(mailTemplate, MailTemplate.class));
    }

    void updateMailTemplate(MailTemplateView mailTemplate){
        MailTemplate mailTemplateEntity = repository.findByMailType(mailTemplate.getMailType()).orElseThrow(() -> new IllegalArgumentException("Mail template not found"));
        checkArgument(mailTemplate.getTemplates() != null && !mailTemplate.getTemplates().isEmpty(), "Mail template cannot be null or empty");
        mailTemplateEntity.getTemplates().clear();
        mailTemplateEntity.getTemplates().addAll(mailTemplate.getTemplates().stream().map(template -> modelMapper.map(template, LanguageMailContent.class)).collect(Collectors.toList()));
        repository.save(mailTemplateEntity);
    }

    void storeHTMLTemplate(MultipartFile file){
        checkArgument(file != null && !file.isEmpty(), "HTML template cannot be null or empty");
        checkArgument(Objects.equals(file.getContentType(), MailTemplateElements.HTML_TYPE), "HTML template must be in html format");
        checkArgument(fileStorageService.getFileInfoByContentType(MailTemplateElements.HTML_TYPE).isEmpty(), "Only one HTML template is supported.");
        fileStorageService.store(file);
    }

    void updateHTMLTemplate(MultipartFile file){
        FileInfo fileInfo = getHTMLTemplateFileInfo();
        if(fileStorageService.remove(fileInfo)){
            storeHTMLTemplate(file);
        }
    }

    public Template getHTMLTemplate() throws IOException {
        File template = fileStorageService.getFile(getHTMLTemplateFileInfo().getId());
        return new Template(template.getName(), new FileReader(template), new Configuration(Configuration.VERSION_2_3_28));
    }

    private FileInfo getHTMLTemplateFileInfo(){
        List<FileInfo> template = fileStorageService.getFileInfoByContentType(MailTemplateElements.HTML_TYPE);
        if(template.size() == 1){
            return template.get(0);
        }
        throw new IllegalArgumentException(String.format("Only one html template supported (actually got %d)", template.size()));
    }

}
