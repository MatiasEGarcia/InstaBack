package com.instaJava.instaJava.service;

import org.springframework.web.multipart.MultipartFile;

public interface UserService {

	void updateImage(MultipartFile file); //will return the new image
	
	byte[] getImage();
}
