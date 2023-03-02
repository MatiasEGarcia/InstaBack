package com.instaJava.instaJava.service;

import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.PersonalDetailsDto;

public interface UserService {

	void updateImage(MultipartFile file); //will return the new image
	
	String getImage();
	
	PersonalDetailsDto savePersonalDetails(PersonalDetailsDto personalDetailsDto);
	
	PersonalDetailsDto getPersonalDetailsByUser();
}
