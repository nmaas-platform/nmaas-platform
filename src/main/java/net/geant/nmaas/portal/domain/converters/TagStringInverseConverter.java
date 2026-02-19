package net.geant.nmaas.portal.domain.converters;

import lombok.NoArgsConstructor;
import org.modelmapper.AbstractConverter;

import net.geant.nmaas.portal.persistence.entity.Tag;

@NoArgsConstructor
public class TagStringInverseConverter extends AbstractConverter<Tag, String> {

	@Override
	protected String convert(Tag source) {
		return (source != null ? source.getName() : null);
	}
}
