package com.instaJava.instaJava.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

//Will ask if the String is equals to a value in some enum
@Constraint(validatedBy = {IsEnumConstraint.class})
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.FIELD,ElementType.PARAMETER})
public @interface IsEnum {

	String message() default "{vali.string-no-valid}";
	
	Class<?> enumSource();

	public Class<?>[] groups() default {};

    public Class<? extends Payload>[] payload() default {};
}
