package com.instaJava.instaJava.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IsEnumConstraint implements ConstraintValidator<IsEnum,String>{

	private Class<?> enumSource;
	
	@Override
	public void initialize(IsEnum isEnum) {
		this.enumSource = isEnum.enumSource();
	}
	
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		Object[] constants = enumSource.getEnumConstants();
		for(Object constant : constants){
			if(value.equalsIgnoreCase(constant.toString())) return true;
		};
		return false;
	}

}
