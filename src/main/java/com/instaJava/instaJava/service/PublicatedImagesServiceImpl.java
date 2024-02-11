package com.instaJava.instaJava.service;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dao.PublicatedImagesDao;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.InvalidImageException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublicatedImagesServiceImpl implements PublicatedImageService {

	private final Clock clock;
	private final PublicatedImagesDao publicatedImagesDao;
	private final MessagesUtils messUtils;
	private final PageableUtils pagUtils;
	
	@Override
	@Transactional(readOnly = true)
	public Optional<PublicatedImage> findById(Long id) {
		if (id == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		return publicatedImagesDao.findById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public Long countPublicationsByOwnerId(Long id) {
		if (id == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		return publicatedImagesDao.countByUserOwnerId(id);
	}

	@Override
	@Transactional(readOnly = true)
	public PublicatedImage getById(Long id) {
		if (id == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		return publicatedImagesDao.findById(id)
				.orElseThrow(() -> new RecordNotFoundException(messUtils.getMessage("publiImage.not-found"),
						List.of(id.toString()), HttpStatus.NOT_FOUND));
	}

	@Override
	@Transactional
	public PublicatedImage save(String description, MultipartFile file) {
		if (file == null || file.isEmpty())
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		PublicatedImage publicatedImage;
		try {
			publicatedImage = PublicatedImage.builder().description(description)
					.image(Base64.getEncoder().encodeToString(file.getBytes()))
					.userOwner((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
					.createdAt(ZonedDateTime.now(clock)).build();
		} catch (Exception e) {
			throw new InvalidImageException(messUtils.getMessage("generic.image-base-64"), HttpStatus.BAD_REQUEST, e);
		}

		return publicatedImagesDao.save(publicatedImage);
	}
	
	@Override
	@Transactional
	public PublicatedImage deleteById(Long publicatedImageId) {
		if (publicatedImageId == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		User authUser;
		PublicatedImage publiImage = publicatedImagesDao.findById(publicatedImageId).orElseThrow(
				() -> new RecordNotFoundException(messUtils.getMessage("publiImage.not-found"), HttpStatus.NOT_FOUND));
		authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (!publiImage.getUserOwner().getId().equals(authUser.getId())) {
			throw new InvalidActionException(messUtils.getMessage("generic.auth-user-no-owner"),
					HttpStatus.BAD_REQUEST);
		}
		publicatedImagesDao.delete(publiImage);
		return publiImage;
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<PublicatedImage> getAllByOwnerVisible(PageInfoDto pageInfoDto) {
		if (pageInfoDto == null || pageInfoDto.getSortDir() == null || pageInfoDto.getSortField() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		Page<PublicatedImage> page = publicatedImagesDao.findByUserOwnerVisible(true,
				pagUtils.getPageable(pageInfoDto));
		if (page.getContent().isEmpty()) {
			throw new RecordNotFoundException(messUtils.getMessage("publiImage.group-not-found"),
					HttpStatus.NO_CONTENT);
		}
		return page;
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<PublicatedImage> getAllByOnwerId(Long ownerId, PageInfoDto pageInfoDto) {
		if (ownerId == null || pageInfoDto == null || pageInfoDto.getSortDir() == null
				|| pageInfoDto.getSortField() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		Page<PublicatedImage> page = publicatedImagesDao.findByUserOwnerId(ownerId, pagUtils.getPageable(pageInfoDto));
		if (!page.hasContent()) {
			throw new RecordNotFoundException(messUtils.getMessage("publiImage.group-not-found"),
					HttpStatus.NO_CONTENT);
		}
		return page;
	}
	
	@Override
	public Page<PublicatedImage> getPublicationsFromUsersFollowed(PageInfoDto pageInfoDto) {
		if (pageInfoDto == null || pageInfoDto.getSortField() == null || pageInfoDto.getSortField().isBlank()
				|| pageInfoDto.getSortDir() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Page<PublicatedImage> page = publicatedImagesDao.findPublicationsFromUsersFollowed(authUser.getId(),
				pagUtils.getPageable(pageInfoDto));
		if (!page.hasContent()) {
			throw new RecordNotFoundException(messUtils.getMessage("publiImage.group-not-found"),
					HttpStatus.NO_CONTENT);
		}
		return page;
	}

}
