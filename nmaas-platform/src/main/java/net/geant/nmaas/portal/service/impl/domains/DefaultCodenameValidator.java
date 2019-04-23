package net.geant.nmaas.portal.service.impl.domains;

import java.util.Optional;
import java.util.regex.Pattern;

import net.geant.nmaas.portal.service.impl.DomainServiceImpl.CodenameValidator;

public class DefaultCodenameValidator implements CodenameValidator {

	private String pattern;
	
	public DefaultCodenameValidator(String pattern) {
		super();
		this.pattern = pattern;
	}

	@Override
	public boolean valid(String codename) {
		return Optional.of(pattern)
						.map(pattern -> (pattern.trim().length() <= 0 || Pattern.matches(pattern, codename)))
						.orElse(true);
	}

}
