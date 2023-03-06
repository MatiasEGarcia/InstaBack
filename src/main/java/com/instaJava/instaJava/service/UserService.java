package com.instaJava.instaJava.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.PersonalDetailsDto;
import com.instaJava.instaJava.dto.response.ResUser;

public interface UserService {

	void updateImage(MultipartFile file); //will return the new image
	
	String getImage();
	
	PersonalDetailsDto savePersonalDetails(PersonalDetailsDto personalDetailsDto);
	
	PersonalDetailsDto getPersonalDetailsByUser();
	
	List<ResUser> findByUsernameLike(String username,int limit);
}
