package com.instaJava.instaJava.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.PersonalDetailsDto;
import com.instaJava.instaJava.entity.PersonalDetails;
import com.instaJava.instaJava.entity.User;

public interface UserService {

	void updateImage(MultipartFile file); //will return the new image
	
	String getImage();
	
	PersonalDetails getPersonalDetailsByUser();
	
	PersonalDetails savePersonalDetails(PersonalDetailsDto personalDetailsDto);
	
	List<User> findByUsernameLike(String username,int limit);
	
	boolean existsByUsername(String username);
}
