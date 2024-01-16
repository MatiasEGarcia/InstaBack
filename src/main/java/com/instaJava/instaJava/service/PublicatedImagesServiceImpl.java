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

import com.instaJava.instaJava.dao.CommentDao;
import com.instaJava.instaJava.dao.PublicatedImagesDao;
import com.instaJava.instaJava.dto.CommentDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.UserDto;
import com.instaJava.instaJava.dto.response.PublicatedImageDto;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Comment;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.InvalidImageException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.mapper.CommentMapper;
import com.instaJava.instaJava.mapper.PublicatedImageMapper;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublicatedImagesServiceImpl implements PublicatedImageService {

	private final Clock clock;
	private final PublicatedImagesDao publicatedImagesDao;
	private final CommentDao commentDao;
	private final MessagesUtils messUtils;
	private final PageableUtils pagUtils;
	private final FollowService followService;
	private final UserService userService;
	private final PublicatedImageMapper publicatedImageMapper;
	private final CommentMapper commentMapper;

	
	@Override
	@Transactional
	public PublicatedImageDto save(String description, MultipartFile file) {
		if (file == null || file.isEmpty())
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		PublicatedImage publicatedImage;
		try {
			publicatedImage = PublicatedImage.builder().description(description)
					.image(Base64.getEncoder().encodeToString(file.getBytes()))
					.userOwner((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
					.createdAt(ZonedDateTime.now(clock)).build();
		} catch (Exception e) {
			throw new InvalidImageException(messUtils.getMessage("generic.image-base-64"),HttpStatus.BAD_REQUEST, e);
		}
		
		publicatedImage = publicatedImagesDao.save(publicatedImage);

		return publicatedImageMapper.publicatedImageToPublicatedImageDto(publicatedImage);
	}

	
	@Override
	@Transactional
	public void deleteById(Long id) {
		if (id == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		User authUser;
		PublicatedImage publiImage = publicatedImagesDao.findById(id).orElseThrow(() ->
				new RecordNotFoundException(messUtils.getMessage("publiImage.not-found"),HttpStatus.NOT_FOUND));
		authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (!publiImage.getUserOwner().getId().equals(authUser.getId())) {
			throw new InvalidActionException(messUtils.getMessage("generic.auth-user-no-owner"),HttpStatus.BAD_REQUEST);
		}
		publicatedImagesDao.deleteById(id);
	}

	//check test
	@Override
	@Transactional(readOnly = true)
	public PublicatedImageDto getById(Long id, PageInfoDto pageInfoDtoComments) {
		if (id == null || pageInfoDtoComments == null || pageInfoDtoComments.getSortDir() == null || pageInfoDtoComments.getSortField() == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		PublicatedImageDto publicatedImageDto;
		ResPaginationG<CommentDto> commentResPaginationG;
		Page<Comment> commentsPage;
		FollowStatus followStatus;
		PublicatedImage publicatedImage = publicatedImagesDao.findById(id).orElseThrow(() -> 
				new RecordNotFoundException(messUtils.getMessage("publiImage.not-found"), List.of(id.toString()), HttpStatus.NOT_FOUND));
		
		followStatus = followService.getFollowStatusByFollowedId( publicatedImage.getUserOwner().getId() );
		
		//check ownerUser visibility and follow status
		if(!publicatedImage.getUserOwner().isVisible() && followStatus != FollowStatus.ACCEPTED) {
			throw new InvalidActionException(messUtils.getMessage("publiImage.follow-status-not-accepted"), HttpStatus.BAD_REQUEST);
		}
		
		publicatedImageDto = publicatedImageMapper.publicatedImageToPublicatedImageDto(publicatedImage);
		
		//getting root comments
		
		//getting from dao
		commentsPage = commentDao.getRootCommentsByAssociatedImage(id, pagUtils.getPageable(pageInfoDtoComments));
		//mapping
		commentResPaginationG = commentMapper.pageAndPageInfoDtoToResPaginationG(commentsPage, pageInfoDtoComments);
		//set
		publicatedImageDto.setRootComments(commentResPaginationG);
		return publicatedImageDto;
	}
	
	@Override
	@Transactional(readOnly = true)
	public Optional<PublicatedImage> findById(Long id) {
		if (id == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		return publicatedImagesDao.findById(id);
	}
	
	@Override
	@Transactional(readOnly = true)
	public ResPaginationG<PublicatedImageDto> getAllByOwnersVisibles(PageInfoDto pageInfoDto) {
		if (pageInfoDto == null || pageInfoDto.getSortDir() == null || pageInfoDto.getSortField() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		Page<PublicatedImage> page = publicatedImagesDao.findByUserOwnerVisible(true, pagUtils.getPageable(pageInfoDto));
		if(page.getContent().isEmpty()) {
			throw new RecordNotFoundException(messUtils.getMessage("publiImage.group-not-found"), HttpStatus.NO_CONTENT);
		}
		return publicatedImageMapper.pageAndPageInfoDtoToResPaginationG(page, pageInfoDto);
		
	}
	
	@Override
	@Transactional(readOnly= true)
	public ResPaginationG<PublicatedImageDto> getAllByOnwer(Long ownerId, PageInfoDto pageInfoDto){
		if (ownerId == null || pageInfoDto == null || pageInfoDto.getSortDir() == null
				|| pageInfoDto.getSortField() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		Page<PublicatedImage> publicatedImageFoundPage;
		UserDto ownerUser;
		
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(authUser.getId() != ownerId) {
			ownerUser = userService.getById(ownerId);
			//if the owner user is not visible we need to check the followStatus
			if(!ownerUser.isVisible()) {
				FollowStatus followStatus = followService.getFollowStatusByFollowedId(ownerId);
				switch (followStatus) {
				case NOT_ASKED:
					throw new InvalidActionException(messUtils.getMessage("follow.followStatus-not-asked"), HttpStatus.BAD_REQUEST);
				case REJECTED:
					throw new InvalidActionException(messUtils.getMessage("follow.followStatus-rejected"), HttpStatus.BAD_REQUEST);
				case IN_PROCESS:
					throw new InvalidActionException(messUtils.getMessage("follow.followStatus-in-process"), HttpStatus.BAD_REQUEST);
				case ACCEPTED:
					break;
				default:
					throw new IllegalArgumentException("Unexpected value: " + followStatus);
				}
			}

		}
		
		// if owner is the same than the user is auth then we return publications, and
		// if the user is visible or follow status is Accepted too.
		publicatedImageFoundPage = publicatedImagesDao.findByUserOwnerId(ownerId, pagUtils.getPageable(pageInfoDto));
		if(!publicatedImageFoundPage.hasContent()) {
			throw new RecordNotFoundException(messUtils.getMessage("publiImage.group-not-found"), HttpStatus.NO_CONTENT);
		}
		return publicatedImageMapper.pageAndPageInfoDtoToResPaginationG(publicatedImageFoundPage, pageInfoDto);
	}
	

	@Override
	@Transactional(readOnly=true)
	public Long countPublicationsByOwnerId(Long id) {
		if(id == null) throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		userService.getById(id);//if there is not an exception then the user exists and the request can continue.
		return publicatedImagesDao.countByUserOwnerId(id);
	}


	//falta tests
	@Override
	@Transactional(readOnly = true)
	public ResPaginationG<PublicatedImageDto> getPublicationsFromUsersFollowed(PageInfoDto pageInfoDto) {
		if(pageInfoDto == null || pageInfoDto.getSortField() == null || pageInfoDto.getSortField().isBlank() ||
				pageInfoDto.getSortDir() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Page<PublicatedImage> page = publicatedImagesDao.findPublicationsFromUsersFollowed(authUser.getId(),
					pagUtils.getPageable(pageInfoDto));
		if(!page.hasContent()) {
			throw new RecordNotFoundException(messUtils.getMessage("publiImage.group-not-found"), HttpStatus.NO_CONTENT);
		}
		
		return publicatedImageMapper.pageAndPageInfoDtoToResPaginationG(page, pageInfoDto);
	}


	

}
