package com.instaJava.instaJava.service;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dao.PublicatedImagesDao;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.OperationEnum;
import com.instaJava.instaJava.exception.IllegalActionException;
import com.instaJava.instaJava.exception.ImageException;
import com.instaJava.instaJava.util.MessagesUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublicatedImagesServiceImpl implements PublicatedImageService {

	private final Clock clock;
	private final PublicatedImagesDao publicatedImagesDao;
	private final MessagesUtils messUtils;
	private final SpecificationService<PublicatedImage> specService;

	@Override
	@Transactional
	public PublicatedImage save(String description, MultipartFile file) {
		if (file == null || file.isEmpty())
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null-empty"));
		PublicatedImage publicatedImage;
		try {
			publicatedImage = PublicatedImage.builder().description(description)
					.image(Base64.getEncoder().encodeToString(file.getBytes()))
					.userOwner((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
					.createdAt(ZonedDateTime.now(clock)).build();
		} catch (Exception e) {
			throw new ImageException(e);
		}

		return publicatedImagesDao.save(publicatedImage);
	}

	// to delete an publicatedImage the authenticated user has to be the owner
	@Override
	@Transactional
	public void deleteById(Long id) {
		if (id == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		User authUser;
		Optional<PublicatedImage> optPublImage = publicatedImagesDao.findById(id);
		if (optPublImage.isEmpty())
			return;
		authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (!optPublImage.get().getUserOwner().equals(authUser))
			throw new IllegalActionException(messUtils.getMessage("exception.owner-not-same"));
		publicatedImagesDao.deleteById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<PublicatedImage> getById(Long id) {
		if (id == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		return publicatedImagesDao.findById(id);
	}
	
	/*
	 * Return all the publicated images by the authenticated user
	 * */
	@Override
	@Transactional(readOnly = true)
	public Page<PublicatedImage> getAllByUser(PageInfoDto pageInfoDto){
		if (pageInfoDto == null || pageInfoDto.getSortDir() == null || pageInfoDto.getSortField() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		}
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		ReqSearch reqSearchPubliImaByOwnerEqual = ReqSearch.builder().column("userId")
				.dateValue(false).value(authUser.getUserId().toString()).joinTable("userOwner")
				.operation(OperationEnum.EQUAL).build();
		return publicatedImagesDao.findAll(specService.getSpecification(reqSearchPubliImaByOwnerEqual), this.getPageable(pageInfoDto));
	};

	@Override
	@Transactional(readOnly = true)
	public Page<PublicatedImage> getAllByOwnersVisibles(PageInfoDto pageInfoDto) {
		if (pageInfoDto == null || pageInfoDto.getSortDir() == null || pageInfoDto.getSortField() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		}
		ReqSearch reqSearchuserOwnersVisibleTrue = ReqSearch.builder().column("visible")
				.value("true").joinTable("userOwner").operation(OperationEnum.IS_TRUE).build();
		return publicatedImagesDao.findAll(specService.getSpecification(reqSearchuserOwnersVisibleTrue),this.getPageable(pageInfoDto));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<PublicatedImage> getAllByOwnerId(PageInfoDto pageInfoDto, Long ownerId) {
		if (pageInfoDto == null || ownerId == null || pageInfoDto.getSortDir() == null || pageInfoDto.getSortField() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		}
		ReqSearch reqSearchuserOwnerIdEqual = ReqSearch.builder().column("userId").dateValue(false)
				.value(ownerId.toString()).joinTable("userOwner").operation(OperationEnum.EQUAL).build();
		return publicatedImagesDao.findAll(specService.getSpecification(reqSearchuserOwnerIdEqual),this.getPageable(pageInfoDto));
	}

	
	private Pageable getPageable(PageInfoDto pageInfoDto) {
		Sort sort = pageInfoDto.getSortDir().equals(Sort.Direction.ASC)
				? Sort.by(pageInfoDto.getSortField()).ascending()
				: Sort.by(pageInfoDto.getSortField()).descending();
		// first page for the most people is 1 , but for us is 0
		 return  PageRequest.of(
				pageInfoDto.getPageNo() == 0 ? pageInfoDto.getPageNo() : pageInfoDto.getPageNo() - 1,
				pageInfoDto.getPageSize(), sort);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
