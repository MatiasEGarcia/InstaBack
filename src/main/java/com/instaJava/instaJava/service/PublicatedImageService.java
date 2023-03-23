package com.instaJava.instaJava.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.entity.PublicatedImage;

public interface PublicatedImageService {
	
	PublicatedImage save(String Description,MultipartFile file);
	
	void deleteById(Long id);
	
	PublicatedImage findById(Long id);
	
	Page<PublicatedImage> findPublicatedImagesByOwner(int pageNo, int pageSize);
	
	Page<PublicatedImage> findPublicatedImagesByOwnerSorted(int pageNo, int pageSize, String sortField, String sortDir);
}
