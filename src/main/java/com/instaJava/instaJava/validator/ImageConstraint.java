package com.instaJava.instaJava.validator;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 
 * @author matia
 *	Validate that image isn't null or empty and if the content type is acceptable.
 *
 */
public class ImageConstraint implements ConstraintValidator<Image,MultipartFile> {

	@Override
	public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
		if(file.isEmpty() || file == null) return false;
		String contentType = file.getContentType();
		if(!isSupportedContentType(contentType)) return false;
		return true;
	}

	
	private boolean isSupportedContentType(String contentType) {
		return  contentType.equals("image/png")
                || contentType.equals("image/jpg")
                || contentType.equals("image/jpeg");
	}
	
}
