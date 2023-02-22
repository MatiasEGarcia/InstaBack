package com.instaJava.instaJava.service;

import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.request.PersonalDetailsDto;

public interface UserService {

	void updateImage(MultipartFile file); //will return the new image
	
	byte[] getImage();
	
	PersonalDetailsDto savePersonalDetails(PersonalDetailsDto personalDetailsDto);
	
	PersonalDetailsDto getPersonalDetailsByUser();
}
