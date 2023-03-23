package com.instaJava.instaJava.validator;

import java.lang.reflect.Field;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IsFieldConstraint implements ConstraintValidator<IsField,String> {

	private Class<?> classSource;
	
	@Override
	public void initialize(IsField isField) {
		this.classSource = isField.classSource();
	}
	
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		Field[] fields = classSource.getDeclaredFields();
		for(Field field : fields) {
			if(field.getName().equalsIgnoreCase(value)) return true;
		}
		
		return false;
	}

}
