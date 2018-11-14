package net.geant.nmaas.portal.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.persistent.entity.Content;
import net.geant.nmaas.portal.persistent.repositories.ContentRepository;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

@Service
@Slf4j
public class ContentServiceImpl implements net.geant.nmaas.portal.service.ContentService {

	ContentRepository contentRepo;
//    shell/data/i18n/en.json src/test/shell/data/i18n/en.json
	@Value("classpath:data/i18n/en.json")
	private Resource englishContent;

	@Value("classpath:data/i18n/fr.json")
	private Resource frenchContent;

	@Value("classpath:data/i18n/pl.json")
	private Resource polishContent;

	@Autowired
	public ContentServiceImpl(ContentRepository repository){
		this.contentRepo = repository;
	}

	@Override
	public Optional<Content> findByName(String name){
		return (name != null ? contentRepo.findByName(name) : Optional.empty());
	}

	@Override
	public Optional<Content> findById(Long id){
		return (id != null ? contentRepo.findById(id) : Optional.empty());
	}

	@Override
	public Content createNewContentRecord(String name, String content, String title) throws ObjectAlreadyExistsException{
		checkParam(name);
		Optional<Content> cnt = contentRepo.findByName(name);
		if(cnt.isPresent()){
			throw new ObjectAlreadyExistsException("Content with this name exists.");
		}
		Content newContent = new Content(name, title, content);
		return contentRepo.save(newContent);
	}

	@Override
	public void update(Content content) throws ProcessingException{
		checkParam(content);
		checkParam(content.getId());

		if(!contentRepo.existsById(content.getId())){
			throw new ProcessingException("Content (id=" + content.getId() + ") does not exists.");
		}

		contentRepo.saveAndFlush(content);

	}

	@Override
	public void delete(Content content) throws MissingElementException, ProcessingException{
		checkParam(content);
		checkParam(content.getId());

		if(!contentRepo.existsById(content.getId())){
			throw new ProcessingException("Content (id=" + content.getId() + ") does not exists.");
		}
		contentRepo.delete(content);
	}

	private void checkParam(Long id) {
		if(id == null)
			throw new IllegalArgumentException("id is null");
	}

	private void checkParam(String name) {
		if(name == null)
			throw new IllegalArgumentException("name is null");
	}

	private void checkParam(Content content) {
		if(content == null)
			throw new IllegalArgumentException("content is null");
	}

    public String getContent(String language, String root, String key) {
        String value = "";
        try {
            JSONParser jsonParser = new JSONParser();
            Object object = null;
            switch (language) {
                case "en":
                    object = jsonParser.parse(readAsString(englishContent));
                case "fr":
                    object = jsonParser.parse(readAsString(frenchContent));
                case "pl":
                    object = jsonParser.parse(readAsString(polishContent));
            }
            JSONObject jsonObject = (JSONObject) object;
            JSONObject rootJSONObject = Optional.of((JSONObject) jsonObject.get(root))
                    .orElse(new JSONObject());
            value = Optional.of((String) ((JSONObject) rootJSONObject.get(root)).get(key))
                    .orElse("");
        } catch (ParseException | IOException e) {
            log.error("Error happened while parsing the language " + language + e.getMessage());
        }
        return value;
    }

	private String readAsString(Resource resource) throws IOException {
		return new String(Files.readAllBytes(resource.getFile().toPath()));
	}
}
