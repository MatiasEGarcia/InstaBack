package com.instaJava.instaJava.service;

import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.response.ResPublicatedImage;

public interface PublicatedImageService {
	
	ResPublicatedImage save(String Description,MultipartFile file);
	
	void deleteById(Long id);
}
