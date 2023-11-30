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
import com.instaJava.instaJava.dto.UserDto;
import com.instaJava.instaJava.dto.response.PublicatedImageDto;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.exception.IllegalActionException;
import com.instaJava.instaJava.exception.ImageException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.mapper.PublicatedImageMapper;
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
	private final FollowService followService;
	private final UserService userService;
	private final PublicatedImageMapper publicatedImageMapper;

	
	@Override
	@Transactional
	public PublicatedImageDto save(String description, MultipartFile file) {
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
		
		publicatedImage = publicatedImagesDao.save(publicatedImage);

		return publicatedImageMapper.publicatedImageToPublicatedImageDto(publicatedImage);
	}

	
	@Override
	@Transactional
	public void deleteById(Long id) {
		if (id == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		User authUser;
		PublicatedImageDto publiImageDto = getById(id);
		authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (!publiImageDto.getUserOwner().getUserId().equals(authUser.getUserId().toString())) {
			throw new IllegalActionException(messUtils.getMessage("exception.owner-not-same"));
		}
		publicatedImagesDao.deleteById(id);
	}

	
	@Override
	@Transactional(readOnly = true)
	public PublicatedImageDto getById(Long id) {
		if (id == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		Optional<PublicatedImage> optPublicatedImage = publicatedImagesDao.findById(id);
		if(optPublicatedImage.isEmpty()) {
			throw new RecordNotFoundException(messUtils.getMessage("excepcion.record-by-id-not-found"), "pubImaId", List.of(id.toString()), HttpStatus.NOT_FOUND);
		}
		
		return publicatedImageMapper.publicatedImageToPublicatedImageDto(optPublicatedImage.get());
	}
	
	@Override
	@Transactional(readOnly = true)
	public Optional<PublicatedImage> findById(Long id) {
		if (id == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		return publicatedImagesDao.findById(id);
	}
	
	@Override
	@Transactional(readOnly = true)
	public ResPaginationG<PublicatedImageDto> getAllByOwnersVisibles(PageInfoDto pageInfoDto) {
		if (pageInfoDto == null || pageInfoDto.getSortDir() == null || pageInfoDto.getSortField() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		}
		Page<PublicatedImage> page = publicatedImagesDao.findByUserOwnerVisible(true, pagUtils.getPageable(pageInfoDto));
		if(page.getContent().isEmpty()) {
			throw new RecordNotFoundException(messUtils.getMessage("exception.publications-not-found"), HttpStatus.NO_CONTENT);
		}
		return publicatedImageMapper.pageAndPageInfoDtoToResPaginationG(page, pageInfoDto);
		
	}
	
	@Override
	@Transactional(readOnly= true)
	public ResPaginationG<PublicatedImageDto> getAllByOnwer(Long ownerId, PageInfoDto pageInfoDto){
		if (ownerId == null || pageInfoDto == null || pageInfoDto.getSortDir() == null
				|| pageInfoDto.getSortField() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		}
		Page<PublicatedImage> publicatedImageFoundPage;
		UserDto ownerUser;
		
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(authUser.getUserId() != ownerId) {
			ownerUser = userService.getById(ownerId);
			//if the owner user is not visible we need to check the followStatus
			if(!ownerUser.isVisible()) {
				FollowStatus followStatus = followService.getFollowStatusByFollowedId(ownerId);
				switch (followStatus) {
				case NOT_ASKED:
					throw new IllegalActionException(messUtils.getMessage("mess.followStatus-not-asked"), HttpStatus.BAD_REQUEST);
				case REJECTED:
					throw new IllegalActionException(messUtils.getMessage("mess.followStatus-rejected"), HttpStatus.BAD_REQUEST);
				case IN_PROCESS:
					throw new IllegalActionException(messUtils.getMessage("mess.followStatus-in-process"), HttpStatus.BAD_REQUEST);
				case ACCEPTED:
					break;
				default:
					throw new IllegalArgumentException("Unexpected value: " + followStatus);
				}
			}

		}
		
		// if owner is the same than the user is auth then we return publications, and
		// if the user is visible or follow status is Accepted too.
		publicatedImageFoundPage = publicatedImagesDao.findByUserOwner(ownerId, pagUtils.getPageable(pageInfoDto));
		if(!publicatedImageFoundPage.hasContent()) {
			throw new RecordNotFoundException(messUtils.getMessage("exception.publications-not-found"), HttpStatus.NO_CONTENT);
		}
		return publicatedImageMapper.pageAndPageInfoDtoToResPaginationG(publicatedImageFoundPage, pageInfoDto);
	}
	

	@Override
	@Transactional(readOnly=true)
	public Long countPublicationsByOwnerId(Long id) {
		if(id == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		userService.getById(id);//if there is not an exception then the user exists and the request can continue.
		return publicatedImagesDao.countByUserOwner(id);
	}


	

}
