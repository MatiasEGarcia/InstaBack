package com.instaJava.instaJava.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = {IsFieldConstraint.class})
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.FIELD,ElementType.PARAMETER})
public @interface IsField {

	String message() default "{vali.wrong-field-name}";
	
	Class<?> classSource();
	
	public Class<?>[] groups() default {};

    public Class<? extends Payload>[] payload() default {};
}
