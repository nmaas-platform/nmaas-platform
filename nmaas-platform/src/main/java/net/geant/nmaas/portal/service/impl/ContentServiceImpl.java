package net.geant.nmaas.portal.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.persistent.entity.Content;
import net.geant.nmaas.portal.persistent.entity.Internationalization;
import net.geant.nmaas.portal.persistent.repositories.ContentRepository;
import net.geant.nmaas.portal.persistent.repositories.InternationalizationRepository;
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

	private ContentRepository contentRepository;

	private InternationalizationRepository internationalizationRepository;

	@Autowired
	public ContentServiceImpl(ContentRepository contentRepository, InternationalizationRepository internationalizationRepository) {
		this.contentRepository = contentRepository;
		this.internationalizationRepository = internationalizationRepository;

	}

	@Override
	public Optional<Content> findByName(String name){
		return (name != null ? contentRepository.findByName(name) : Optional.empty());
	}

	@Override
	public Optional<Content> findById(Long id){
		return (id != null ? contentRepository.findById(id) : Optional.empty());
	}

	@Override
	public Content createNewContentRecord(String name, String content, String title) throws ObjectAlreadyExistsException{
		checkParam(name);
		Optional<Content> cnt = contentRepository.findByName(name);
		if(cnt.isPresent()){
			throw new ObjectAlreadyExistsException("Content with this name exists.");
		}
		Content newContent = new Content(name, title, content);
		return contentRepository.save(newContent);
	}

	@Override
	public void update(Content content) throws ProcessingException{
		checkParam(content);
		checkParam(content.getId());

		if(!contentRepository.existsById(content.getId())){
			throw new ProcessingException("Content (id=" + content.getId() + ") does not exists.");
		}

		contentRepository.saveAndFlush(content);

	}

	@Override
	public void delete(Content content) throws MissingElementException, ProcessingException{
		checkParam(content);
		checkParam(content.getId());

		if(!contentRepository.existsById(content.getId())){
			throw new ProcessingException("Content (id=" + content.getId() + ") does not exists.");
		}
		contentRepository.delete(content);
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

	@Override
	public String getContent(String language, String root, String key) {
		Optional<Internationalization> internationalizationOptional = internationalizationRepository.findByLanguageOrderByIdDesc(language);
		return internationalizationOptional.map(internationalization -> {
			String value = "";
			try {
				JSONParser jsonParser = new JSONParser();
				Object object = jsonParser.parse(internationalization.getContent());
				JSONObject jsonObject = (JSONObject) object;
				JSONObject rootJSONObject = Optional.ofNullable((JSONObject) jsonObject.get(root))
						.orElse(new JSONObject());
				value = (String)Optional.ofNullable(rootJSONObject.get(key))
						.orElse("Enexpected error");
			} catch (ParseException e) {
				log.error("Error happened while parsing the content - " + e.getMessage());
			}
			return value;
		}).orElse("Enexpected error - invalid language");
	}

	private String readAsString(Resource resource) throws IOException {
		return new String(Files.readAllBytes(resource.getFile().toPath()));
	}
}
