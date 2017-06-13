package net.geant.nmaas.portal.api.domain.converters;

import org.modelmapper.AbstractConverter;

import net.geant.nmaas.portal.persistent.entity.Tag;

public class TagInverseConverter extends AbstractConverter<Tag, String> {

	public TagInverseConverter() {
	}

	@Override
	protected String convert(Tag source) {
		return (source != null ? source.getName() : null);
	}
}
