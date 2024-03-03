package com.instaback.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.instaback.dto.PageInfoDto;
import com.instaback.dto.response.PublicatedImageDto;
import com.instaback.dto.response.ResPaginationG;
import com.instaback.entity.Comment;
import com.instaback.entity.PublicatedImage;
import com.instaback.entity.User;
import com.instaback.enums.FollowStatus;
import com.instaback.exception.InvalidActionException;
import com.instaback.exception.RecordNotFoundException;
import com.instaback.mapper.CommentMapper;
import com.instaback.mapper.PublicatedImageMapper;
import com.instaback.service.CommentService;
import com.instaback.service.FollowService;
import com.instaback.service.LikeService;
import com.instaback.service.PublicatedImageService;
import com.instaback.service.UserService;
import com.instaback.util.MessagesUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublicatedImageApplicationImpl implements PublicatedImageApplication{

	private final PublicatedImageService pImageService;
	private final FollowService followService;
	private final CommentService commentService;
	private final UserService userService;
	private final LikeService likeService;
	private final PublicatedImageMapper pImageMapper;
	private final CommentMapper commentMapper;
	private final MessagesUtils messUtils;
	

	@Override
	public PublicatedImageDto save(String Description, MultipartFile file) {
		PublicatedImage pImage = pImageService.save(Description, file);
		return pImageMapper.publicatedImageToPublicatedImageDto(pImage);
	}
	
	@Override
	public PublicatedImageDto deleteById(Long publicatedImageId) {
		PublicatedImage pImage = pImageService.deleteById(publicatedImageId);
		return pImageMapper.publicatedImageToPublicatedImageDto(pImage);
	}
	
	//check tests
	@Override
	public PublicatedImageDto getById(Long id, int pageNo, int pageSize, String sortField, Direction sortDir) {
		User authUser;
		FollowStatus imageOwnerFollowStatus;
		PublicatedImage publicatedImage;
		PublicatedImageDto publicatedImageDto;
		Page<Comment> pageComments;
		Long numberOfPositiveLikes;
		Long numberOfNegativeLikes;
		PageInfoDto commentsPageInfoDto;
		//getting publicatedImage
		publicatedImage = pImageService.getById(id);
		//checking if the auth user can get the publicatedImage record.
		authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (!publicatedImage.getUserOwner().equals(authUser)) {
			imageOwnerFollowStatus = followService.getFollowStatusByFollowedId(publicatedImage.getUserOwner().getId());
			// check ownerUser visibility and follow status
			if (!publicatedImage.getUserOwner().isVisible() && imageOwnerFollowStatus != FollowStatus.ACCEPTED) {
				throw new InvalidActionException(messUtils.getMessage("publiImage.follow-status-not-accepted"),
						HttpStatus.BAD_REQUEST);
			}
		}
		//is liked by the auth user?
		likeService.setItemDecision(publicatedImage);
		//gettting number of likes, positive and negative
		numberOfPositiveLikes = likeService.getLikesNumberByItemIdAndDecision(id,true);
		numberOfNegativeLikes = likeService.getLikesNumberByItemIdAndDecision(id,false);
		//getting rootComments
		commentsPageInfoDto = new PageInfoDto(pageNo, pageSize, 0, 0, sortField, sortDir);
		try {
			pageComments = commentService.getRootCommentsByAssociatedImgId(id, commentsPageInfoDto);
		}catch(RecordNotFoundException e) {//if there are not comments.
			pageComments = Page.empty();
		}
		//mapping
		publicatedImageDto = pImageMapper.publicatedImageToPublicatedImageDto(publicatedImage);
		publicatedImageDto.setRootComments(commentMapper.pageAndPageInfoDtoToResPaginationG(pageComments, commentsPageInfoDto));
		publicatedImageDto.setNumberPositiveLikes(numberOfPositiveLikes.toString());
		publicatedImageDto.setNumberNegativeLikes(numberOfNegativeLikes.toString());
		return publicatedImageDto;
	}

	
	@Override
	public ResPaginationG<PublicatedImageDto> getAllByOwnersVisibles(int pageNo, int pageSize, String sortField,
			Direction sortDir) {
		Page<PublicatedImage> pagePImge;
		PageInfoDto pInfoDto = new PageInfoDto(pageNo, pageSize, 0, 0, sortField, sortDir);
		pagePImge = pImageService.getAllByOwnerVisible(pInfoDto);
		likeService.setItemDecisions(pagePImge.getContent());
		return pImageMapper.pageAndPageInfoDtoToResPaginationG(pagePImge, pInfoDto);
	}

	
	@Override
	public ResPaginationG<PublicatedImageDto> getAllByOnwerId(Long ownerId, int pageNo, int pageSize, String sortField,
			Direction sortDir) {
		Page<PublicatedImage> pImagePage;
		PageInfoDto pageInfoDto;
		User ownerUser;
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		if (authUser.getId() != ownerId) {
			ownerUser = userService.findById(ownerId);
			// if the owner user is not visible we need to check the followStatus
			if (!ownerUser.isVisible()) {
				FollowStatus followStatus = followService.getFollowStatusByFollowedId(ownerId);
				switch (followStatus) {
				case NOT_ASKED:
					throw new InvalidActionException(messUtils.getMessage("follow.followStatus-not-asked"),
							HttpStatus.BAD_REQUEST);
				case REJECTED:
					throw new InvalidActionException(messUtils.getMessage("follow.followStatus-rejected"),
							HttpStatus.BAD_REQUEST);
				case IN_PROCESS:
					throw new InvalidActionException(messUtils.getMessage("follow.followStatus-in-process"),
							HttpStatus.BAD_REQUEST);
				case ACCEPTED:
					break;
				default:
					throw new IllegalArgumentException("Unexpected value: " + followStatus);
				}
			}
		}
		pageInfoDto = new PageInfoDto(pageNo, pageSize, 0, 0, sortField, sortDir);
		pImagePage = pImageService.getAllByOnwerId(ownerId, pageInfoDto);
		likeService.setItemDecisions(pImagePage.getContent());
		return pImageMapper.pageAndPageInfoDtoToResPaginationG(pImagePage, pageInfoDto);
	}

	
	@Override
	public ResPaginationG<PublicatedImageDto> getPublicationsFromUsersFollowed(int pageNo, int pageSize,
			String sortField, Direction sortDir) {
		PageInfoDto pageInfoDto = new PageInfoDto(pageNo, pageSize, 0, 0, sortField, sortDir);
		Page<PublicatedImage> pagePImages = pImageService.getPublicationsFromUsersFollowed(pageInfoDto);
		likeService.setItemDecisions(pagePImages.getContent());
		return pImageMapper.pageAndPageInfoDtoToResPaginationG(pagePImages, pageInfoDto);
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
