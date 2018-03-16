package net.geant.nmaas.portal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.geant.nmaas.portal.service.impl.DomainServiceImpl.CodenameValidator;
import net.geant.nmaas.portal.service.impl.domains.DefaultCodenameValidator;

@Configuration
public class ValidatorsConfig {
	
	@Bean
	CodenameValidator defaultCodenameValidator(@Value("${nmaas.portal.domains.codename.pattern}") String pattern) {
		return new DefaultCodenameValidator(pattern);
	}
}
