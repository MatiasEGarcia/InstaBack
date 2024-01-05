package com.instaJava.instaJava.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dao.FollowDao;
import com.instaJava.instaJava.dto.FollowDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.UserDto;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Follow;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.exception.AlreadyExistsException;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.mapper.FollowMapper;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

	private final UserService userService;
	private final FollowDao followDao;
	private final MessagesUtils messUtils;
	private final FollowMapper followMapper;
	private final SpecificationService<Follow> specService;
	private final PageableUtils pagUtils;
	private final NotificationService notificationService;

	
	@Override
	@Transactional
	public FollowDto save(Long followedId) {
		if (followedId == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		User userFollower;
		Follow follow;
		String customMessage;
		User userFollowed;
		UserDto userDtoFollowed = userService.getById(followedId);
		// check if the follow record already exists.
		userFollower = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (existsByFollowedAndFollower(followedId)) {
			throw new AlreadyExistsException(messUtils.getMessage("generic.create-record-already.exists"),HttpStatus.BAD_REQUEST);
		}
		userFollowed = User.builder()
				.userId(Long.parseLong(userDtoFollowed.getUserId()))
				.build();
		follow = Follow.builder().followed(userFollowed).follower(userFollower).build();
		// set follow status by visible state of the user wanted to follow
		if (userDtoFollowed.isVisible()) {
			follow.setFollowStatus(FollowStatus.ACCEPTED);
			customMessage = "A new user is following you";
		} else {
			follow.setFollowStatus(FollowStatus.IN_PROCESS);
			customMessage = "A new user wants to follow you";
		}
		// save notification for the followed user
		notificationService.saveNotificationOfFollow(follow, customMessage);
		Follow followCreated = followDao.save(follow);
		return followMapper.followToFollowDto(followCreated);
	}

	@Override
	@Transactional(readOnly = true)
	public ResPaginationG<FollowDto> search(PageInfoDto pageInfoDto, ReqSearchList reqSearchList) {
		if (pageInfoDto == null || reqSearchList == null || pageInfoDto.getSortField() == null
				|| pageInfoDto.getSortDir() == null)
			throw new IllegalArgumentException(messUtils.getMessage("exepcion.argument-not-null-empty"));
		Specification<Follow> spec = specService.getSpecification(reqSearchList.getReqSearchs(),
				reqSearchList.getGlobalOperator());
		Page<Follow> page = followDao.findAll(spec, pagUtils.getPageable(pageInfoDto));
		if(page.getContent().isEmpty()) {
			throw new RecordNotFoundException(messUtils.getMessage("follow.not-found"), HttpStatus.NO_CONTENT);
		}
		return followMapper.pageAndPageInfoDtoToResPaginationG(page, pageInfoDto);
	}

	@Override
	@Transactional
	public FollowDto updateFollowStatusById(Long id, FollowStatus newStatus) {
		if (newStatus == null || id == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		User userFollowed;
		FollowDto follower = findById(id);
		userFollowed = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (!follower.getFollowed().getUserId().equalsIgnoreCase(userFollowed.getUserId().toString()))
			throw new IllegalArgumentException(messUtils.getMessage("follow.followed-not-same"));
		follower.setFollowStatus(newStatus);
		Follow followCreated = followDao.save(followMapper.followDtoToFollow(follower));
		return followMapper.followToFollowDto(followCreated);
	}

	@Override
	@Transactional
	public FollowDto updateFollowStatusByFollower(Long followerUserId, FollowStatus newStatus) {
		if(followerUserId == null ||  newStatus == null) throw new IllegalArgumentException("generic.arg-not-null");
		User authUser;
		Follow follow;
		userService.getById(followerUserId);//if the user no exists throw exception.
		authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		//getting follow record
		follow = followDao.findOneByFollowedUserIdAndFollowerUserId(authUser.getUserId(), followerUserId)
				.orElseThrow(() -> new RecordNotFoundException(messUtils.getMessage("follow.not-found"),HttpStatus.NOT_FOUND));
		//updating follow recod
		follow.setFollowStatus(newStatus);
		follow = followDao.save(follow);
		//mapping
		return followMapper.followToFollowDto(follow);
	}
	
	
	@Override
	@Transactional(readOnly = true)
	public FollowDto findById(Long id) {
		if(id == null) throw new IllegalArgumentException("generic.arg-not-null");
		Optional<Follow> followerOpt = followDao.findById(id);
		if (followerOpt.isEmpty())
			throw new RecordNotFoundException(messUtils.getMessage("follow.not-found"),HttpStatus.NOT_FOUND);
		Follow followFound = followerOpt.get();
		return followMapper.followToFollowDto(followFound);
	}

	
	@Override
	@Transactional(readOnly = true)
	public boolean existsByFollowedAndFollower(Long followedId) {
		if (followedId == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		User follower = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return followDao.existsByFollowedUserIdAndFollowerUserId(followedId, follower.getUserId());
	}

	@Override
	@Transactional
	public void deleteById(Long id) {
		if (id == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		FollowDto foll = findById(id);
		User userFollower = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (!foll.getFollower().getUserId().equalsIgnoreCase(userFollower.getUserId().toString()))
			throw new InvalidActionException(messUtils.getMessage("follow.follower-not-same"),HttpStatus.BAD_REQUEST);
		followDao.delete(followMapper.followDtoToFollow(foll));
	}

	
	
	@Override
	@Transactional(readOnly = true)
	public FollowStatus getFollowStatusByFollowedId(Long id) {
		if (id == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		Optional<Follow> optFollow;
		User userFollower;
		
		//checking that followed user exists.
		userService.getById(id);
		
		userFollower = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		optFollow = followDao.findOneByFollowedUserIdAndFollowerUserId(id, userFollower.getUserId());
		
		if (optFollow.isEmpty())
			return FollowStatus.NOT_ASKED;
		return optFollow.get().getFollowStatus();
	}
	
	@Override
	@Transactional(readOnly = true)
	public FollowStatus getFollowStatusByFollowerId(Long followerId) {
		if(followerId == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		Optional<Follow> optFollow;
		User userFollowed;
		
		//checking that follower user exists
		userService.getById(followerId);
		
		userFollowed = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		optFollow = followDao.findOneByFollowedUserIdAndFollowerUserId(userFollowed.getUserId(), followerId);
		
		if(optFollow.isEmpty())
			return FollowStatus.NOT_ASKED;
		return optFollow.get().getFollowStatus();
	}

	@Override
	@Transactional(readOnly = true)
	public Long countByFollowStatusAndFollowed(FollowStatus followStatus,Long followedId) {
		if (followedId == null || followStatus == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));

		return followDao.countByFollowedUserIdAndFollowStatus(followedId, followStatus);
	}

	@Override
	@Transactional(readOnly = true)
	public Long countByFollowStatusAndFollower(FollowStatus followStatus, Long followerId) {
		if (followerId == null || followStatus == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));

		return followDao.countByFollowerUserIdAndFollowStatus(followerId, followStatus);
	}

}
