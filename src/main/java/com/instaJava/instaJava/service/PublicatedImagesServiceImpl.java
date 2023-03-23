package com.instaJava.instaJava.service;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dao.PublicatedImagesDao;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.exception.ImageException;
import com.instaJava.instaJava.util.MessagesUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublicatedImagesServiceImpl implements PublicatedImageService {

	private final Clock clock;
	private final PublicatedImagesDao publicatedImagesDao;
	private final MessagesUtils messUtils;
	

	@Override
	@Transactional
	public PublicatedImage save(String description, MultipartFile file) {
		if(file == null || file.isEmpty()) throw new IllegalArgumentException(messUtils.getMessage("exepcion.argument-not-null-empty"));
		PublicatedImage publicatedImage;
		try {
			publicatedImage = PublicatedImage.builder()
					.description(description)
					.image(Base64.getEncoder().encodeToString(file.getBytes()))
					.userOwner((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
					.createdAt(ZonedDateTime.now(clock))
					.build();
		} catch (Exception e) {
			throw new ImageException(e);
		}
		
		return publicatedImagesDao.save(publicatedImage);
	}

	@Override
	@Transactional
	public void deleteById(Long id) {//I should search if the entity exist first
		findById(id); //if not exist will throw an Exception
		publicatedImagesDao.deleteById(id);
	}
	
	@Override
	@Transactional(readOnly = true)
	public PublicatedImage findById(Long id) {
		Optional<PublicatedImage> publicatedImage = publicatedImagesDao.findById(id);
		if(publicatedImage.isEmpty()) throw new IllegalArgumentException(messUtils.getMessage("exepcion.publicatedImage-not-found-id"));
		return publicatedImage.get();
	}

	@Override
	@Transactional(readOnly = true)
	public Page<PublicatedImage> findPublicatedImagesByOwner(int pageNo, int pageSize) {
		//first page for the most people is 1 , but for us is 0 
		Pageable pag= PageRequest.of(pageNo - 1, pageSize);
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Page<PublicatedImage> pagePublicatedImage = publicatedImagesDao.findPublicatedImagesByOwner(user, pag);
		return pagePublicatedImage;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
