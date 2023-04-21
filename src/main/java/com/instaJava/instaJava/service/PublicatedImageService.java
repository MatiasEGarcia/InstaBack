package com.instaJava.instaJava.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.entity.PublicatedImage;

public interface PublicatedImageService {
	
	PublicatedImage save(String Description,MultipartFile file);
	
	void deleteById(Long id);
	
	Optional<PublicatedImage> getById(Long id);
	
	Page<PublicatedImage> getAllByUser(PageInfoDto pageInfoDto);
	
	Page<PublicatedImage> getAllByOwnersVisibles(PageInfoDto pageInfoDto);
	
	Page<PublicatedImage> getAllByOwnerId(PageInfoDto pageInfoDto, Long OwnerId);
}
