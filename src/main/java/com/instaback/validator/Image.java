package com.instaback.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = {ImageConstraint.class})
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.FIELD,ElementType.PARAMETER})
public @interface Image {

	String message() default "{vali.image}";
	
	public Class<?>[] groups() default {};

    public Class<? extends Payload>[] payload() default {};
}
