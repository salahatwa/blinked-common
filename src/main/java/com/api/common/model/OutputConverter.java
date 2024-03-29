package com.api.common.model;

import static com.api.common.utils.BeanUtils.updateProperties;

import org.springframework.lang.NonNull;

/**
 * Converter interface for output DTO.
 *
 * <b>The implementation type must be equal to DTO type</b>
 *
 * @param <DTO>    the implementation class type
 * @param <DOMAIN> domain type
 * @author ssatwa
 */
public interface OutputConverter<DTO extends OutputConverter<DTO, DOMAIN>, DOMAIN> {

	/**
	 * Convert from domain.(shallow)
	 *
	 * @param domain domain data
	 * @return converted dto data
	 */
	@SuppressWarnings("unchecked")
	@NonNull
	default <T extends DTO> T convertFrom(@NonNull DOMAIN domain) {

		updateProperties(domain, this);

		return (T) this;
	}
}
