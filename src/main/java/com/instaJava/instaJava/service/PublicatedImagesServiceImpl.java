package com.instaJava.instaJava.service;

import java.io.IOException;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Base64;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dao.PublicatedImagesDao;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.exception.ImageException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublicatedImagesServiceImpl implements PublicatedImageService {

	private final Clock clock;
	private final PublicatedImagesDao publicatedImagesDao;
	

	@Override
	@Transactional
	public PublicatedImage save(String Description, MultipartFile file) {
		PublicatedImage publicatedImage;
		try {
			publicatedImage = PublicatedImage.builder()
					.description(Description)
					.image(Base64.getEncoder().encodeToString(file.getBytes()))
					.userOwner((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
					.createdAt(ZonedDateTime.now(clock))
					.build();
		} catch (IOException e) {
			throw new ImageException(e);
		}
		
		return publicatedImagesDao.save(publicatedImage);
	}

	@Override
	@Transactional
	public void deleteById(Long id) {//I should search if the entity exist first
		publicatedImagesDao.deleteById(id);
	}
	
}
