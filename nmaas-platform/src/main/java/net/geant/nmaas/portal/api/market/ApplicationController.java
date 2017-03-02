package net.geant.nmaas.portal.api.market;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.AbstractConverter;
import org.modelmapper.AbstractProvider;
import org.modelmapper.Converter;
import org.modelmapper.Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.geant.nmaas.portal.api.domain.Application;
import net.geant.nmaas.portal.api.domain.ApplicationBrief;
import net.geant.nmaas.portal.api.domain.Id;
import net.geant.nmaas.portal.api.domain.UserFile;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.StorageException;
import net.geant.nmaas.portal.persistent.entity.Tag;
import net.geant.nmaas.portal.persistent.repositories.TagRepository;
import net.geant.nmaas.portal.service.FileStorageService;

@RestController
@RequestMapping("/portal/api/apps")
public class ApplicationController extends AppBaseController {
	
	@RequestMapping(method=RequestMethod.GET)
	public List<ApplicationBrief> getApplications() {
		return appRepo.findAll().stream().map(app -> modelMapper.map(app, ApplicationBrief.class)).collect(Collectors.toList());
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_MANAGER')")
	@RequestMapping(method=RequestMethod.POST)
	public Id addApplication(@RequestBody(required=true) ApplicationBrief appRequest) {
		net.geant.nmaas.portal.persistent.entity.Application app;
		
		app = modelMapper.map(appRequest, net.geant.nmaas.portal.persistent.entity.Application.class);
		appRepo.save(app);
		
		return new Id(app.getId());
	}
	
	@RequestMapping(value="/{appId}", method=RequestMethod.GET)
	public Application getApplication(@PathVariable(value = "appId", required=true) Long id) throws MissingElementException {
		net.geant.nmaas.portal.persistent.entity.Application app = getApp(id); 
		return modelMapper.map(appRepo.getOne(id), Application.class);
	}
	
}
