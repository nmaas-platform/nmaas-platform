package net.geant.nmaas.portal.api.domain.converters;

import lombok.NoArgsConstructor;
import org.modelmapper.AbstractConverter;

import net.geant.nmaas.portal.persistent.entity.Tag;

@NoArgsConstructor
public class TagInverseConverter extends AbstractConverter<Tag, String> {

	@Override
	protected String convert(Tag source) {
		return (source != null ? source.getName() : null);
	}
}
