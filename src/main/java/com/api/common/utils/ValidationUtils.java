package com.api.common.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.FieldError;

/**
 * Object validation utilities.
 *
 * @author ssatwa
 * @date 03/29/19
 */
public class ValidationUtils {

	private static volatile Validator VALIDATOR;

	private ValidationUtils() {
	}

	/**
	 * Gets validator, or create it.
	 *
	 * @return validator
	 */
	@NonNull
	public static Validator getValidator() {
		if (VALIDATOR == null) {
			synchronized (ValidationUtils.class) {
				if (VALIDATOR == null) {
					// Init the validation
					VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
				}
			}
		}

		return VALIDATOR;
	}

	/**
	 * Validates bean by hand.
	 *
	 * @param obj    bean to be validated
	 * @param groups validation group
	 * @throws ConstraintViolationException throw if validation failure
	 */
	public static void validate(Object obj, Class<?>... groups) {

		Validator validator = getValidator();

		if (obj instanceof Iterable) {
			// validate for iterable
			validate((Iterable<?>) obj, groups);
		} else {
			// validate the non-iterable object
			Set<ConstraintViolation<Object>> constraintViolations = validator.validate(obj, groups);

			if (!CollectionUtils.isEmpty(constraintViolations)) {
				// If contain some errors then throw constraint violation exception
				throw new ConstraintViolationException(constraintViolations);
			}
		}
	}

	/**
	 * Validates iterable objects.
	 *
	 * @param objs   iterable objects could be null
	 * @param groups validation groups
	 */
	public static void validate(@Nullable Iterable<?> objs, @Nullable Class<?>... groups) {
		if (objs == null) {
			return;
		}

		// get validator
		Validator validator = getValidator();

		// wrap index
		AtomicInteger i = new AtomicInteger(0);
		final Set<ConstraintViolation<?>> allViolations = new LinkedHashSet<>();
		objs.forEach(obj -> {
			int index = i.getAndIncrement();
			Set<? extends ConstraintViolation<?>> violations = validator.validate(obj, groups);
			violations.forEach(violation -> {
				Path path = violation.getPropertyPath();
//				if (path instanceof PathImpl) {
//					PathImpl pathImpl = (PathImpl) path;
//					pathImpl.makeLeafNodeIterableAndSetIndex(index);
//				}
				allViolations.add(violation);
			});
		});
		if (!CollectionUtils.isEmpty(allViolations)) {
			throw new ConstraintViolationException(allViolations);
		}
	}

	/**
	 *
	 * @param constraintViolations constraint violations(contain error information)
	 * @return error detail map
	 */
	@NonNull
	public static Map<String, String> mapWithValidError(Set<ConstraintViolation<?>> constraintViolations) {
		if (CollectionUtils.isEmpty(constraintViolations)) {
			return Collections.emptyMap();
		}

		Map<String, String> errMap = new HashMap<>(4);
		// Format the error message
		constraintViolations.forEach(constraintViolation -> errMap.put(constraintViolation.getPropertyPath().toString(),
				constraintViolation.getMessage()));
		return errMap;
	}

	/**
	 * Convert field validation errors to standard map type, key:value =
	 * field:message
	 *
	 * @param fieldErrors field error group
	 * @return If null is returned, it means that no error occurred
	 */
	public static Map<String, String> mapWithFieldError(@Nullable List<FieldError> fieldErrors) {
		if (CollectionUtils.isEmpty(fieldErrors)) {
			return Collections.emptyMap();
		}

		Map<String, String> errMap = new HashMap<>(4);
		fieldErrors.forEach(filedError -> errMap.put(filedError.getField(), filedError.getDefaultMessage()));
		return errMap;
	}
}
