package com.instaJava.instaJava.service;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dao.PublicatedImagesDao;
import com.instaJava.instaJava.dto.response.ResPublicatedImage;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.exception.ImageException;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PublicatedImagesServiceImpl implements PublicatedImageService {

	private final PublicatedImagesDao publicatedImagesDao;

	@Override
	@Transactional
	public ResPublicatedImage save(String Description, MultipartFile file) {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Clock clock = Clock.systemUTC();
		PublicatedImage publicatedImage;
		try {
			publicatedImage = PublicatedImage.builder()
					.description(Description)
					.image(Base64.getEncoder().encodeToString(file.getBytes()))
					.userOwner(user)
					.createdAt(ZonedDateTime.now(clock))
					.build();
		} catch (IOException e) {
			throw new ImageException(e);
		}
		publicatedImage = publicatedImagesDao.save(publicatedImage);
		return ResPublicatedImage.builder()
				.id(publicatedImage.getId())
				.description(publicatedImage.getDescription())
				.image(publicatedImage.getImage())
				.createdAt(publicatedImage.getCreatedAt())
				.userOwner(user.getUsername())
				.build();
	}

	@Override
	@Transactional
	public void deleteById(Long id) {//I should search if the entity exist first
		publicatedImagesDao.deleteById(id);
	}
	
	
	
	
	
	
	
	
	
	
	
}
