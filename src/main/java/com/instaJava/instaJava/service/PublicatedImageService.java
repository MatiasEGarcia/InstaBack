package com.instaJava.instaJava.service;

import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.response.ResPublicatedImage;
import com.instaJava.instaJava.entity.PublicatedImage;

public interface PublicatedImageService {
	
	PublicatedImage save(String Description,MultipartFile file);
	
	void deleteById(Long id);
}
